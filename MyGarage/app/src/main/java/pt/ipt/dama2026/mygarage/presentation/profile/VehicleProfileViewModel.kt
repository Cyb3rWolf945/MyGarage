package pt.ipt.dama2026.mygarage.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.data.mapper.toDomain
import pt.ipt.dama2026.mygarage.data.mapper.toEntity
import pt.ipt.dama2026.mygarage.data.repository.ImageUploadRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.presentation.locale.LocaleManager
import pt.ipt.dama2026.mygarage.domain.location.LocationManager
import pt.ipt.dama2026.mygarage.domain.location.LocationResult
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import pt.ipt.dama2026.mygarage.presentation.validation.VehicleValidator
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel do perfil de um veículo.
 *
 * Gere:
 * - Visualização e edição dos detalhes do veículo.
 * - Upload de imagens e visualização (Coil via proxy).
 * - Obtenção de localização GPS.
 * - Eliminação do veículo (soft-delete).
 */
@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val repository: VehicleRepository,
    val locationManager: LocationManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    val imageStorageManager: ImageStorageManager,
    private val imageUploadRepository: ImageUploadRepository,
    private val application: Application
) : ViewModel() {

    fun resolveImagePath(fileName: String): String? = imageStorageManager.getImagePath(fileName)

    private val _resolvedDistanceUnit = MutableStateFlow("KILOMETERS")
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

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    private val _deleteCompleted = MutableStateFlow(false)
    val deleteCompleted: StateFlow<Boolean> = _deleteCompleted.asStateFlow()

    private val _isCarouselVisible = MutableStateFlow(false)
    val isCarouselVisible: StateFlow<Boolean> = _isCarouselVisible.asStateFlow()

    private val _carouselStartIndex = MutableStateFlow(0)
    val carouselStartIndex: StateFlow<Int> = _carouselStartIndex.asStateFlow()

    /** Mostra diálogo de confirmação para apagar o veículo. */
    fun showDeleteDialog() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirmation.value = false
    }

    /** Abre o carrossel de imagens na posição indicada. */
    fun openCarousel(startIndex: Int = 0) {
        _carouselStartIndex.value = startIndex
        _isCarouselVisible.value = true
    }

    fun closeCarousel() {
        _isCarouselVisible.value = false
    }

    /** Executa o soft-delete do veículo e agenda sync. */
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
            SyncWorker.enqueueOneTimeSync(application)
        }
    }

    fun onDeleteCompletedHandled() {
        _deleteCompleted.value = false
    }

    /** Carrega o veículo e o histórico de serviços da BD. */
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

    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

    /** Valida campos obrigatórios do veículo via VehicleValidator. */
    private fun validateVehicle(vehicle: Vehicle): Boolean {
        val errors = VehicleValidator.validate(vehicle.toEntity())
        _formErrors.value = errors
        return errors.isEmpty()
    }

    private suspend fun uploadFirstImage(vehicle: Vehicle): Vehicle {
        val imageFileName = vehicle.localImageFileNames.firstOrNull() ?: return vehicle
        val imagePath = imageStorageManager.getImagePath(imageFileName) ?: return vehicle
        val uri = Uri.fromFile(java.io.File(imagePath))
        val uploadResult = imageUploadRepository.uploadImage(uri, "vehicle")
        return if (uploadResult.isSuccess) vehicle.copy(remoteImageUrl = uploadResult.getOrNull()) else vehicle
    }

    /** Valida, faz upload da primeira imagem (se houver) e guarda o veículo. */
    fun updateVehicle(vehicle: Vehicle) {
        if (!validateVehicle(vehicle)) return
        viewModelScope.launch {
            repository.updateVehicle(uploadFirstImage(vehicle))
            SyncWorker.enqueueOneTimeSync(application)
        }
    }

    /** Pede localização GPS e guarda as coordenadas no veículo atual. */
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
                    SyncWorker.enqueueOneTimeSync(application)
                    Log.d("MyGarage.Location", "Vehicle updated in Room — Flow should emit new state")
                }
                is LocationResult.Error -> {
                    Log.e("MyGarage.Location", "getCurrentLocation ERROR: ${result.message}")
                }
            }
        }
    }
}
