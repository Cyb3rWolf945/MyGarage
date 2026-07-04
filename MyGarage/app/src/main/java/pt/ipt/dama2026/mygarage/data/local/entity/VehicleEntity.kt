package pt.ipt.dama2026.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Veículo na base de dados local. Tabela "vehicles".
 * Data class para ter copy() disponível nos updates parciais do repositório.
 * Guarda dados da matrícula (plate, nome, ano, quilometragem, combustível, etc.),
 * localização GPS, imagens locais/remotas, e metadados de sync (updatedAt, isDeleted).
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plate: String,
    val name: String,
    val year: String,
    val mileage: String,
    val mileageKm: Double = 0.0,
    val inspectionDate: String? = null,
    val oilType: String? = null,
    val owner: String,
    val seatCount: String? = null,
    val doorCount: String? = null,
    val fuelType: String,
    val engineCapacity: String,
    val iucValue: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val localImageFileNames: List<String> = emptyList(),
    val remoteImageUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
