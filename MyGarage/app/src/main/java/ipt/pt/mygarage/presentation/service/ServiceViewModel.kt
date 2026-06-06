package ipt.pt.mygarage.presentation.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.domain.repository.VehicleRepository
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
        viewModelScope.launch {
            repository.insertServiceLog(serviceLog)
            val partsToInsert = _temporaryParts.value.map { part ->
                part.copy(serviceLogId = serviceLog.id.toString())
            }
            partsToInsert.forEach { repository.insertPart(it) }
            _temporaryParts.value = emptyList()
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
