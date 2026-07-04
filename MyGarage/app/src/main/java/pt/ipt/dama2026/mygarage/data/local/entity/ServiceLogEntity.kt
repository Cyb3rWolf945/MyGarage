package pt.ipt.dama2026.mygarage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Registo de serviço de manutenção de um veículo. Tabela "service_logs".
 * Data class para ter copy() disponível nos updates parciais do repositório.
 * Chave estrangeira para VehicleEntity com CASCADE: ao apagar o veículo,
 * os seus serviços são automaticamente removidos. O índice em vehicleId acelera queries por veículo.
 * O campo type categoriza o serviço (ex.: "Inspection", "revision", "regular").
 */
@Entity(
    tableName = "service_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class ServiceLogEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val vehicleId: String,
    val date: String,
    val description: String,
    val mileage: String,
    val mileageKm: Double = 0.0,
    val type: String, // e.g. "revision", "Inspection", "regular"
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
