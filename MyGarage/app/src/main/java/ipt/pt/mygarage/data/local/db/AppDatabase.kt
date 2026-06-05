package ipt.pt.mygarage.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ipt.pt.mygarage.data.local.converter.Converters
import ipt.pt.mygarage.data.local.dao.VehicleDao
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
        PieceEntity::class,
        ServiceLogPieceCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_garage_database"
                )
                .addCallback(DatabaseSeederCallback())
                .build()
                .also { Instance = it }
            }
        }
    }

    private class DatabaseSeederCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            // Seed Pieces
            db.execSQL("INSERT INTO pieces (id, name, price) VALUES ('oil_filter', 'Motul Oil Filter', 15.99)")
            db.execSQL("INSERT INTO pieces (id, name, price) VALUES ('spark_plug', 'NGK Laser Platinum Spark Plug', 12.50)")
            db.execSQL("INSERT INTO pieces (id, name, price) VALUES ('brake_pads', 'Brembo Ceramic Brake Pads', 85.00)")
            db.execSQL("INSERT INTO pieces (id, name, price) VALUES ('air_filter', 'K&N High-Flow Air Filter', 45.00)")
            db.execSQL("INSERT INTO pieces (id, name, price) VALUES ('cabin_filter', 'Bosch Activated Carbon Cabin Filter', 22.00)")

            // Seed Vehicles
            db.execSQL("""
                INSERT INTO vehicles (
                    id, plate, modelName, year, mileage, inspectionDate, oilType, owner, 
                    seatCount, doorCount, fuelType, engineCapacity, iucValue, 
                    mileageToNextService, locationAddress
                ) VALUES (
                    'porsche_911', '911-GT3-RS', 'Porsche 911 GT3 RS', '2024', '12,450 mi', '15/11/2026', 
                    '0W-40 Synthetic', 'Private Owner', '4', '2', 'Petrol', '3,000 cc', '218', 
                    '8,200 mi', 'Porscheplatz 1, 70435 Stuttgart, Germany'
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO vehicles (
                    id, plate, modelName, year, mileage, inspectionDate, oilType, owner, 
                    seatCount, doorCount, fuelType, engineCapacity, iucValue, 
                    mileageToNextService, locationAddress
                ) VALUES (
                    'bmw_m4', 'BMW-M4-COMP', 'BMW M4 Competition', '2023', '8,920 mi', '02/09/2026', 
                    '5W-30 Synthetic', 'Private Owner', '4', '2', 'Petrol', '3,000 cc', '196', 
                    '6,500 mi', 'Petuelring 130, 80809 Munich, Germany'
                )
            """.trimIndent())

            // Seed Initial Service Logs
            db.execSQL("""
                INSERT INTO service_logs (id, vehicleId, date, description, mileage, type) 
                VALUES ('f1f2f3f4-e5e6-4748-898a-b9b0b1b2b3b4', 'porsche_911', '15/05/2025', 'Full Service & Oil Change - Atelier Stuttgart Service Center', '12,000 mi', 'regular')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO service_logs (id, vehicleId, date, description, mileage, type) 
                VALUES ('c1c2c3c4-d5d6-4748-898a-a9a0a1a2a3a4', 'porsche_911', '10/01/2025', 'Tire Rotation & Balance - Michelin Certified Partner', '10,200 mi', 'regular')
            """.trimIndent())

            db.execSQL("""
                INSERT INTO service_logs (id, vehicleId, date, description, mileage, type) 
                VALUES ('e1e2e3e4-f5f6-4748-898a-c9c0c1c2c3c4', 'bmw_m4', '20/04/2025', 'Break-in Service - Atelier Munich Service Center', '1,200 mi', 'regular')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO service_logs (id, vehicleId, date, description, mileage, type) 
                VALUES ('d1d2d3d4-e5e6-4748-898a-d9d0d1d2d3d4', 'bmw_m4', '01/02/2025', 'Brake Fluid Flush - BMW Certified Service', '800 mi', 'regular')
            """.trimIndent())
        }
    }
}
