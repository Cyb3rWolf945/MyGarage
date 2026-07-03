package pt.ipt.dama2026.mygarage.domain.model

/**
 * Service log with its associated parts.
 */
data class ServiceLogWithParts(
    val serviceLog: ServiceLog,
    val parts: List<Part>
)
