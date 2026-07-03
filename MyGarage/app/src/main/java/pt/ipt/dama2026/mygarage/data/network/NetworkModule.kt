package pt.ipt.dama2026.mygarage.data.network

import android.content.Context
import android.content.pm.PackageManager

/**
 * Static network utilities. Hilt handles service creation in di/NetworkModule.kt.
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

    /** Builds a backend proxy URL for a raw S3 URL. */
    fun buildImageProxyUrl(context: Context, remoteUrl: String?): String? {
        if (remoteUrl.isNullOrBlank()) return null
        val base = readMyGarageApiUrl(context).trimEnd('/')
        val encoded = java.net.URLEncoder.encode(remoteUrl, "UTF-8")
        return "$base/api/images/proxy?url=$encoded"
    }

    /** Strips stray quotes from a remote URL. */
    fun cleanRemoteUrl(remoteUrl: String?): String? = remoteUrl?.replace("\"", "")
}
