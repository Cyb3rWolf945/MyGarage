package ipt.pt.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a mechanical piece or spare part in the garage.
 */
@Entity(tableName = "pieces")
data class PieceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
