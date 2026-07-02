package pt.ipt.dama2026.mygarage.domain.repository

/**
 * Domain contract for image storage (save, download, retrieve path).
 */
interface ImageStorageManager {

    suspend fun saveImage(uri: String): String?
    suspend fun downloadImage(url: String): String?
    fun getImagePath(fileName: String): String?
}
