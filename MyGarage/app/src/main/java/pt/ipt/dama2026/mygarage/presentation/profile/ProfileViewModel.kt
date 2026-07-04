package pt.ipt.dama2026.mygarage.presentation.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.local.db.AppDatabase
import pt.ipt.dama2026.mygarage.data.network.SyncApiService
import pt.ipt.dama2026.mygarage.data.repository.AuthRepository
import pt.ipt.dama2026.mygarage.data.repository.ImageUploadRepository
import pt.ipt.dama2026.mygarage.data.repository.SyncRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import pt.ipt.dama2026.mygarage.presentation.locale.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel do ecrã de perfil.
 *
 * Gere:
 * - Preferências do utilizador (nome, garagem, idioma, unidades).
 * - Upload e visualização do avatar.
 * - Sincronização manual (pull-to-refresh).
 * - Logout e eliminação de conta.
 * - Navegação para autenticação/onboarding.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val database: AppDatabase,
    private val syncApiService: SyncApiService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val vehicleRepository: VehicleRepository,
    val imageStorageManager: ImageStorageManager,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val imageUploadRepository: ImageUploadRepository
) : ViewModel() {

    fun getAvatarPath(fileName: String): String? = imageStorageManager.getImagePath(fileName)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navigateToAuth = MutableStateFlow(false)
    val navigateToAuth: StateFlow<Boolean> = _navigateToAuth.asStateFlow()

    private val _navigateToOnboarding = MutableStateFlow(false)
    val navigateToOnboarding: StateFlow<Boolean> = _navigateToOnboarding.asStateFlow()

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
                    prefs.distanceUnit, application
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

    /** Alterar entre modo visualização e edição. Limpa erros ao ativar edição. */
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

    /** Valida nome e garagem. Se ok, guarda no DataStore e sai do modo edição. */
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

    /** Se guest → navega para login. Se autenticado → faz logout. */
    fun onAuthActionClicked() {
        if (_uiState.value.isGuestMode) {
            _navigateToAuth.value = true
        } else {
            viewModelScope.launch {
                authRepository.logout()
            }
        }
    }

    fun onAuthNavigationHandled() {
        _navigateToAuth.value = false
    }

    /** Dispara sync manual (fullSync) e mostra indicador de carregamento. */
    fun onSyncClicked() {
        _uiState.value = _uiState.value.copy(isSyncing = true)
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.fullSync()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    /** Guarda imagem local, faz upload para o servidor e atualiza avatar na UI. */
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
                        SyncWorker.enqueueOneTimeSync(application)
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

    /** Muda idioma e atualiza unidade de distância (PT → km, EN → mi). */
    fun onLanguageChanged(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.updateAppLanguage(language)
            val distanceUnit = when (language.uppercase()) {
                "EN", "ENGLISH" -> "MILES"
                "PT", "PT-PT", "PORTUGUESE" -> "KILOMETERS"
                else -> null
            }
            if (distanceUnit != null) {
                userPreferencesRepository.updateDistanceUnit(distanceUnit)
            }
            withContext(Dispatchers.Main) {
                LocaleManager.applyLanguage(language)
            }
        }
    }

    /** Guarda a unidade de distância escolhida (km ou milhas). */
    fun onDistanceUnitChanged(unit: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateDistanceUnit(unit)
        }
    }

    fun onDeleteAccountClicked() {
        _showDeleteAccountDialog.value = true
    }

    fun onDismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
        _deleteAccountError.value = null
    }

    /** Elimina conta no servidor, limpa BD local e redireciona para onboarding. */
    fun onConfirmDeleteAccount() {
        _showDeleteAccountDialog.value = false
        _isDeletingAccount.value = true
        _deleteAccountError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = syncApiService.deleteAccount()

                if (response.isSuccessful) {
                    database.clearAllTables()
                    userPreferencesRepository.clearAllUserData()

                    _navigateToOnboarding.value = true
                    _navigateToAuth.value = true
                } else {
                    _deleteAccountError.value = application.getString(
                        R.string.delete_account_error
                    ) + " (${response.code()})"
                }
            } catch (e: Exception) {
                _deleteAccountError.value = application.getString(
                    R.string.delete_account_error
                ) + ": ${e.message}"
            } finally {
                _isDeletingAccount.value = false
            }
        }
    }
}
