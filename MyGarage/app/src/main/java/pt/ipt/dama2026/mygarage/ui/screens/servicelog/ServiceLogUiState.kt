package pt.ipt.dama2026.mygarage.ui.screens.servicelog

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity

/** Estado do diálogo de serviço: modo, registo selecionado, peças e erros. */
data class ServiceLogUiState(
    val dialogMode: ServiceDialogMode = ServiceDialogMode.HIDDEN,
    val selectedLog: ServiceLogEntity? = null,
    val selectedLogParts: List<PartEntity> = emptyList(),
    val formErrors: Map<String, Int> = emptyMap()
)
