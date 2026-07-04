package pt.ipt.dama2026.mygarage.presentation.garage

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.data.mapper.toDomain
import pt.ipt.dama2026.mygarage.data.mapper.toEntity
import pt.ipt.dama2026.mygarage.data.repository.ImageUploadRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.engine.EngineCapacityHelper
import pt.ipt.dama2026.mygarage.domain.location.LocationManager
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import pt.ipt.dama2026.mygarage.presentation.validation.VehicleValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel do ecrã principal (garagem).
 *
 * Gere:
 * - CRUD de veículos (adicionar, editar, apagar).
 * - Upload de imagens para veículos novos.
 * - Obtenção de localização GPS ao criar veículo.
 * - Validação de formulário (via VehicleValidator).
 * - Arredondamento de cilindrada (via EngineCapacityHelper).
 */
@HiltViewModel
class GarageViewModel @Inject constructor(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val repository: VehicleRepository,
    val imageStorageManager: ImageStorageManager,
    val locationManager: LocationManager,
    private val imageUploadRepository: ImageUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarageUiState())
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    val vehiclesState: StateFlow<List<Vehicle>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val formErrors: StateFlow<Map<String, Int>> = _formErrors.asStateFlow()

    private val _selectedVehicleForOptions = MutableStateFlow<Vehicle?>(null)
    val selectedVehicleForOptions: StateFlow<Vehicle?> = _selectedVehicleForOptions.asStateFlow()

    private val _vehicleToEdit = MutableStateFlow<Vehicle?>(null)
    val vehicleToEdit: StateFlow<Vehicle?> = _vehicleToEdit.asStateFlow()

    private val _vehicleToDelete = MutableStateFlow<Vehicle?>(null)
    val vehicleToDelete: StateFlow<Vehicle?> = _vehicleToDelete.asStateFlow()

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

    /** Mostra menu de opções (editar/apagar) ao fazer long-press num veículo. */
    fun onVehicleLongPressed(vehicle: Vehicle) {
        _selectedVehicleForOptions.value = vehicle
    }

    fun onDismissOptionsMenu() {
        _selectedVehicleForOptions.value = null
    }

    /** Prepara a edição: guarda o veículo a editar e carrega a imagem existente. */
    fun onSelectEdit(vehicle: Vehicle) {
        _selectedVehicleForOptions.value = null
        _vehicleToEdit.value = vehicle
        _uiState.update { it.copy(existingImageFileName = vehicle.localImageFileNames.firstOrNull()) }
    }

    fun onSelectDelete(vehicle: Vehicle) {
        _selectedVehicleForOptions.value = null
        showDeleteDialog(vehicle)
    }

    /** Mostra diálogo de confirmação para apagar o veículo. */
    fun showDeleteDialog(vehicle: Vehicle) {
        _vehicleToDelete.value = vehicle
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirmation.value = false
        _vehicleToDelete.value = null
    }

    /** Soft-delete do veículo, fecha diálogo e agenda sync. */
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
            SyncWorker.enqueueOneTimeSync(application)
        }
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

    /** Guarda a imagem selecionada no armazenamento interno. Devolve o nome do ficheiro ou null. */
    private suspend fun saveSelectedImage(): String? {
        val uri = _uiState.value.selectedImageUri ?: return null
        return imageStorageManager.saveImage(uri)
    }

    /** Faz upload da primeira imagem local do veículo. Devolve o veículo com remoteUrl atualizado. */
    private suspend fun uploadFirstImage(vehicle: Vehicle): Vehicle {
        val imageFileName = vehicle.localImageFileNames.firstOrNull() ?: return vehicle
        val imagePath = imageStorageManager.getImagePath(imageFileName) ?: return vehicle
        val uri = Uri.fromFile(java.io.File(imagePath))
        val uploadResult = imageUploadRepository.uploadImage(uri, "vehicle")
        return if (uploadResult.isSuccess) vehicle.copy(remoteImageUrl = uploadResult.getOrNull()) else vehicle
    }

    /** Insere ou atualiza o veículo, faz upload da imagem, limpa formulário e agenda sync. */
    private suspend fun saveVehicle(vehicle: Vehicle, isNew: Boolean) {
        val updated = uploadFirstImage(vehicle)
        if (isNew) repository.insertVehicle(updated) else repository.updateVehicle(updated)
        clearImageSelection()
        SyncWorker.enqueueOneTimeSync(application)
    }

    fun clearFieldError(fieldName: String) {
        if (_formErrors.value.containsKey(fieldName)) {
            _formErrors.update { it - fieldName }
        }
    }

    private fun validateVehicle(vehicle: Vehicle): Boolean {
        val errors = VehicleValidator.validate(vehicle.toEntity())
        _formErrors.value = errors
        return errors.isEmpty()
    }

    /** Guarda as alterações do veículo editado. */
    fun confirmEdit(vehicle: Vehicle) {
        viewModelScope.launch { saveVehicle(vehicle, isNew = false) }
    }

    /** Valida e insere um veículo novo. Faz upload da imagem se existir. */
    fun insertVehicle(vehicle: Vehicle) {
        if (!validateVehicle(vehicle)) return
        viewModelScope.launch { saveVehicle(vehicle, isNew = true) }
    }

    /** Abre o diálogo de novo veículo com dados pré-preenchidos (ex.: da matrícula). */
    fun openAddDialogWithData(
        plate: String,
        name: String,
        year: String,
        fuelType: String,
        engineCapacity: String
    ) {
        val roundedEngineCapacity = EngineCapacityHelper.roundToNearestOption(engineCapacity)
        val newVehicle = Vehicle(
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
