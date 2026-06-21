package ipt.pt.mygarage.presentation.garage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.MyGarageApplication
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import ipt.pt.mygarage.domain.engine.EngineCapacityHelper
import ipt.pt.mygarage.domain.repository.ImageStorageManager
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the main garage screen.
 * Combines vehicle management (Room) with user preferences (DataStore).
 */
class GarageViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val repository: VehicleRepository = (application as MyGarageApplication).repository
    private val imageStorageManager: ImageStorageManager =
        (application as MyGarageApplication).imageStorageManager

    // ── User Preferences (garage name) ────────────────────────────────────
    private val _uiState = MutableStateFlow(GarageUiState())
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    // ── Vehicle management ────────────────────────────────────────────────

    // Expose vehicles from local Room persistence as a read-only StateFlow
    val vehiclesState: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    // Long-press options menu state
    private val _selectedVehicleForOptions = MutableStateFlow<VehicleEntity?>(null)
    val selectedVehicleForOptions: StateFlow<VehicleEntity?> = _selectedVehicleForOptions.asStateFlow()

    // Edit dialog state (for both Add and Edit flows)
    private val _vehicleToEdit = MutableStateFlow<VehicleEntity?>(null)
    val vehicleToEdit: StateFlow<VehicleEntity?> = _vehicleToEdit.asStateFlow()

    // Delete confirmation state
    private val _vehicleToDelete = MutableStateFlow<VehicleEntity?>(null)
    val vehicleToDelete: StateFlow<VehicleEntity?> = _vehicleToDelete.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    garageName = preferences.garageName
                )
            }
        }
    }

    // ── Long-Press Options Menu Intents ─────────────────────────────────────

    fun onVehicleLongPressed(vehicle: VehicleEntity) {
        _selectedVehicleForOptions.value = vehicle
    }

    fun onDismissOptionsMenu() {
        _selectedVehicleForOptions.value = null
    }

    fun onSelectEdit(vehicle: VehicleEntity) {
        _selectedVehicleForOptions.value = null
        _vehicleToEdit.value = vehicle
        _uiState.update { it.copy(existingImageFileName = vehicle.localImageFileNames.firstOrNull()) }
    }

    fun onSelectDelete(vehicle: VehicleEntity) {
        _selectedVehicleForOptions.value = null
        showDeleteDialog(vehicle)
    }

    fun onDismissEditDialog() {
        _vehicleToEdit.value = null
        clearImageSelection()
    }

    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    private fun clearImageSelection() {
        _uiState.update { it.copy(selectedImageUri = null, existingImageFileName = null) }
    }

    /**
     * Saves the currently selected image (if any) to internal storage
     * and returns the resulting file name, or null.
     */
    private suspend fun saveSelectedImage(): String? {
        val uri = _uiState.value.selectedImageUri ?: return null
        return imageStorageManager.saveImage(uri)
    }

    fun confirmEdit(vehicle: VehicleEntity) {
        viewModelScope.launch {
            val fileName = saveSelectedImage()
            val updated = if (fileName != null) {
                val updatedImages = vehicle.localImageFileNames.toMutableList().apply { add(0, fileName) }
                vehicle.copy(localImageFileNames = updatedImages)
            } else {
                vehicle
            }
            repository.updateVehicle(updated)
            clearImageSelection()
        }
    }

    // ── Delete Confirmation Intents ─────────────────────────────────────────

    fun showDeleteDialog(vehicle: VehicleEntity) {
        _vehicleToDelete.value = vehicle
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirmation.value = false
        _vehicleToDelete.value = null
    }

    fun confirmDelete() {
        val vehicle = _vehicleToDelete.value ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteVehicle(vehicle)
                }
            } finally {
                _showDeleteConfirmation.value = false
                _vehicleToDelete.value = null
            }
        }
    }

    /** Removes the error for the given field so it disappears as the user types. */
    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

    /**
     * Validates all mandatory fields of the vehicle entity.
     * Returns true if all mandatory fields are present, false otherwise.
     * On failure, populates [_formErrors] with field → error resource mappings.
     */
    private fun validateVehicle(vehicle: VehicleEntity): Boolean {
        val errors = mutableMapOf<String, Int>()
        if (vehicle.name.isBlank()) errors["name"] = R.string.error_field_required
        if (vehicle.plate.isBlank()) errors["plate"] = R.string.error_field_required
        if (vehicle.year.isBlank()) errors["year"] = R.string.error_field_required
        if (vehicle.mileage.isBlank()) errors["mileage"] = R.string.error_field_required
        if (vehicle.owner.isBlank()) errors["owner"] = R.string.error_field_required
        if (vehicle.fuelType.isBlank()) errors["fuelType"] = R.string.error_field_required
        if (vehicle.engineCapacity.isBlank()) errors["engineCapacity"] = R.string.error_field_required
        _formErrors.value = errors
        return errors.isEmpty()
    }

    fun insertVehicle(vehicle: VehicleEntity) {
        if (!validateVehicle(vehicle)) return
        viewModelScope.launch {
            repository.insertVehicle(vehicle)
            clearImageSelection()
        }
    }

    fun openAddDialogWithData(
        plate: String,
        name: String,
        year: String,
        fuelType: String,
        engineCapacity: String
    ) {
        val roundedEngineCapacity = EngineCapacityHelper.roundToNearestOption(engineCapacity)
        val newVehicle = VehicleEntity(
            id = java.util.UUID.randomUUID().toString(),
            plate = plate,
            name = name,
            year = year,
            mileage = "",
            inspectionDate = null,
            oilType = "",
            owner = "",
            seatCount = "",
            doorCount = "",
            fuelType = fuelType,
            engineCapacity = roundedEngineCapacity,
            iucValue = "",
            mileageToNextService = "",
            locationAddress = "",
            latitude = null,
            longitude = null,
            localImageFileNames = emptyList(),
            remoteImageUrl = ""
        )
        _vehicleToEdit.value = newVehicle
        clearImageSelection()
    }
}
