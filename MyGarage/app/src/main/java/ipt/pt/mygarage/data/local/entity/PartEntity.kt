package ipt.pt.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a part used during a specific service log.
 * Foreign key to [ServiceLogEntity] with cascade deletion.
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
    val reference: String? = null
)
