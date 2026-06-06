package ipt.pt.mygarage.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the profile screen of a specific vehicle.
 */
class VehicleProfileViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleWithServices?>(null)
    val uiState: StateFlow<VehicleWithServices?> = _uiState.asStateFlow()

    /**
     * Starts collecting the vehicle and its services from Room.
     */
    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.getVehicleWithServices(vehicleId)
                .catch { e -> e.printStackTrace() }
                .collect { vehicleWithServices ->
                    _uiState.value = vehicleWithServices
                }
        }
    }

    fun updateVehicle(vehicle: VehicleEntity) {
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
