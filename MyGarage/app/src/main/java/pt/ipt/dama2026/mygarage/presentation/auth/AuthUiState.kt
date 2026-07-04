package pt.ipt.dama2026.mygarage.presentation.auth

/**
 * Estado do ecrã de autenticação (login/registo).
 *
 * - isLogin: true = mostra formulário de login, false = registo.
 * - isLoading: true enquanto a chamada à API está a decorrer.
 * - errorMessage: mensagem geral de erro (ex.: credenciais inválidas).
 * - formErrors: erros por campo (chave = nome do campo, valor = ID da string).
 *
 * Gerido pelo AuthViewModel e observado pelo AuthScreen.
 */
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
