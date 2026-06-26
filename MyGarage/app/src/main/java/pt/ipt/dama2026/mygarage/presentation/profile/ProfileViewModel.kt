package pt.ipt.dama2026.mygarage.presentation.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import pt.ipt.dama2026.mygarage.MyGarageApplication
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.repository.AuthRepository
import pt.ipt.dama2026.mygarage.data.repository.ImageUploadRepository
import pt.ipt.dama2026.mygarage.data.repository.SyncRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import pt.ipt.dama2026.mygarage.domain.locale.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val vehicleRepository: VehicleRepository =
        (application as MyGarageApplication).repository
    private val imageStorageManager: ImageStorageManager =
        (application as MyGarageApplication).imageStorageManager
    private val authRepository = AuthRepository(application)
    private val syncRepository = SyncRepository(application)
    private val imageUploadRepository = ImageUploadRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Emits true when the user should navigate to the auth screen. */
    private val _navigateToAuth = MutableStateFlow(false)
    val navigateToAuth: StateFlow<Boolean> = _navigateToAuth.asStateFlow()

    /** Emits true when navigation is from account deletion — goes to onboarding. */
    private val _navigateToOnboarding = MutableStateFlow(false)
    val navigateToOnboarding: StateFlow<Boolean> = _navigateToOnboarding.asStateFlow()

    // ── Delete Account state ───────────────────────────────────────────
    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog: StateFlow<Boolean> = _showDeleteAccountDialog.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private val _deleteAccountError = MutableStateFlow<String?>(null)
    val deleteAccountError: StateFlow<String?> = _deleteAccountError.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.userPreferencesFlow,
                vehicleRepository.getAllVehicles(),
                userPreferencesRepository.totalUserMileageFlow,
                userPreferencesRepository.lastSyncTimestampFlow
            ) { prefs, vehicles, drivenMileage, lastSync ->
                val resolvedUnit = LocaleManager.resolveDistanceUnit(
                    prefs.distanceUnit, getApplication()
                )
                ProfileUiState(
                    userName = prefs.userName,
                    garageName = prefs.garageName,
                    isGuestMode = prefs.isGuestMode,
                    carsOwned = vehicles.size,
                    totalMileage = drivenMileage,
                    isEditing = _uiState.value.isEditing,
                    formErrors = _uiState.value.formErrors,
                    avatarFileName = prefs.avatarFileName,
                    appLanguage = prefs.appLanguage,
                    distanceUnit = prefs.distanceUnit,
                    resolvedDistanceUnit = resolvedUnit,
                    userEmail = prefs.userEmail,
                    lastSyncTimestamp = lastSync,
                    isSyncing = _uiState.value.isSyncing,
                    isUploadingAvatar = _uiState.value.isUploadingAvatar,
                    avatarUploadError = _uiState.value.avatarUploadError,
                    avatarRemoteUrl = _uiState.value.avatarRemoteUrl
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
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
        if (_uiState.value.isGuestMode) {
            // Navigate to the auth (login/register) screen
            _navigateToAuth.value = true
        } else {
            // Sign out: clear auth token, switch to guest mode
            viewModelScope.launch {
                authRepository.logout()
            }
        }
    }

    fun onAuthNavigationHandled() {
        _navigateToAuth.value = false
    }

    fun onSyncClicked() {
        _uiState.value = _uiState.value.copy(isSyncing = true)
        viewModelScope.launch(Dispatchers.IO) {
            val result = syncRepository.fullSync()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun onAvatarSelected(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                isUploadingAvatar = true,
                avatarUploadError = null
            )

            try {
                val fileName = imageStorageManager.saveImage(uri)
                if (fileName != null) {
                    userPreferencesRepository.updateAvatarFileName(fileName)

                    val imageUri = Uri.parse(uri)
                    val uploadResult = imageUploadRepository.uploadImage(imageUri, "user-profile")

                    uploadResult.onSuccess { imageUrl ->
                        userPreferencesRepository.updateAvatarRemoteUrl(imageUrl)
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false,
                            avatarRemoteUrl = imageUrl,
                            avatarUploadError = null
                        )
                        // Push avatar URL to backend immediately so it survives reinstalls
                        SyncWorker.enqueueOneTimeSync(getApplication())
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false,
                            avatarUploadError = error.message ?: "Upload failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    avatarUploadError = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun onLanguageChanged(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Wait for the DataStore write to fully commit to disk
            userPreferencesRepository.updateAppLanguage(language)
            // 2. Switch to Main thread to trigger UI / Activity recreation
            withContext(Dispatchers.Main) {
                LocaleManager.applyLanguage(language)
            }
        }
    }

    fun onDistanceUnitChanged(unit: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateDistanceUnit(unit)
        }
    }

    // ── Delete Account ─────────────────────────────────────────────────

    fun onDeleteAccountClicked() {
        _showDeleteAccountDialog.value = true
    }

    fun onDismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
        _deleteAccountError.value = null
    }

    fun onConfirmDeleteAccount() {
        _showDeleteAccountDialog.value = false
        _isDeletingAccount.value = true
        _deleteAccountError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<android.app.Application>()
                val api = pt.ipt.dama2026.mygarage.data.network.NetworkModule
                    .createSyncApiService(app)
                val response = api.deleteAccount()

                if (response.isSuccessful) {
                    // Clear local Room database
                    val db = (app as MyGarageApplication).database
                    db.clearAllTables()

                    // Clear DataStore (logout also clears auth token)
                    userPreferencesRepository.clearAllUserData()

                    _navigateToOnboarding.value = true
                    _navigateToAuth.value = true
                } else {
                    _deleteAccountError.value = app.getString(R.string.delete_account_error)
                }
            } catch (_: Exception) {
                _deleteAccountError.value = getApplication<android.app.Application>()
                    .getString(R.string.delete_account_error)
            } finally {
                _isDeletingAccount.value = false
            }
        }
    }
}
