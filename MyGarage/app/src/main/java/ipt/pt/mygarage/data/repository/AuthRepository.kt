package ipt.pt.mygarage.data.repository

import android.content.Context
import ipt.pt.mygarage.data.local.db.AppDatabase
import ipt.pt.mygarage.data.model.AuthResponse
import ipt.pt.mygarage.data.model.ErrorResponse
import ipt.pt.mygarage.data.model.LoginRequest
import ipt.pt.mygarage.data.model.RegisterRequest
import ipt.pt.mygarage.data.network.NetworkModule
import ipt.pt.mygarage.data.sync.SyncWorker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(private val context: Context) {

    private val api = NetworkModule.createAuthApiService(context)
    private val prefs = UserPreferencesRepository(context)
    private val dao = AppDatabase.getDatabase(context).vehicleDao()
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
                    val message = parseError(errorBody, "Invalid email or password")
                    Result.failure(AuthException(message))
                }
            } catch (e: Exception) {
                Result.failure(AuthException("Network error. Please check your connection."))
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
                    val message = parseError(errorBody, "Registration failed")
                    Result.failure(AuthException(message))
                }
            } catch (e: Exception) {
                Result.failure(AuthException("Network error. Please check your connection."))
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
