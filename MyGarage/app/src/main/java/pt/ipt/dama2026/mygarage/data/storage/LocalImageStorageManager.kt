package pt.ipt.dama2026.mygarage.data.storage

import android.content.Context
import android.net.Uri
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Guarda e carrega imagens no armazenamento interno da app.
 * As imagens ficam no armazenamento privado da app em si, na pasta "files/vehicle_images".
 * Esta pasta é privada — outras apps não lhe acedem.
 *
 * Três operações:
 * - saveImage: copia um ficheiro local (URI) para a pasta interna com nome único.
 * - downloadImage: descarrega uma imagem de um URL remoto e guarda localmente.
 * - getImagePath: devolve o caminho absoluto do ficheiro, ou null se não existir.
 *
 * Usado pelo SyncRepository (download de imagens remotas) e pelos ViewModels
 * (guardar fotos escolhidas da galeria).
 */
class LocalImageStorageManager(
    private val context: Context
) : ImageStorageManager {

    private val imagesDir: File
        get() = File(context.filesDir, IMAGES_SUBDIR).also { dir ->
            if (!dir.exists()) dir.mkdirs()
        }

    /**
     * Copia uma imagem da galeria (URI) para a pasta interna da app.
     * Gera um nome único (UUID.jpg). Devolve o nome do ficheiro ou null se falhar.
     */
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

    /**
     * Descarrega uma imagem de um URL (via HTTP GET) e guarda na pasta interna.
     * Timeout de 15s para ligar e 30s para ler. Se o código não for 200, devolve null.
     *
     * Usa HttpURLConnection em vez de Retrofit porque é um download simples
     * O Retrofit seria desnecessário aqui — só precisamos de um GET e guardar os bytes num ficheiro.
     */
    override suspend fun downloadImage(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val fileName = "${UUID.randomUUID()}.$DEFAULT_EXTENSION"
            val targetFile = File(imagesDir, fileName)

            connection.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()

            fileName
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /** Devolve o caminho absoluto do ficheiro se existir, ou null. */
    override fun getImagePath(fileName: String): String? {
        val file = File(imagesDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    companion object {
        private const val IMAGES_SUBDIR = "vehicle_images"
        private const val DEFAULT_EXTENSION = "jpg"
    }
}
