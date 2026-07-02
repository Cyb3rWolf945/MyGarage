package pt.ipt.dama2026.mygarage.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.domain.locale.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Resolves initial navigation destination based on onboarding state.
 * Also applies stored language preference.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _avatarFileName = MutableStateFlow<String?>(null)
    val avatarFileName: StateFlow<String?> = _avatarFileName.asStateFlow()

    private val _avatarRemoteUrl = MutableStateFlow<String?>(null)
    val avatarRemoteUrl: StateFlow<String?> = _avatarRemoteUrl.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _startDestination.value = when {
            preferences.hasCompletedOnboarding -> ROUTE_GARAGE_GRAPH
                    else -> ROUTE_ONBOARDING_GRAPH
                }
                _avatarFileName.value = preferences.avatarFileName
                _avatarRemoteUrl.value = preferences.avatarRemoteUrl
                withContext(Dispatchers.Main) {
                    LocaleManager.applyLanguage(preferences.appLanguage)
                }
                _isLoading.value = false
            }
        }
    }

    companion object {
        const val ROUTE_ONBOARDING_GRAPH = "onboarding_graph"
        const val ROUTE_GARAGE_GRAPH = "garage_graph"
    }
}
