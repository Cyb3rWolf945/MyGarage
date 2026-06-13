package ipt.pt.mygarage.data.storage

import android.content.Context
import android.net.Uri
import ipt.pt.mygarage.domain.repository.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Concrete implementation of [ImageStorageManager] that stores
 * images inside the application's internal storage under a
 * dedicated "vehicle_images" subdirectory.
 *
 * @param context Android [Context] (the Application context is
 *   preferred to avoid accidental activity leaks).
 */
class LocalImageStorageManager(
    private val context: Context
) : ImageStorageManager {

    private val imagesDir: File
        get() = File(context.filesDir, IMAGES_SUBDIR).also { dir ->
            if (!dir.exists()) dir.mkdirs()
        }

    override suspend fun saveImage(uri: String): String? = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(uri)
            val fileName = "${UUID.randomUUID()}.$DEFAULT_EXTENSION"
            val targetFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null // input stream failed

            fileName
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    override fun getImagePath(fileName: String): String? {
        val file = File(imagesDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    companion object {
        private const val IMAGES_SUBDIR = "vehicle_images"
        private const val DEFAULT_EXTENSION = "jpg"
    }
}
