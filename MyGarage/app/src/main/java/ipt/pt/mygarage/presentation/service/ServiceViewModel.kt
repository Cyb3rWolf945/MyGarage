package ipt.pt.mygarage.presentation.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.domain.repository.VehicleRepository
import ipt.pt.mygarage.ui.screens.servicelog.ServiceDialogMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing service logs and parts lists.
 */
class ServiceViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    // Expose all vehicles for selection on the service page
    val vehicles: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose available piece items (parts catalog)
    val pieces: StateFlow<List<PieceEntity>> = repository.getAllPieces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    private val _selectedVehicleWithServices = MutableStateFlow<VehicleWithServices?>(null)
    val selectedVehicleWithServices: StateFlow<VehicleWithServices?> = _selectedVehicleWithServices.asStateFlow()

    // Temporary parts list while composing a new service log (revision type)
    private val _temporaryParts = MutableStateFlow<List<PartEntity>>(emptyList())
    val temporaryParts: StateFlow<List<PartEntity>> = _temporaryParts.asStateFlow()

    // Form validation errors for the service log form
    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    // ── Long-Press Options Menu State ──────────────────────────────────────
    private val _selectedLogForOptions = MutableStateFlow<ServiceLogEntity?>(null)
    val selectedLogForOptions: StateFlow<ServiceLogEntity?> = _selectedLogForOptions.asStateFlow()

    // ── Edit Mode State ────────────────────────────────────────────────────
    private val _editingLogId = MutableStateFlow<String?>(null)
    val editingLogId: StateFlow<String?> = _editingLogId.asStateFlow()

    // ── Form Field State (driven by ViewModel for Add/Edit modes) ──────────
    private val _serviceDate = MutableStateFlow("")
    val serviceDate: StateFlow<String> = _serviceDate.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _mileage = MutableStateFlow("")
    val mileage: StateFlow<String> = _mileage.asStateFlow()

    private val _selectedType = MutableStateFlow("regular")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // ── Delete Confirmation State ──────────────────────────────────────────
    private val _logToDelete = MutableStateFlow<ServiceLogEntity?>(null)
    val logToDelete: StateFlow<ServiceLogEntity?> = _logToDelete.asStateFlow()

    // ── Unified Dialog Mode State ──────────────────────────────────────────
    private val _dialogMode = MutableStateFlow(ServiceDialogMode.HIDDEN)
    val dialogMode: StateFlow<ServiceDialogMode> = _dialogMode.asStateFlow()

    private val _selectedLog = MutableStateFlow<ServiceLogEntity?>(null)
    val selectedLog: StateFlow<ServiceLogEntity?> = _selectedLog.asStateFlow()

    private val _selectedLogParts = MutableStateFlow<List<PartEntity>>(emptyList())
    val selectedLogParts: StateFlow<List<PartEntity>> = _selectedLogParts.asStateFlow()

    /** Removes the error for the given field so it disappears as the user types. */
    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

    // ── Form Field Change Intents ──────────────────────────────────────────

    fun onDateChanged(date: String) {
        _serviceDate.value = date
        clearFieldError("date")
    }

    fun onDescriptionChanged(description: String) {
        _description.value = description
        clearFieldError("description")
    }

    fun onMileageChanged(mileage: String) {
        _mileage.value = mileage
        clearFieldError("mileage")
    }

    fun onTypeChanged(type: String) {
        _selectedType.value = type
    }

    /**
     * Validates mandatory service log fields.
     * Returns true if all required fields are present, false otherwise.
     */
    private fun validateServiceLogFields(
        description: String,
        mileage: String,
        selectedVehicleId: String?
    ): Boolean {
        val errors = mutableMapOf<String, Int>()
        if (selectedVehicleId.isNullOrBlank()) errors["vehicle"] = R.string.error_field_required
        if (description.isBlank()) errors["description"] = R.string.error_field_required
        if (mileage.isBlank()) errors["mileage"] = R.string.error_field_required
        _formErrors.value = errors
        return errors.isEmpty()
    }

    /**
     * Changes the current selected vehicle and retrieves its complete service history.
     */
    fun selectVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
        _temporaryParts.value = emptyList()
        viewModelScope.launch {
            repository.getVehicleWithServices(vehicleId)
                .catch { e -> e.printStackTrace() }
                .collect {
                    _selectedVehicleWithServices.value = it
                }
        }
    }

    fun insertServiceLog(serviceLog: ServiceLogEntity) {
        if (!validateServiceLogFields(
                description = serviceLog.description,
                mileage = serviceLog.mileage,
                selectedVehicleId = serviceLog.vehicleId
            )) return
        viewModelScope.launch {
            repository.insertServiceLog(serviceLog)
        }
    }

    /**
     * Inserts a service log along with pieces used (for revision type services).
     */
    fun insertServiceLogWithPieces(
        serviceLog: ServiceLogEntity,
        piecesUsed: List<ServiceLogPieceCrossRef>
    ) {
        viewModelScope.launch {
            repository.insertServiceLogWithPieces(serviceLog, piecesUsed)
        }
    }

    fun addTemporaryPart(name: String, quantity: Int, reference: String? = null) {
        if (name.isBlank() || quantity <= 0) return
        _temporaryParts.update { current ->
            current + PartEntity(
                id = UUID.randomUUID().toString(),
                serviceLogId = "",
                name = name.trim(),
                quantity = quantity,
                reference = reference?.trim()?.ifBlank { null }
            )
        }
    }

    fun removeTemporaryPart(partId: String) {
        _temporaryParts.update { current ->
            current.filter { it.id != partId }
        }
    }

    /**
     * Inserts a service log and then persists all temporary parts,
     * assigning them the newly created service log ID.
     */
    fun insertServiceLogWithParts(serviceLog: ServiceLogEntity) {
        if (!validateServiceLogFields(
                description = serviceLog.description,
                mileage = serviceLog.mileage,
                selectedVehicleId = serviceLog.vehicleId
            )) return
        viewModelScope.launch {
            repository.insertServiceLog(serviceLog)
            val partsToInsert = _temporaryParts.value.map { part ->
                part.copy(serviceLogId = serviceLog.id.toString())
            }
            partsToInsert.forEach { repository.insertPart(it) }
            _temporaryParts.value = emptyList()
        }
    }

    /**
     * Unified save intent for both Add and Edit flows.
     * Validates mandatory fields first; if valid, either inserts a new
     * service log or updates an existing one.
     * Only executes when [dialogMode] is ADD or EDIT.
     */
    fun onSaveServiceLog() {
        val mode = _dialogMode.value
        if (mode != ServiceDialogMode.ADD && mode != ServiceDialogMode.EDIT) return

        val desc = _description.value
        val mileage = _mileage.value
        val vehicleId = _selectedVehicleId.value
        val date = _serviceDate.value
        val type = _selectedType.value

        if (!validateServiceLogFields(
                description = desc,
                mileage = mileage,
                selectedVehicleId = vehicleId
            )) return

        viewModelScope.launch {
            if (mode == ServiceDialogMode.EDIT) {
                // ── EDIT MODE: update existing log with current parts ──────
                val editingId = _selectedLog.value?.id ?: return@launch
                val updatedLog = ServiceLogEntity(
                    id = editingId,
                    vehicleId = vehicleId!!,
                    date = date,
                    description = desc,
                    mileage = if (mileage.contains("mi")) mileage else "$mileage mi",
                    type = type
                )
                val partsToSave = _temporaryParts.value.map { part ->
                    part.copy(serviceLogId = editingId.toString())
                }
                repository.updateServiceLogWithParts(updatedLog, partsToSave)
            } else {
                // ── ADD MODE: insert new log with current parts ────────────
                val newLog = ServiceLogEntity(
                    id = UUID.randomUUID(),
                    vehicleId = vehicleId!!,
                    date = date,
                    description = desc,
                    mileage = if (mileage.contains("mi")) mileage else "$mileage mi",
                    type = type
                )
                if (type == "revision") {
                    repository.insertServiceLog(newLog)
                    val partsToInsert = _temporaryParts.value.map { part ->
                        part.copy(serviceLogId = newLog.id.toString())
                    }
                    partsToInsert.forEach { repository.insertPart(it) }
                } else {
                    repository.insertServiceLog(newLog)
                }
            }

            // Clear state and close the form upon success
            clearFormState()
        }
    }

    /** Resets form and dialog state back to defaults after a successful save or dismiss. */
    private fun clearFormState() {
        _dialogMode.value = ServiceDialogMode.HIDDEN
        _selectedLog.value = null
        _selectedLogParts.value = emptyList()
        _editingLogId.value = null
        _serviceDate.value = ""
        _description.value = ""
        _mileage.value = ""
        _selectedType.value = "regular"
        _temporaryParts.value = emptyList()
        _formErrors.value = emptyMap()
    }

    // ── Long-Press Options Menu Intents ────────────────────────────────────

    fun onLogLongPressed(serviceLog: ServiceLogEntity) {
        _selectedLogForOptions.value = serviceLog
    }

    fun onDismissOptionsMenu() {
        _selectedLogForOptions.value = null
    }

    fun onSelectEdit(serviceLog: ServiceLogEntity) {
        _selectedLogForOptions.value = null
        _selectedLog.value = serviceLog
        _editingLogId.value = serviceLog.id.toString()
        _dialogMode.value = ServiceDialogMode.EDIT

        // Populate form fields with the selected log's existing data
        _serviceDate.value = serviceLog.date
        _description.value = serviceLog.description
        _mileage.value = serviceLog.mileage
        _selectedType.value = serviceLog.type
        _formErrors.value = emptyMap()

        // Load existing parts for this service log
        viewModelScope.launch {
            repository.getServiceLogWithParts(serviceLog.id.toString())
                .catch { e -> e.printStackTrace() }
                .collect { serviceWithParts ->
                    _selectedLogParts.value = serviceWithParts.parts
                    _temporaryParts.value = serviceWithParts.parts
                }
        }
    }

    /**
     * FAB tapped — opens the dialog in ADD mode with a clean form slate.
     */
    fun onAddFabClicked() {
        clearFormState()
        _dialogMode.value = ServiceDialogMode.ADD
    }

    /**
     * A service log card was tapped in the timeline — opens the dialog
     * in VIEW (read-only) mode, loading the log and its parts.
     */
    fun onLogClicked(serviceLog: ServiceLogEntity) {
        _selectedLog.value = serviceLog
        _dialogMode.value = ServiceDialogMode.VIEW
        _selectedLogParts.value = emptyList()

        viewModelScope.launch {
            repository.getServiceLogWithParts(serviceLog.id.toString())
                .catch { e -> e.printStackTrace() }
                .collect { serviceWithParts ->
                    _selectedLogParts.value = serviceWithParts.parts
                }
        }
    }

    /**
     * User dismissed the unified dialog — reset everything to HIDDEN.
     */
    fun onDismissDialog() {
        clearFormState()
    }

    fun onSelectDelete(serviceLog: ServiceLogEntity) {
        _selectedLogForOptions.value = null
        _logToDelete.value = serviceLog
    }

    fun onDismissDeleteDialog() {
        _logToDelete.value = null
    }

    fun onConfirmDeleteLog() {
        val log = _logToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteServiceLog(log)
            _logToDelete.value = null
        }
    }

    companion object {
        fun factory(repository: VehicleRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ServiceViewModel(repository) as T
                }
            }
    }
}
