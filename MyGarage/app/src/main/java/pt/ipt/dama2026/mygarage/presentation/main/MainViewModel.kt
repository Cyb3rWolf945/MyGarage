package pt.ipt.dama2026.mygarage.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.data.repository.SyncRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.presentation.locale.LocaleManager
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel principal. Decide o ecrã inicial (onboarding ou garagem),
 * aplica idioma guardado e carrega avatar.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val imageStorageManager: ImageStorageManager,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _avatarFileName = MutableStateFlow<String?>(null)
    val avatarFileName: StateFlow<String?> = _avatarFileName.asStateFlow()

    private val _avatarRemoteUrl = MutableStateFlow<String?>(null)
    val avatarRemoteUrl: StateFlow<String?> = _avatarRemoteUrl.asStateFlow()

    private val _avatarLocalFile = MutableStateFlow<java.io.File?>(null)
    val avatarLocalFile: StateFlow<java.io.File?> = _avatarLocalFile.asStateFlow()

    /** Exposto para o MainScreen observar se o user está autenticado. */
    val authToken: StateFlow<String?> = userPreferencesRepository.userAuthTokenFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _startDestination.value = when {
            preferences.hasCompletedOnboarding -> ROUTE_GARAGE_GRAPH
                    else -> ROUTE_ONBOARDING_GRAPH
                }
                _avatarFileName.value = preferences.avatarFileName
                _avatarRemoteUrl.value = preferences.avatarRemoteUrl
                _avatarLocalFile.value = preferences.avatarFileName?.let { fileName ->
                    imageStorageManager.getImagePath(fileName)?.let { java.io.File(it) }
                }
                withContext(Dispatchers.Main) {
                    LocaleManager.applyLanguage(preferences.appLanguage)
                }
                _isLoading.value = false
            }
        }
    }

    /** Puxa o perfil do utilizador do servidor (avatar, nome, garagem). */
    fun pullUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.pullAndSyncUserProfile()
        }
    }

    companion object {
        const val ROUTE_ONBOARDING_GRAPH = "onboarding_graph"
        const val ROUTE_GARAGE_GRAPH = "garage_graph"
    }
}
