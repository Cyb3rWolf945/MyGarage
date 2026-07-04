package pt.ipt.dama2026.mygarage.domain.model

import java.util.UUID

/**
 *
 * Modelo de domínio
 * O type indica o tipo de serviço: "Inspection", "revision", "regular".
 */
data class ServiceLog(
    val id: UUID = UUID.randomUUID(),
    val vehicleId: String,
    val date: String,
    val description: String,
    val mileage: String,
    val mileageKm: Double = 0.0,
    val type: String // "revision", "Inspection", "regular"
)
