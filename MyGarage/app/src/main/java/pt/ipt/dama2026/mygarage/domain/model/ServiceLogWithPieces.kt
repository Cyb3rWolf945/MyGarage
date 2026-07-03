package pt.ipt.dama2026.mygarage.domain.model

/**
 * Piece usage detail with its metadata and quantity.
 */
data class PieceUseWithDetail(
    val crossRef: ServiceLogCrossRef,
    val piece: Piece
)

/**
 * Service log with its associated pieces and their usage quantities.
 */
data class ServiceLogWithPieces(
    val serviceLog: ServiceLog,
    val pieceUses: List<PieceUseWithDetail>
)
