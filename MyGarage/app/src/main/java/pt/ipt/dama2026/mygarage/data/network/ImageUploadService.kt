package pt.ipt.dama2026.mygarage.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** Interface Retrofit para upload de imagens (multipart). */
interface ImageUploadService {
    @Multipart
    @POST("/api/images/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("imageType") imageType: RequestBody
    ): ImageUploadResponse
}

/** Resposta do endpoint de upload de imagem. */
data class ImageUploadResponse(
    val ok: Boolean,
    val imageUrl: String?,
    val error: String? = null
)
