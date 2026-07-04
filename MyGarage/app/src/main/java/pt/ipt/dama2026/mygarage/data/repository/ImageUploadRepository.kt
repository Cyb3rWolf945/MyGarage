package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import pt.ipt.dama2026.mygarage.data.network.ImageUploadService
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Faz upload de imagens para o servidor e devolve o URL remoto.
 *
 *
 * Fluxo:
 * 1. Recebe um URI local (galeria ou câmara).
 * 2. Abre o ficheiro e descodifica para bitmap.
 * 3. Redimensiona para caber em 2000×2000px (mantendo a proporção).
 * 4. Comprime para JPEG com qualidade 80.
 * 5. Converte para byte array e envia como multipart para a API.
 * 6. Devolve o URL remoto (sem aspas) ou erro.
 */
@Singleton
class ImageUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageUploadService: ImageUploadService
) {

    /**
     * Abre o URI da imagem, descodifica, redimensiona e envia para o servidor.
     * O parâmetro imageType identifica o contexto (ex.: "vehicle", "avatar").
     * Remove aspas do URL devolvido pela API antes de retornar.
     */
    suspend fun uploadImage(
        uri: Uri,
        imageType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open image file"))

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                return@withContext Result.failure(Exception("Could not decode image"))
            }

            val compressedBitmap = compressImage(bitmap)
            val imageBytes = bitmapToByteArray(compressedBitmap)

            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
            val imageTypeBody = imageType.toRequestBody("text/plain".toMediaType())

            val response = imageUploadService.uploadImage(part, imageTypeBody)

            if (response.ok && response.imageUrl != null) {
                val cleanUrl = response.imageUrl.replace("\"", "")
                Result.success(cleanUrl)
            } else {
                Result.failure(Exception(response.error ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Redimensiona o bitmap para no máximo 2000×2000px, mantendo a proporção.
     * Se a imagem já for mais pequena, devolve-a sem alterações.
     * 2000px é suficiente para ecrãs de telemóvel e mantém o ficheiro leve.
     */
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val maxWidth = 2000
        val maxHeight = 2000
        val ratio: Float = Math.min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        if (ratio >= 1.0f) {
            return bitmap
        }

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /** Comprime o bitmap para JPEG com qualidade 80 e devolve como byte array. */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
}
