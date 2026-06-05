package ipt.pt.mygarage.data.local.converter

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Type converters for Room to handle conversion between [UUID] and [String] representation in SQLite.
 */
class Converters {
    @TypeConverter
    fun fromString(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }
}
