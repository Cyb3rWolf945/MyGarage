package pt.ipt.dama2026.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity

/**
 * Relação 1-N: um veiculo e os seus serviços de manutenção.
 * O Room faz a junção via @Relation: vehicles.id → service_logs.vehicleId.
 * Usada pelo DAO getVehicleWithServices() para optimizar o carregamento de veículo + serviços numa só query.
 */
data class VehicleWithServices(
    @Embedded val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vehicleId"
    )
    val services: List<ServiceLogEntity>
)
