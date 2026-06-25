package ipt.pt.mygarage.presentation.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(name = name, errorMessage = null, formErrors = state.formErrors - FIELD_NAME)
        }
    }

    fun onGarageNameChanged(garageName: String) {
        _uiState.update { state ->
            state.copy(garageName = garageName, errorMessage = null, formErrors = state.formErrors - FIELD_GARAGE_NAME)
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { state ->
            state.copy(email = email, errorMessage = null, formErrors = state.formErrors - FIELD_EMAIL)
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { state ->
            state.copy(password = password, errorMessage = null, formErrors = state.formErrors - FIELD_PASSWORD)
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { state ->
            state.copy(confirmPassword = confirmPassword, errorMessage = null, formErrors = state.formErrors - FIELD_CONFIRM_PASSWORD)
        }
    }

    fun onToggleMode() {
        _uiState.update { state ->
            state.copy(
                isLogin = !state.isLogin,
                errorMessage = null,
                formErrors = emptyMap(),
                name = "",
                garageName = "",
                confirmPassword = ""
            )
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        val errors = mutableMapOf<String, Int>()

        if (state.email.isBlank()) {
            errors[FIELD_EMAIL] = R.string.auth_error_email_blank
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            errors[FIELD_EMAIL] = R.string.auth_error_email_invalid
        }

        if (state.password.isBlank()) {
            errors[FIELD_PASSWORD] = R.string.auth_error_password_blank
        } else if (state.password.length < 6) {
            errors[FIELD_PASSWORD] = R.string.auth_error_password_short
        } else if (!isPasswordStrong(state.password)) {
            errors[FIELD_PASSWORD] = R.string.auth_error_password_weak
        }

        if (!state.isLogin && state.password != state.confirmPassword) {
            errors[FIELD_CONFIRM_PASSWORD] = R.string.auth_error_password_match
        }

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(formErrors = errors, errorMessage = null)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, formErrors = emptyMap())

        viewModelScope.launch {
            val result = if (state.isLogin) {
                authRepository.login(state.email.trim(), state.password)
            } else {
                authRepository.register(
                    email = state.email.trim(),
                    password = state.password,
                    name = state.name.trim().ifBlank { null },
                    garageName = state.garageName.trim().ifBlank { null }
                )
            }

            result.fold(
                onSuccess = { _authSuccess.value = true },
                onFailure = { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unexpected error occurred"
                    )}
                }
            )
        }
    }

    fun clearAuthSuccess() {
        _authSuccess.value = false
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        return hasUpperCase && hasSymbol
    }

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_GARAGE_NAME = "garageName"
        const val FIELD_EMAIL = "email"
        const val FIELD_PASSWORD = "password"
        const val FIELD_CONFIRM_PASSWORD = "confirmPassword"
    }
}
