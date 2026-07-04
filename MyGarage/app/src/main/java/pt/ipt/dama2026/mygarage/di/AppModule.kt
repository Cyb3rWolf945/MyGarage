package pt.ipt.dama2026.mygarage.di

import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pt.ipt.dama2026.mygarage.data.location.AndroidLocationManager
import pt.ipt.dama2026.mygarage.data.network.LicensePlateNetworkService
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.storage.LocalImageStorageManager
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiService
import pt.ipt.dama2026.mygarage.domain.location.LocationManager
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import javax.inject.Singleton

/**
 * Módulo Hilt.
 *
 * Providencia:
 * - UserPreferencesRepository (precisa de Context para o DataStore).
 * - ImageStorageManager → LocalImageStorageManager (armazenamento de imagens).
 * - LocationManager → AndroidLocationManager (GPS).
 * - LicensePlateApiService → LicensePlateNetworkService (lê a chave MATRICULA_USERNAME
 *   do AndroidManifest para autenticação no serviço SOAP).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideImageStorageManager(@ApplicationContext context: Context): ImageStorageManager {
        return LocalImageStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return AndroidLocationManager(context)
    }

    @Provides
    @Singleton
    fun provideLicensePlateApiService(@ApplicationContext context: Context): LicensePlateApiService {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        val username = appInfo.metaData?.getString("MATRICULA_USERNAME") ?: ""
        return LicensePlateNetworkService(username)
    }
}
