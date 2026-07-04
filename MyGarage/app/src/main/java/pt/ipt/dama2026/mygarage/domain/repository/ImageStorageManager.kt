package pt.ipt.dama2026.mygarage.domain.repository

/**
 * Interface para guardar e carregar imagens localmente.
 *
 * Define o contrato: quem quiser gerir imagens tem de saber guardar
 * (de URI), descarregar (de URL) e devolver o caminho do ficheiro.
 *
 * Implementação concreta: LocalImageStorageManager (internal storage).
 * Injetada via Hilt no AppModule.
 */
interface ImageStorageManager {

    suspend fun saveImage(uri: String): String?
    suspend fun downloadImage(url: String): String?
    fun getImagePath(fileName: String): String?
}
