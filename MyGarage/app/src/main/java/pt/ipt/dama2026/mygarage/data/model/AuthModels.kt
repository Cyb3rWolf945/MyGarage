package pt.ipt.dama2026.mygarage.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelos de dados para autenticação (login/registo).
 * Estes modelos servem para serializar (envio) e desserializar (resposta).
 *
 * @SerializedName mapeia os nomes Kotlin (camelCase) para os nomes JSON da API (campo).
 */

/** Payload para pedido de login. */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/** Payload para pedido de registo. */
data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("garageName") val garageName: String? = null
)

/** Resposta de sucesso de autenticação (token JWT + perfil). */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUser
)

/** Perfil do utilizador autenticado. */
data class AuthUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("garageName") val garageName: String? = null
)

/** Corpo de erro devolvido pela API. */
data class ErrorResponse(
    @SerializedName("error") val error: String
)
