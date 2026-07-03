package pt.ipt.dama2026.mygarage.domain.model

import java.util.UUID

/**
 * Domain model for a service log / maintenance event.
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
