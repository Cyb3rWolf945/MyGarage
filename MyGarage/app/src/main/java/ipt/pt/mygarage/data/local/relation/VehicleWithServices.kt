package ipt.pt.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.VehicleEntity

/**
 * Maps the 1-to-many relationship between a [VehicleEntity] and its associated [ServiceLogEntity]s.
 */
data class VehicleWithServices(
    @Embedded val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vehicleId"
    )
    val services: List<ServiceLogEntity>
)
