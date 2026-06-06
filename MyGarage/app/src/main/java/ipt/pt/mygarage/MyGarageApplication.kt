package ipt.pt.mygarage

import android.app.Application
import ipt.pt.mygarage.data.local.db.AppDatabase
import ipt.pt.mygarage.data.repository.OfflineVehicleRepository

/**
 * Custom application class providing access to database and repository instances.
 */
class MyGarageApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { OfflineVehicleRepository(database.vehicleDao()) }
}
