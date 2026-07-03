package pt.ipt.dama2026.mygarage.domain.model

/**
 * Vehicle with its associated service logs.
 */
data class VehicleWithServices(
    val vehicle: Vehicle,
    val services: List<ServiceLog>
)
