package pt.ipt.dama2026.mygarage.domain.model

/**
 * Peça usada num serviço de manutenção.
 *
 * Modelo de domínio
 * A conversão de/para PartEntity é feita pelos mappers em data/mapper/.
 */
data class Part(
    val id: String,
    val serviceLogId: String,
    val name: String,
    val quantity: Int,
    val reference: String? = null
)
