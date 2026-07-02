package pt.ipt.dama2026.mygarage.presentation.garage

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import pt.ipt.dama2026.mygarage.MyGarageApplication
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.repository.ImageUploadRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.engine.EngineCapacityHelper
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
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
 * ViewModel for the garage screen. Manages vehicle CRUD, image upload,
 * form validation, and long-press options.
 */
class GarageViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val repository: VehicleRepository = (application as MyGarageApplication).repository
    private val imageStorageManager: ImageStorageManager =
        (application as MyGarageApplication).imageStorageManager
    private val imageUploadRepository = ImageUploadRepository(application)

    private val _uiState = MutableStateFlow(GarageUiState())
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    val vehiclesState: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    private val _selectedVehicleForOptions = MutableStateFlow<VehicleEntity?>(null)
    val selectedVehicleForOptions: StateFlow<VehicleEntity?> = _selectedVehicleForOptions.asStateFlow()

    private val _vehicleToEdit = MutableStateFlow<VehicleEntity?>(null)
    val vehicleToEdit: StateFlow<VehicleEntity?> = _vehicleToEdit.asStateFlow()

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
     * Saves selected image to internal storage. Returns file name or null.
     */
    private suspend fun saveSelectedImage(): String? {
        val uri = _uiState.value.selectedImageUri ?: return null
        return imageStorageManager.saveImage(uri)
    }

    /**
     * Confirms edit: uploads image to S3, updates vehicle in Room, triggers sync.
     */
    fun confirmEdit(vehicle: VehicleEntity) {
        viewModelScope.launch {
            var updated = vehicle

            val imageFileName = vehicle.localImageFileNames.firstOrNull()
            if (imageFileName != null) {
                val imagePath = imageStorageManager.getImagePath(imageFileName)
                if (imagePath != null) {
                    val uri = Uri.fromFile(java.io.File(imagePath))
                    val uploadResult = imageUploadRepository.uploadImage(uri, "vehicle")
                    if (uploadResult.isSuccess) {
                        updated = updated.copy(remoteImageUrl = uploadResult.getOrNull())
                    }
                }
            }

            repository.updateVehicle(updated)
            clearImageSelection()
            SyncWorker.enqueueOneTimeSync(getApplication())
        }
    }

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
            SyncWorker.enqueueOneTimeSync(getApplication())
        }
    }

    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

    /**
     * Validates mandatory vehicle fields. Returns true if all valid.
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
            var vehicleToInsert = vehicle

            val imageFileName = vehicle.localImageFileNames.firstOrNull()
            if (imageFileName != null) {
                val imagePath = imageStorageManager.getImagePath(imageFileName)
                if (imagePath != null) {
                    val uri = Uri.fromFile(java.io.File(imagePath))
                    val uploadResult = imageUploadRepository.uploadImage(uri, "vehicle")
                    if (uploadResult.isSuccess) {
                        vehicleToInsert = vehicleToInsert.copy(remoteImageUrl = uploadResult.getOrNull())
                    }
                }
            }

            repository.insertVehicle(vehicleToInsert)
            clearImageSelection()
            SyncWorker.enqueueOneTimeSync(getApplication())
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
            remoteImageUrl = null
        )
        _vehicleToEdit.value = newVehicle
        clearImageSelection()
    }
}
