package pt.ipt.dama2026.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity

/**
 * Maps the 1-to-many relationship between a [ServiceLogEntity] and its [PartEntity]s.
 */
data class ServiceLogWithParts(
    @Embedded val serviceLog: ServiceLogEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "serviceLogId"
    )
    val parts: List<PartEntity>
)
