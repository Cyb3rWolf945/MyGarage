package pt.ipt.dama2026.mygarage.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.ipt.dama2026.mygarage.data.local.converter.Converters
import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity

/**
 * Room database for the app. Holds vehicles, service logs, parts, and pieces.
 */
@Database(
    entities = [
        VehicleEntity::class,
        ServiceLogEntity::class,
        PartEntity::class,
        PieceEntity::class,
        ServiceLogPieceCrossRef::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // v4→v5: add canonical mileageKm columns
        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN mileageKm REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN mileageKm REAL NOT NULL DEFAULT 0.0")
            }
        }

        // v5→v6: upgrade single image to multiple images (JSON array)
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE vehicles ADD COLUMN localImageFileNames TEXT DEFAULT NULL
                """.trimIndent())

                db.execSQL("""
                    UPDATE vehicles
                    SET localImageFileNames =
                        CASE
                            WHEN localImageFileName IS NOT NULL
                            THEN json_array(localImageFileName)
                            ELSE NULL
                        END
                """.trimIndent())

                db.execSQL("DROP TABLE IF EXISTS vehicles_backup")
            }
        }

        // v6→v7: add updatedAt for cloud sync delta detection
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_parts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_log_pieces ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        // v7→v8: add isDeleted for soft delete support
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_parts ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_log_pieces ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        // v8→v9: add canonical mileageToNextServiceKm column
        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN mileageToNextServiceKm REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_garage_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build()
                .also { Instance = it }
            }
        }
    }
}
