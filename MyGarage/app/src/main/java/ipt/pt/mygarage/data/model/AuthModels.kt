package ipt.pt.mygarage.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("garageName") val garageName: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUser
)

data class AuthUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("garageName") val garageName: String? = null
)

data class ErrorResponse(
    @SerializedName("error") val error: String
)
