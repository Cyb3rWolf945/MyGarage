package ipt.pt.mygarage.presentation.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.MyGarageApplication
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import ipt.pt.mygarage.domain.repository.ImageStorageManager
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val vehicleRepository: VehicleRepository =
        (application as MyGarageApplication).repository
    private val imageStorageManager: ImageStorageManager =
        (application as MyGarageApplication).imageStorageManager

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.userPreferencesFlow,
                vehicleRepository.getAllVehicles()
            ) { prefs, vehicles ->
                ProfileUiState(
                    userName = prefs.userName,
                    garageName = prefs.garageName,
                    isGuestMode = prefs.isGuestMode,
                    carsOwned = vehicles.size,
                    totalMileage = vehicles.sumOf { extractNumericMileage(it.mileage) },
                    isEditing = _uiState.value.isEditing,
                    formErrors = _uiState.value.formErrors,
                    avatarFileName = prefs.avatarFileName
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    /**
     * Strips all non-numeric characters from a mileage string and returns the integer value.
     * Examples: "12,450 mi" → 12450, "8,920 mi" → 8920, "" → 0
     */
    private fun extractNumericMileage(mileage: String): Int {
        return mileage.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    fun onEditToggled() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing,
            formErrors = emptyMap()
        )
    }

    fun onUserNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            userName = name,
            formErrors = _uiState.value.formErrors - "userName"
        )
    }

    fun onGarageNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            garageName = name,
            formErrors = _uiState.value.formErrors - "garageName"
        )
    }

    fun onSaveProfile() {
        val state = _uiState.value
        val errors = mutableMapOf<String, Int>()

        if (state.userName.isBlank()) {
            errors["userName"] = R.string.error_user_name_required
        }
        if (state.garageName.isBlank()) {
            errors["garageName"] = R.string.error_garage_name_required
        }

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(formErrors = errors)
            return
        }

        viewModelScope.launch {
            userPreferencesRepository.updateUserName(state.userName.trim())
            userPreferencesRepository.updateGarageName(state.garageName.trim())
            _uiState.value = _uiState.value.copy(
                isEditing = false,
                formErrors = emptyMap()
            )
        }
    }

    fun onAuthActionClicked() {
        val current = _uiState.value.isGuestMode
        viewModelScope.launch {
            userPreferencesRepository.setGuestMode(!current)
        }
    }

    fun onAvatarSelected(uri: String) {
        viewModelScope.launch {
            val fileName = imageStorageManager.saveImage(uri)
            if (fileName != null) {
                userPreferencesRepository.updateAvatarFileName(fileName)
            }
        }
    }
}
