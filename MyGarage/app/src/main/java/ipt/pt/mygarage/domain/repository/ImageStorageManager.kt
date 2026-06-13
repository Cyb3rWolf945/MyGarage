package ipt.pt.mygarage.domain.repository

/**
 * Domain-layer contract for offline-first image storage.
 *
 * Decouples the rest of the app from Android framework classes
 * (e.g. [android.net.Uri]) and from concrete storage locations.
 */
interface ImageStorageManager {

    /**
     * Persists an image from [uri] (a content-URI string) to
     * internal storage and returns the generated unique file name,
     * or `null` if the operation failed.
     */
    suspend fun saveImage(uri: String): String?

    /**
     * Returns the absolute file-system path for a previously saved
     * image identified by [fileName], or `null` if the file does
     * not exist.
     */
    fun getImagePath(fileName: String): String?
}
