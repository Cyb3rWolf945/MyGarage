package ipt.pt.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a vehicle in the local database.
 * Attributes match [ipt.pt.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState]
 * to ensure Unidirectional Data Flow (UDF) is clean and direct.
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plate: String,
    val modelName: String,
    val year: String,
    val mileage: String,
    val inspectionDate: String,
    val oilType: String,
    val owner: String,
    val seatCount: String,
    val doorCount: String,
    val fuelType: String,
    val engineCapacity: String,
    val iucValue: String,
    val mileageToNextService: String,
    val locationAddress: String
)
