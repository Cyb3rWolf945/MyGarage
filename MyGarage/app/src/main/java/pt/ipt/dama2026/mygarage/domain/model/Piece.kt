package pt.ipt.dama2026.mygarage.domain.model

/**
 * Domain model for a mechanical piece / spare part.
 */
data class Piece(
    val id: String,
    val name: String,
    val price: Double
)
