package ipt.pt.mygarage

import android.app.Application
import ipt.pt.mygarage.data.local.db.AppDatabase
import ipt.pt.mygarage.data.repository.OfflineVehicleRepository
import ipt.pt.mygarage.data.storage.LocalImageStorageManager
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
}
