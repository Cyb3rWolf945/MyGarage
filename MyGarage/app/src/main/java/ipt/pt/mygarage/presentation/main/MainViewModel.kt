package ipt.pt.mygarage.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for resolving the initial navigation destination
 * based on whether the user has completed the onboarding flow.
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

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _startDestination.value = if (preferences.hasCompletedOnboarding) {
                    ROUTE_GARAGE_GRAPH
                } else {
                    ROUTE_ONBOARDING_GRAPH
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
