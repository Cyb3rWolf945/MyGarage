package ipt.pt.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a service log/maintenance event for a vehicle.
 * Configured with a foreign key referencing [VehicleEntity] with cascade deletion.
 */
@Entity(
    tableName = "service_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class ServiceLogEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val vehicleId: String,
    val date: String,
    val description: String,
    val mileage: String,
    val type: String // e.g. "revision", "Inspection", "regular"
)
