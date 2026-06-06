package ipt.pt.mygarage.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the main garage screen.
 */
class GarageViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    // Expose vehicles from local Room persistence as a read-only StateFlow
    val vehiclesState: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.insertVehicle(vehicle)
        }
    }

    companion object {
        fun factory(repository: VehicleRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GarageViewModel(repository) as T
                }
            }
    }
}
