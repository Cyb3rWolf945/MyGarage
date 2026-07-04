package pt.ipt.dama2026.mygarage.data.network

import pt.ipt.dama2026.mygarage.data.model.AuthResponse
import pt.ipt.dama2026.mygarage.data.model.LoginRequest
import pt.ipt.dama2026.mygarage.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Interface Retrofit para os endpoints de autenticação (login/registo). */
interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
