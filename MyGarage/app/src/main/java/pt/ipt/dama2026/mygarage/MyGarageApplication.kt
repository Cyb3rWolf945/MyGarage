package pt.ipt.dama2026.mygarage

import android.app.Application
import android.content.pm.PackageManager
import pt.ipt.dama2026.mygarage.data.location.AndroidLocationManager
import pt.ipt.dama2026.mygarage.data.local.db.AppDatabase
import pt.ipt.dama2026.mygarage.data.network.LicensePlateNetworkService
import pt.ipt.dama2026.mygarage.data.repository.OfflineVehicleRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.storage.LocalImageStorageManager
import pt.ipt.dama2026.mygarage.domain.location.LocationManager
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiService
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager

/**
 * Custom application class providing singleton access to
 * database, repository, and storage dependencies.
 *
 * This manual DI approach avoids framework overhead while
 * staying compatible with a future Hilt / Koin migration.
 */
class MyGarageApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { OfflineVehicleRepository(database.vehicleDao(), UserPreferencesRepository(this)) }

    /** Singleton [ImageStorageManager] wired to the local filesystem implementation. */
    val imageStorageManager: ImageStorageManager by lazy {
        LocalImageStorageManager(applicationContext)
    }

    /**
     * Platform-level [LocationManager] backed by FusedLocationProviderClient.
     */
    val locationManager: LocationManager by lazy {
        AndroidLocationManager(applicationContext)
    }

    /**
     * License plate API service for Portuguese vehicle lookups via SOAP.
     * Note: Requires MATRICULA_USERNAME in local.properties (via Gradle Secrets Plugin).
     */
    val licensePlateApiService: LicensePlateApiService by lazy {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val username = appInfo.metaData?.getString("MATRICULA_USERNAME") ?: ""
        LicensePlateNetworkService(username)
    }
}
