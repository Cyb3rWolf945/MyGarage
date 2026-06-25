package ipt.pt.mygarage.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import android.app.Application
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import ipt.pt.mygarage.domain.locale.LocaleManager
import ipt.pt.mygarage.domain.location.LocationManager
import android.net.Uri
import android.util.Log
import ipt.pt.mygarage.MyGarageApplication
import ipt.pt.mygarage.data.repository.ImageUploadRepository
import ipt.pt.mygarage.domain.location.LocationResult
import ipt.pt.mygarage.domain.repository.ImageStorageManager
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the profile screen of a specific vehicle.
 */
class VehicleProfileViewModel(
    private val repository: VehicleRepository,
    private val locationManager: LocationManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val application: Application
) : ViewModel() {

    /** Resolved distance unit (KILOMETERS or MILES) observed from DataStore + OS locale. */
    private val _resolvedDistanceUnit = MutableStateFlow("MILES")
    val resolvedDistanceUnit: StateFlow<String> = _resolvedDistanceUnit.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.distanceUnitFlow.collect { rawUnit ->
                _resolvedDistanceUnit.value = LocaleManager.resolveDistanceUnit(
                    rawUnit, application
                )
            }
        }
    }

    private val _uiState = MutableStateFlow<VehicleWithServices?>(null)
    val uiState: StateFlow<VehicleWithServices?> = _uiState.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    // Delete confirmation state
    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    // One-time navigation event: emitted after successful deletion, pop backstack
    private val _deleteCompleted = MutableStateFlow(false)
    val deleteCompleted: StateFlow<Boolean> = _deleteCompleted.asStateFlow()

    // Carousel state
    private val _isCarouselVisible = MutableStateFlow(false)
    val isCarouselVisible: StateFlow<Boolean> = _isCarouselVisible.asStateFlow()

    private val _carouselStartIndex = MutableStateFlow(0)
    val carouselStartIndex: StateFlow<Int> = _carouselStartIndex.asStateFlow()

    fun showDeleteDialog() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirmation.value = false
    }

    fun openCarousel(startIndex: Int = 0) {
        _carouselStartIndex.value = startIndex
        _isCarouselVisible.value = true
    }

    fun closeCarousel() {
        _isCarouselVisible.value = false
    }

    fun confirmDelete() {
        val vehicleWithServices = _uiState.value ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteVehicle(vehicleWithServices.vehicle)
                }
            } finally {
                _showDeleteConfirmation.value = false
                _deleteCompleted.value = true
            }
        }
    }

    /** Consume the one-shot navigation event after handling it. */
    fun onDeleteCompletedHandled() {
        _deleteCompleted.value = false
    }

    /**
     * Starts collecting the vehicle and its services from Room.
     */
    fun loadVehicle(vehicleId: String) {
        _formErrors.value = emptyMap()
        viewModelScope.launch {
            repository.getVehicleWithServices(vehicleId)
                .catch { e -> e.printStackTrace() }
                .collect { vehicleWithServices ->
                    _uiState.value = vehicleWithServices
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

    fun updateVehicle(vehicle: VehicleEntity) {
        if (!validateVehicle(vehicle)) return
        viewModelScope.launch {
            var updated = vehicle
            val app = application as MyGarageApplication
            val imageStorageManager: ImageStorageManager = app.imageStorageManager
            val imageUploadRepo = ImageUploadRepository(application)

            // Upload the first local image if present (dialog already saved it to storage)
            val imageFileName = vehicle.localImageFileNames.firstOrNull()
            if (imageFileName != null) {
                val imagePath = imageStorageManager.getImagePath(imageFileName)
                if (imagePath != null) {
                    val uri = Uri.fromFile(java.io.File(imagePath))
                    val uploadResult = imageUploadRepo.uploadImage(uri, "vehicle")
                    uploadResult.onSuccess { imageUrl ->
                        updated = updated.copy(remoteImageUrl = imageUrl)
                    }
                }
            }

            repository.updateVehicle(updated)
        }
    }

    /**
     * Fetches the device's current GPS location and persists the coordinates
     * on the currently loaded vehicle entity in Room.
     */
    fun onFetchLocationClicked() {
        Log.d("MyGarage.Location", "onFetchLocationClicked() called")
        val currentVehicle = _uiState.value?.vehicle
        if (currentVehicle == null) {
            Log.w("MyGarage.Location", "onFetchLocationClicked: _uiState.value?.vehicle is NULL, aborting")
            return
        }
        Log.d("MyGarage.Location", "Current vehicle: id=${currentVehicle.id}, lat=${currentVehicle.latitude}, lng=${currentVehicle.longitude}")
        Log.d("MyGarage.Location", "Calling locationManager.getCurrentLocation()...")
        viewModelScope.launch {
            when (val result = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    Log.d("MyGarage.Location", "getCurrentLocation SUCCESS: lat=${result.lat}, lng=${result.lng}")
                    val updated = currentVehicle.copy(
                        latitude = result.lat,
                        longitude = result.lng
                    )
                    Log.d("MyGarage.Location", "Updating vehicle in Room: lat=${updated.latitude}, lng=${updated.longitude}")
                    repository.updateVehicle(updated)
                    Log.d("MyGarage.Location", "Vehicle updated in Room — Flow should emit new state")
                }
                is LocationResult.Error -> {
                    Log.e("MyGarage.Location", "getCurrentLocation ERROR: ${result.message}")
                }
            }
        }
    }

    companion object {
        fun factory(
            repository: VehicleRepository,
            locationManager: LocationManager,
            userPreferencesRepository: UserPreferencesRepository,
            application: Application
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VehicleProfileViewModel(
                        repository, locationManager,
                        userPreferencesRepository, application
                    ) as T
                }
            }
    }
}
