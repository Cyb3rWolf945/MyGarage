package ipt.pt.mygarage.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import ipt.pt.mygarage.domain.locale.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for resolving the initial navigation destination
 * based on whether the user has completed the onboarding flow.
 *
 * Logic:
 * - If hasCompletedOnboarding = true → show GARAGE_GRAPH (user logged in OR guest mode)
 * - If hasCompletedOnboarding = false → show ONBOARDING_GRAPH (first time user)
 *
 * Note: When user logs out, hasCompletedOnboarding remains true, so they won't see
 * the onboarding screen again. They'll go directly to the app in guest mode.
 *
 * Controls the splash screen visibility via [isLoading] and exposes
 * the resolved [startDestination] route string for the NavHost.
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
                    // If user has completed onboarding (logged in OR guest mode) → show garage
                    preferences.hasCompletedOnboarding -> ROUTE_GARAGE_GRAPH
                    // Otherwise → show onboarding
                    else -> ROUTE_ONBOARDING_GRAPH
                }
                _avatarFileName.value = preferences.avatarFileName
                _avatarRemoteUrl.value = preferences.avatarRemoteUrl
                // Apply the stored language preference to the app process on the Main thread
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
