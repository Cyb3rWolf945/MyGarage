package pt.ipt.dama2026.mygarage.domain.model

/** Serviço + peças usadas. Usado para mostrar o detalhe de um serviço na UI. */
data class ServiceLogWithParts(
    val serviceLog: ServiceLog,
    val parts: List<Part>
)
