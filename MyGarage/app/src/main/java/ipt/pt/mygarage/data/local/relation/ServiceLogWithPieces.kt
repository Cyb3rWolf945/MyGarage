package ipt.pt.mygarage.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef

/**
 * Details of a piece used including the piece metadata and its usage quantity.
 */
data class PieceUseWithDetail(
    @Embedded val crossRef: ServiceLogPieceCrossRef,
    @Relation(
        parentColumn = "pieceId",
        entityColumn = "id"
    )
    val piece: PieceEntity
)

/**
 * Maps a [ServiceLogEntity] to the list of pieces used, containing their details and quantities.
 */
data class ServiceLogWithPieces(
    @Embedded val serviceLog: ServiceLogEntity,
    @Relation(
        entity = ServiceLogPieceCrossRef::class,
        parentColumn = "id",
        entityColumn = "serviceLogId"
    )
    val pieces: List<PieceUseWithDetail>
)
