package ipt.pt.mygarage.presentation.auth

data class AuthUiState(
    val isLogin: Boolean = true,
    val name: String = "",
    val garageName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val formErrors: Map<String, Int> = emptyMap()
)
