package pt.ipt.dama2026.mygarage.data.network

import android.content.Context
import android.content.pm.PackageManager

/**
 *   A criação dos serviços Retrofit é feita no módulo Hilt.
 *
 * - readMyGarageApiUrl: lê o URL da API do AndroidManifest (MYGARAGE_API_URL),
 *   com fallback para o URL de produção no Railway.
 * - buildImageProxyUrl: constrói URL de proxy para imagens remotas no bucket (S3) do railway,
 *   passando pelo backend para evitar expor o URL real.
 * - cleanRemoteUrl: remove aspas extra de URLs recebidas do servidor, esta função é necessaria
 *   porque a API de upload de imagens devolve o URL entre aspas (ex.: "\"https://s3.amazonaws.com/...\""), 
 *   e o Coil não conseguiria carregar a imagem na UI.
 */
object NetworkModule {

    private fun readMyGarageApiUrl(context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("MYGARAGE_API_URL")
                ?: "https://mygaragebackend-production.up.railway.app"
        } catch (_: Exception) {
            "https://mygaragebackend-production.up.railway.app"
        }
    }

    fun buildImageProxyUrl(context: Context, remoteUrl: String?): String? {
        if (remoteUrl.isNullOrBlank()) return null
        val base = readMyGarageApiUrl(context).trimEnd('/')
        val encoded = java.net.URLEncoder.encode(remoteUrl, "UTF-8")
        return "$base/api/images/proxy?url=$encoded"
    }

    fun cleanRemoteUrl(remoteUrl: String?): String? = remoteUrl?.replace("\"", "")
}
