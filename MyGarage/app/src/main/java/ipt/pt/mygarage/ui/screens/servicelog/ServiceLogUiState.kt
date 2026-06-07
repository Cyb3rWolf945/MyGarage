package ipt.pt.mygarage.ui.screens.servicelog

/**
 * UI state for the Service Log form, holding field-level validation errors.
 * The key is the field name (e.g. "description", "mileage") and the value
 * is a string resource ID for the error message.
 */
data class ServiceLogUiState(
    val editingLogId: String? = null,
    val formErrors: Map<String, Int> = emptyMap()
)
