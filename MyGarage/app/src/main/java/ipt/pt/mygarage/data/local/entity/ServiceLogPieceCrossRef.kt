package ipt.pt.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

/**
 * Junction table mapping the many-to-many relationship between [ServiceLogEntity] and [PieceEntity],
 * containing the quantity of parts used during the service.
 */
@Entity(
    tableName = "service_log_pieces",
    primaryKeys = ["serviceLogId", "pieceId"],
    foreignKeys = [
        ForeignKey(
            entity = ServiceLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceLogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PieceEntity::class,
            parentColumns = ["id"],
            childColumns = ["pieceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["serviceLogId"]),
        Index(value = ["pieceId"])
    ]
)
data class ServiceLogPieceCrossRef(
    val serviceLogId: UUID,
    val pieceId: String,
    val quantity: Int
)
