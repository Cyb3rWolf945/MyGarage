package pt.ipt.dama2026.mygarage.presentation.service

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.mapper.toDomain
import pt.ipt.dama2026.mygarage.data.mapper.toEntity
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.locale.DistanceFormatter
import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.Piece
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogCrossRef
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import pt.ipt.dama2026.mygarage.presentation.locale.LocaleManager
import pt.ipt.dama2026.mygarage.ui.screens.servicelog.ServiceDialogMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the service screen. Manages service logs, parts,
 * vehicle selection, and CRUD operations for service records.
 */
@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val repository: VehicleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val application: Application
) : ViewModel() {

    private val _resolvedDistanceUnit = MutableStateFlow("KILOMETERS")
    val resolvedDistanceUnit: StateFlow<String> = _resolvedDistanceUnit.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                _resolvedDistanceUnit.value = LocaleManager.resolveDistanceUnit(
                    prefs.distanceUnit, application
                )
            }
        }
    }

    val vehicles: StateFlow<List<Vehicle>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pieces: StateFlow<List<Piece>> = repository.getAllPieces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    private val _selectedVehicleWithServices = MutableStateFlow<VehicleWithServices?>(null)
    val selectedVehicleWithServices: StateFlow<VehicleWithServices?> = _selectedVehicleWithServices.asStateFlow()

    private val _temporaryParts = MutableStateFlow<List<Part>>(emptyList())
    val temporaryParts: StateFlow<List<Part>> = _temporaryParts.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    private val _selectedLogForOptions = MutableStateFlow<ServiceLog?>(null)
    val selectedLogForOptions: StateFlow<ServiceLog?> = _selectedLogForOptions.asStateFlow()

    private val _editingLogId = MutableStateFlow<String?>(null)
    val editingLogId: StateFlow<String?> = _editingLogId.asStateFlow()

    private val _serviceDate = MutableStateFlow("")
    val serviceDate: StateFlow<String> = _serviceDate.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _mileage = MutableStateFlow("")
    val mileage: StateFlow<String> = _mileage.asStateFlow()

    private val _selectedType = MutableStateFlow("regular")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _logToDelete = MutableStateFlow<ServiceLog?>(null)
    val logToDelete: StateFlow<ServiceLog?> = _logToDelete.asStateFlow()

    private val _dialogMode = MutableStateFlow(ServiceDialogMode.HIDDEN)
    val dialogMode: StateFlow<ServiceDialogMode> = _dialogMode.asStateFlow()

    private val _selectedLog = MutableStateFlow<ServiceLog?>(null)
    val selectedLog: StateFlow<ServiceLog?> = _selectedLog.asStateFlow()

    private val _selectedLogParts = MutableStateFlow<List<Part>>(emptyList())
    val selectedLogParts: StateFlow<List<Part>> = _selectedLogParts.asStateFlow()

    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

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
        if (type == "Inspection" && _mileage.value.isBlank()) {
            prefillCurrentMileage()
        }
    }

    /**
     * Validates service log fields. Returns true if valid.
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
     * Selects a vehicle and fetches its service history.
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

    fun insertServiceLog(serviceLog: ServiceLog) {
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
     * Inserts service log with pieces (for revision type).
     */
    fun insertServiceLogWithPieces(
        serviceLog: ServiceLog,
        piecesUsed: List<ServiceLogCrossRef>
    ) {
        viewModelScope.launch {
            repository.insertServiceLogWithPieces(serviceLog, piecesUsed)
        }
    }

    fun addTemporaryPart(name: String, quantity: Int, reference: String? = null) {
        if (name.isBlank() || quantity <= 0) return
        _temporaryParts.update { current ->
            current + Part(
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
     * Inserts service log and its temporary parts.
     */
    fun insertServiceLogWithParts(serviceLog: ServiceLog) {
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
     * Extracts canonical km from a vehicle's display mileage string.
     * Falls back to mileageKm field if display string is not parseable.
     */
    private fun resolveCanonicalKm(vehicle: Vehicle): Double {
        if (vehicle.mileageKm > 0.0) return vehicle.mileageKm
        if (vehicle.mileage.isBlank()) return 0.0
        val parsed = DistanceFormatter.parseUserInput(vehicle.mileage)
        if (parsed <= 0.0) return 0.0
        val isMiles = vehicle.mileage.contains("mi", ignoreCase = true) ||
                      vehicle.mileage.contains("Miles", ignoreCase = true)
        return if (isMiles) DistanceFormatter.forStorage(parsed, "MILES") else parsed
    }

    /**
     * Saves service log (add or edit). Validates fields, converts units,
     * enforces mileage not lower than vehicle, updates vehicle mileage.
     */
    fun onSaveServiceLog() {
        val mode = _dialogMode.value
        if (mode != ServiceDialogMode.ADD && mode != ServiceDialogMode.EDIT) return

        val desc = _description.value
        val mileage = _mileage.value
        val vehicleId = _selectedVehicleId.value
        val date = _serviceDate.value
        val type = _selectedType.value
        val resolvedUnit = _resolvedDistanceUnit.value
        val unitLabel = LocaleManager.unitLabel(resolvedUnit)

        if (!validateServiceLogFields(
                description = desc,
                mileage = mileage,
                selectedVehicleId = vehicleId
            )) return

        val inputKm = DistanceFormatter.forStorage(
            DistanceFormatter.parseUserInput(mileage), resolvedUnit
        )

        var vehicleCurrentMileageKm = _selectedVehicleWithServices.value?.vehicle?.let { resolveCanonicalKm(it) } ?: 0.0
        if (vehicleCurrentMileageKm > 0.0 && inputKm < vehicleCurrentMileageKm) {
            _formErrors.update { it + ("mileage" to R.string.error_mileage_lower_than_current) }
            return
        }

        viewModelScope.launch {
            val displayMileage = DistanceFormatter.formatDisplay(inputKm, resolvedUnit)

            if (mode == ServiceDialogMode.EDIT) {
                val editingId = _selectedLog.value?.id ?: return@launch
                val updatedLog = ServiceLog(
                    id = editingId,
                    vehicleId = vehicleId!!,
                    date = date,
                    description = desc,
                    mileage = displayMileage,
                    mileageKm = inputKm,
                    type = type
                )
                val partsToSave = _temporaryParts.value.map { part ->
                    part.copy(serviceLogId = editingId.toString())
                }
                repository.updateServiceLogWithParts(updatedLog, partsToSave)
            } else {
                val newLog = ServiceLog(
                    id = UUID.randomUUID(),
                    vehicleId = vehicleId!!,
                    date = date,
                    description = desc,
                    mileage = displayMileage,
                    mileageKm = inputKm,
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

            // Update vehicle mileage + next-service after any save (add or edit)
            val currentVehicle = _selectedVehicleWithServices.value?.vehicle
            if (currentVehicle != null) {
                val delta = (inputKm - vehicleCurrentMileageKm).toInt()
                if (delta > 0) {
                    userPreferencesRepository.incrementUserMileage(delta)
                }

                val updatedVehicle = repository.getVehicleById(currentVehicle.id)
                if (updatedVehicle != null) {
                    repository.updateVehicle(
                        updatedVehicle.copy(
                            mileage = displayMileage,
                            mileageKm = inputKm
                        )
                    )
                }
            }

            clearFormState()
            SyncWorker.enqueueOneTimeSync(application)
        }
    }

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

    fun onLogLongPressed(serviceLog: ServiceLog) {
        _selectedLogForOptions.value = serviceLog
    }

    fun onDismissOptionsMenu() {
        _selectedLogForOptions.value = null
    }

    fun onSelectEdit(serviceLog: ServiceLog) {
        _selectedLogForOptions.value = null
        _selectedLog.value = serviceLog
        _editingLogId.value = serviceLog.id.toString()
        _dialogMode.value = ServiceDialogMode.EDIT

        _serviceDate.value = serviceLog.date
        _description.value = serviceLog.description
        _mileage.value = if (serviceLog.mileageKm > 0.0) {
            val resolvedUnit = _resolvedDistanceUnit.value
            DistanceFormatter.forDisplay(serviceLog.mileageKm, resolvedUnit).toLong().toString()
        } else serviceLog.mileage.replace(",", "")
        _selectedType.value = serviceLog.type
        _formErrors.value = emptyMap()

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
     * Opens dialog in ADD mode with pre-filled mileage.
     */
    fun onAddFabClicked() {
        clearFormState()
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        _serviceDate.value = today
        prefillCurrentMileage()
        _dialogMode.value = ServiceDialogMode.ADD
    }

    private fun prefillCurrentMileage() {
        val vehicle = _selectedVehicleWithServices.value?.vehicle ?: return
        val canonicalKm = resolveCanonicalKm(vehicle)
        if (canonicalKm > 0.0) {
            val resolvedUnit = _resolvedDistanceUnit.value
            val displayValue = DistanceFormatter.forDisplay(canonicalKm, resolvedUnit)
            _mileage.value = displayValue.toLong().toString()
        }
    }

    /**
     * Opens dialog in VIEW (read-only) mode for a service log.
     */
    fun onLogClicked(serviceLog: ServiceLog) {
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

    fun onDismissDialog() {
        clearFormState()
    }

    fun onSelectDelete(serviceLog: ServiceLog) {
        _selectedLogForOptions.value = null
        _logToDelete.value = serviceLog
    }

    fun onDismissDeleteDialog() {
        _logToDelete.value = null
    }

    /**
     * Deletes the selected service log. Clears UI state references before DB
     * operation to prevent rendering deleted entity during Flow re-emission.
     */
    fun onConfirmDeleteLog() {
        val log = _logToDelete.value ?: return

        viewModelScope.launch {
            try {
                repository.deleteServiceLog(log)
            } catch (_: Exception) {
                return@launch
            }

            _logToDelete.value = null
            _selectedLogForOptions.value = null
            if (_selectedLog.value?.id == log.id) {
                clearFormState()
            }
            SyncWorker.enqueueOneTimeSync(application)
        }
    }
}
