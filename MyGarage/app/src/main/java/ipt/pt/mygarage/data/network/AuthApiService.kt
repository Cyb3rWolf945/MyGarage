package ipt.pt.mygarage.data.network

import ipt.pt.mygarage.data.model.AuthResponse
import ipt.pt.mygarage.data.model.LoginRequest
import ipt.pt.mygarage.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
