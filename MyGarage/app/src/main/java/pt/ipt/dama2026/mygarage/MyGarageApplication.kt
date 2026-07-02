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
 * Custom Application class. Provides singleton access to database,
 * repository, and storage dependencies via manual DI.
 */
class MyGarageApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { OfflineVehicleRepository(database.vehicleDao(), UserPreferencesRepository(this)) }
    val imageStorageManager: ImageStorageManager by lazy {
        LocalImageStorageManager(applicationContext)
    }
    val locationManager: LocationManager by lazy {
        AndroidLocationManager(applicationContext)
    }
    val licensePlateApiService: LicensePlateApiService by lazy {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val username = appInfo.metaData?.getString("MATRICULA_USERNAME") ?: ""
        LicensePlateNetworkService(username)
    }
}
