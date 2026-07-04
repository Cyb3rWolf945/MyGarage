package pt.ipt.dama2026.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity

/**
 * Relação 1-N: entre um serviço de manutenção e as suas partes/peças.
 * O Room faz a junção automaticamente via @Relation: service_logs.id → service_parts.serviceLogId.
 * Usada pelo DAO getServiceLogWithParts() para optimizar o carregamento do serviço + peças numa só query.
 */
data class ServiceLogWithParts(
    @Embedded val serviceLog: ServiceLogEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "serviceLogId"
    )
    val parts: List<PartEntity>
)
