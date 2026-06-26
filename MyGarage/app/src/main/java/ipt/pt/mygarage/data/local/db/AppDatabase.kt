package ipt.pt.mygarage.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ipt.pt.mygarage.data.local.converter.Converters
import ipt.pt.mygarage.data.local.dao.VehicleDao
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity

/**
 * Main application database.
 */
@Database(
    entities = [
        VehicleEntity::class,
        ServiceLogEntity::class,
        PartEntity::class,
        PieceEntity::class,
        ServiceLogPieceCrossRef::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        /**
         * Migration v4 → v5: Adds canonical mileageKm (REAL / Double) columns
         * to both vehicles and service_logs tables.
         */
        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN mileageKm REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN mileageKm REAL NOT NULL DEFAULT 0.0")
            }
        }

        /**
         * Migration v5 → v6: Upgrades single image support to multiple images.
         *
         * Renames localImageFileName to localImageFileNames and converts existing
         * single image (if present) into a JSON array for backward compatibility.
         * For example: "photo123.jpg" becomes "[\"photo123.jpg\"]"
         */
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

        /**
         * Migration v6 → v7: Adds updatedAt (INTEGER, epoch millis) to all
         * entity tables for cloud sync delta detection.
         */
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_parts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_log_pieces ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migration v7 → v8: Adds isDeleted (INTEGER, boolean) to all
         * entity tables for soft delete support.
         */
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_parts ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE service_log_pieces ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_garage_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                .also { Instance = it }
            }
        }
    }
}
