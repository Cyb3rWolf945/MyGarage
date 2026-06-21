package ipt.pt.mygarage

import android.app.Application
import android.content.pm.PackageManager
import ipt.pt.mygarage.data.location.AndroidLocationManager
import ipt.pt.mygarage.data.local.db.AppDatabase
import ipt.pt.mygarage.data.network.LicensePlateNetworkService
import ipt.pt.mygarage.data.repository.OfflineVehicleRepository
import ipt.pt.mygarage.data.storage.LocalImageStorageManager
import ipt.pt.mygarage.domain.location.LocationManager
import ipt.pt.mygarage.domain.licenseplates.LicensePlateApiService
import ipt.pt.mygarage.domain.repository.ImageStorageManager

/**
 * Custom application class providing singleton access to
 * database, repository, and storage dependencies.
 *
 * This manual DI approach avoids framework overhead while
 * staying compatible with a future Hilt / Koin migration.
 */
class MyGarageApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { OfflineVehicleRepository(database.vehicleDao()) }

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
