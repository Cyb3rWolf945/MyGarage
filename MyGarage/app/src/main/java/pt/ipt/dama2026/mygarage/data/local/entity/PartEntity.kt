package pt.ipt.dama2026.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Peça usada num serviço de manutenção. Tabela "service_parts".
 * Data class para ter copy() disponível nos updates parciais do repositório.
 * Chave estrangeira para ServiceLogEntity com CASCADE: ao apagar o serviço, ou seja,
 * as peças associadas são removidas automaticamente.
 * O índice em serviceLogId acelera queries por serviço.
 */
@Entity(
    tableName = "service_parts",
    foreignKeys = [
        ForeignKey(
            entity = ServiceLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceLogId"])]
)
data class PartEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val serviceLogId: String,
    val name: String,
    val quantity: Int,
    val reference: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
