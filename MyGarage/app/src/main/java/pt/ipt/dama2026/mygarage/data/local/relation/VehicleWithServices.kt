package pt.ipt.dama2026.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity

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
