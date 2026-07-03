package pt.ipt.dama2026.mygarage.domain.model

/**
 * Domain model for the many-to-many relationship between ServiceLog and Piece.
 */
import java.util.UUID

data class ServiceLogCrossRef(
    val serviceLogId: UUID,
    val pieceId: String,
    val quantity: Int
)
