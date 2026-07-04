package pt.ipt.dama2026.mygarage.di

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import pt.ipt.dama2026.mygarage.data.network.AuthApiService
import pt.ipt.dama2026.mygarage.data.network.ImageUploadService
import pt.ipt.dama2026.mygarage.data.network.SyncApiService
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para a camada de rede (Retrofit + OkHttp).
 *
 * Providencia:
 * - URL base da API (lido do AndroidManifest, fallback Railway).
 * - OkHttpClient com interceptor de autenticação (adiciona header Authorization
 *   com o token JWT do DataStore automaticamente em todos os pedidos).
 * - Retrofit configurado com Gson (setLenient para tolerar JSON com pequenas
 *   irregularidades como vírgulas extra, sem necessidade de parse estrito).
 * - AuthApiService: endpoints de login/registo.
 * - SyncApiService: endpoints de sincronização e perfil.
 * - ImageUploadService: endpoint de upload de imagens (multipart).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiBaseUrl(@ApplicationContext context: Context): String {
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

    @Provides
    @Singleton
    fun provideOkHttpClient(prefsRepo: UserPreferencesRepository): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
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
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSyncApiService(retrofit: Retrofit): SyncApiService {
        return retrofit.create(SyncApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideImageUploadService(retrofit: Retrofit): ImageUploadService {
        return retrofit.create(ImageUploadService::class.java)
    }
}
