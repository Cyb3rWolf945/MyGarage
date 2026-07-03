package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.model.AuthResponse
import pt.ipt.dama2026.mygarage.data.model.ErrorResponse
import pt.ipt.dama2026.mygarage.data.model.LoginRequest
import pt.ipt.dama2026.mygarage.data.model.RegisterRequest
import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.network.AuthApiService
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApiService,
    private val prefs: UserPreferencesRepository,
    private val dao: VehicleDao
) {
    private val gson = Gson()

    val isLoggedIn: Flow<Boolean> = prefs.userPreferencesFlow.map { !it.authToken.isNullOrBlank() }
    val userEmail: Flow<String?> = prefs.userPreferencesFlow.map { it.userEmail }

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    prefs.saveAuth(body.token, body.user.email, body.user.name, body.user.garageName)
                    prefs.markOnboardingComplete()
                    triggerSyncBasedOnGuestData(body.user.name, body.user.garageName)
                    Result.success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = parseError(errorBody, context.getString(R.string.auth_error_invalid_credentials))
                    Result.failure(AuthException(message))
                }
            } catch (e: Exception) {
                Result.failure(AuthException(context.getString(R.string.auth_error_network)))
            }
        }

    suspend fun register(
        email: String,
        password: String,
        name: String? = null,
        garageName: String? = null
    ): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.register(RegisterRequest(email, password, name, garageName))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    prefs.saveAuth(body.token, body.user.email, name, garageName)
                    prefs.markOnboardingComplete()
                    triggerSyncBasedOnGuestData(name ?: body.user.name, garageName ?: body.user.garageName)
                    Result.success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = parseError(errorBody, context.getString(R.string.auth_error_network))
                    Result.failure(AuthException(message))
                }
            } catch (e: Exception) {
                Result.failure(AuthException(context.getString(R.string.auth_error_network)))
            }
        }

    suspend fun logout() {
        prefs.clearAuth()
    }

    private suspend fun triggerSyncBasedOnGuestData(userName: String?, garageName: String?) {
        val currentPrefs = prefs.userPreferencesFlow.firstOrNull() ?: return
        val hasGuestData = currentPrefs.isGuestMode && dao.getAllVehiclesList().isNotEmpty()

        if (hasGuestData && !userName.isNullOrBlank() && !garageName.isNullOrBlank()) {
            val signature = sha256("${userName}${garageName}")
            prefs.setGuestDataSignature(signature)
            prefs.setRequiresGuestMerge(true)
            SyncWorker.enqueueGuestMergeSyncWorker(context)
        } else {
            SyncWorker.enqueueOneTimeSync(context)
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun parseError(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            val err = gson.fromJson(errorBody, ErrorResponse::class.java)
            err.error.ifBlank { fallback }
        } catch (_: Exception) {
            fallback
        }
    }

    class AuthException(message: String) : Exception(message)
}
