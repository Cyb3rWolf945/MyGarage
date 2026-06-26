package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import pt.ipt.dama2026.mygarage.data.network.NetworkModule
import java.io.ByteArrayOutputStream

class ImageUploadRepository(private val context: Context) {
    private val imageUploadService = NetworkModule.createImageUploadService(context)

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
            // Send imageType as plain text RequestBody to avoid Gson wrapping it in JSON quotes
            val imageTypeBody = imageType.toRequestBody("text/plain".toMediaType())

            val response = imageUploadService.uploadImage(part, imageTypeBody)

            if (response.ok && response.imageUrl != null) {
                // Strip any stray quotes from the URL (defensive)
                val cleanUrl = response.imageUrl.replace("\"", "")
                Result.success(cleanUrl)
            } else {
                Result.failure(Exception(response.error ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
}
