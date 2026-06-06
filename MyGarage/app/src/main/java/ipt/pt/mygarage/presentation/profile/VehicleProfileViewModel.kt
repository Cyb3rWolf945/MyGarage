package ipt.pt.mygarage.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the profile screen of a specific vehicle.
 */
class VehicleProfileViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleWithServices?>(null)
    val uiState: StateFlow<VehicleWithServices?> = _uiState.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

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
            repository.updateVehicle(vehicle)
        }
    }

    companion object {
        fun factory(repository: VehicleRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VehicleProfileViewModel(repository) as T
                }
            }
    }
}
