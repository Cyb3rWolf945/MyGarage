package pt.ipt.dama2026.mygarage.domain.model

/**
 * Domain model for a part used during a service.
 */
data class Part(
    val id: String,
    val serviceLogId: String,
    val name: String,
    val quantity: Int,
    val reference: String? = null
)
