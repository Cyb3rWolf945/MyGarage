package pt.ipt.dama2026.mygarage.data.network

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val MATRICULA_API_BASE_URL = "https://www.matricula.co.pt"

    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
    }

    private fun createMatriculaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MATRICULA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    fun createMatriculaApiService(): LicenseApiService {
        return createMatriculaRetrofit().create(LicenseApiService::class.java)
    }

    private fun readMyGarageApiUrl(context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("MYGARAGE_API_URL")
                ?: "https://mygaragebackend-production.up.railway.app"
        } catch (_: Exception) {
            "https://mygaragebackend-production.up.railway.app"
        }
    }

    /**
     * Builds a backend proxy URL for a raw S3 URL so the client doesn't need direct S3 access.
     */
    fun buildImageProxyUrl(context: Context, remoteUrl: String?): String? {
        if (remoteUrl.isNullOrBlank()) return null
        val base = readMyGarageApiUrl(context).trimEnd('/')
        val encoded = java.net.URLEncoder.encode(remoteUrl, "UTF-8")
        return "$base/api/images/proxy?url=$encoded"
    }

    private fun createAuthInterceptor(context: Context): Interceptor {
        val prefsRepo = UserPreferencesRepository(context)
        return Interceptor { chain ->
            val token = runBlocking { prefsRepo.userAuthTokenFlow.firstOrNull() }
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
    }

    private var _authRetrofit: Retrofit? = null

    private fun getAuthRetrofit(context: Context): Retrofit {
        return _authRetrofit ?: synchronized(this) {
            _authRetrofit ?: run {
                val client = OkHttpClient.Builder()
                    .addInterceptor(createAuthInterceptor(context))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                Retrofit.Builder()
                    .baseUrl(readMyGarageApiUrl(context))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .build()
                    .also { _authRetrofit = it }
            }
        }
    }

    fun createAuthApiService(context: Context): AuthApiService {
        return getAuthRetrofit(context).create(AuthApiService::class.java)
    }

    fun createSyncApiService(context: Context): SyncApiService {
        return getAuthRetrofit(context).create(SyncApiService::class.java)
    }

    fun createImageUploadService(context: Context): ImageUploadService {
        return getAuthRetrofit(context).create(ImageUploadService::class.java)
    }
}
