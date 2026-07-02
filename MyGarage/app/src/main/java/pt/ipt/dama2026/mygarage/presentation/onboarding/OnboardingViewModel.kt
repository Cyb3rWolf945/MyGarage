package pt.ipt.dama2026.mygarage.presentation.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for onboarding.
 */
data class OnboardingUiState(
    val userName: String = "",
    val garageName: String = "",
    val formErrors: Map<String, Int> = emptyMap()
)

/**
 * ViewModel for the 3-step onboarding flow. Validates inputs, persists
 * preferences, emits navigation events.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _navigateToAuth = MutableStateFlow(false)
    val navigateToAuth: StateFlow<Boolean> = _navigateToAuth.asStateFlow()

    private val _advanceToSetupPage = MutableStateFlow(false)
    val advanceToSetupPage: StateFlow<Boolean> = _advanceToSetupPage.asStateFlow()

    fun onUserNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(
                userName = name,
                formErrors = state.formErrors - FIELD_USER_NAME
            )
        }
    }

    fun onGarageNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(
                garageName = name,
                formErrors = state.formErrors - FIELD_GARAGE_NAME
            )
        }
    }

    /**
     * User chose to authenticate. Emits a navigation event
     * that the UI layer uses to route to the auth graph.
     */
    fun onSignInClicked() {
        _navigateToAuth.value = true
    }

    /**
     * User chose to continue without signing in.
     * Emits a signal for the UI to programmatically
     * advance the pager to the setup (guest) screen.
     */
    fun onContinueAsGuest() {
        _advanceToSetupPage.value = true
    }

    /** Reset so the user can re-enter the guest form after going back. */
    fun onAdvanceToSetupConsumed() {
        _advanceToSetupPage.value = false
    }

    fun onFinishClicked() {
        val state = _uiState.value
        val errors = mutableMapOf<String, Int>()

        if (state.userName.isBlank()) {
            errors[FIELD_USER_NAME] = R.string.onboarding_error_user_name_blank
        }
        if (state.garageName.isBlank()) {
            errors[FIELD_GARAGE_NAME] = R.string.onboarding_error_garage_name_blank
        }

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(formErrors = errors)
            return
        }

        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding(
                userName = state.userName.trim(),
                garageName = state.garageName.trim()
            )
            _onboardingCompleted.value = true
        }
    }

    companion object {
        const val FIELD_USER_NAME = "userName"
        const val FIELD_GARAGE_NAME = "garageName"
        const val PAGE_SETUP = 2
    }
}
