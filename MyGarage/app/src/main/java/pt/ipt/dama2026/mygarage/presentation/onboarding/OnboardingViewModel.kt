package pt.ipt.dama2026.mygarage.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado do ecrã de onboarding (nome + garagem). */
data class OnboardingUiState(
    val userName: String = "",
    val garageName: String = "",
    val formErrors: Map<String, Int> = emptyMap()
)

/**
 * ViewModel do fluxo de onboarding.
 * Valida nome e garagem, guarda no DataStore e emite evento de navegação.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

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

    /** Utilizador escolheu autenticar-se. Emite evento para navegar para o ecrã de login. */
    fun onSignInClicked() {
        _navigateToAuth.value = true
    }

    /** Utilizador escolheu continuar como guest. Avança para o ecrã de configuração. */
    fun onContinueAsGuest() {
        _advanceToSetupPage.value = true
    }

    /** Reseta a flag para permitir voltar ao formulário guest. */
    fun onAdvanceToSetupConsumed() {
        _advanceToSetupPage.value = false
    }

    /** Valida nome e garagem, guarda no DataStore e emite onboardingCompleted. */
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
