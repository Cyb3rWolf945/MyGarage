package pt.ipt.dama2026.mygarage.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray
import java.util.UUID

/**
 * Converte tipos não suportados nativamente pelo SQLite como UUID e List<String>
 * para String ao persistir e reconverte ao ler. Registada globalmente no AppDatabase
 * via @TypeConverters. Usada sempre que o Room lê ou escreve entidades com esses tipos
 * (ex.: VehicleEntity.localImageFileNames, ServiceLogEntity.id).
 * Em caso de erro de parse JSON, devolve uma lista vazia.
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
            JSONArray().toString()
        } else {
            JSONArray(value).toString()
        }
    }
}
