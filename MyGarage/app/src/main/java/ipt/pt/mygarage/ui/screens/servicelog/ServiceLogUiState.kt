package ipt.pt.mygarage.ui.screens.servicelog

import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity

/**
 * UI state for the unified Service Log dialog.
 *
 * – [dialogMode] controls which mode (if any) the dialog is in.
 * – [selectedLog] is the log being viewed, edited, or null for ADD.
 * – [selectedLogParts] carries the parts of the selected log.
 * – [formErrors] maps field names (e.g. "description", "mileage") to
 *   string resource IDs for inline error messages.
 */
data class ServiceLogUiState(
    val dialogMode: ServiceDialogMode = ServiceDialogMode.HIDDEN,
    val selectedLog: ServiceLogEntity? = null,
    val selectedLogParts: List<PartEntity> = emptyList(),
    val formErrors: Map<String, Int> = emptyMap()
)
