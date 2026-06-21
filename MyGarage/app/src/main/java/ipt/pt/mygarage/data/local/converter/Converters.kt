package ipt.pt.mygarage.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray
import java.util.UUID

/**
 * Type converters for Room to handle conversion between complex types and SQLite string representation.
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

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return if (value.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val jsonArray = JSONArray(value)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun stringListToString(value: List<String>?): String? {
        return if (value.isNullOrEmpty()) {
            null
        } else {
            JSONArray(value).toString()
        }
    }
}
