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

/**
 * Trata de tudo relacionado com autenticação: login, registo e logout.
 *
 * Quando o utilizador faz login ou registo com sucesso:
 * 1. O token JWT e os dados do utilizador são guardados no DataStore.
 * 2. O onboarding é marcado como concluído.
 * 3. Decide-se o tipo de sincronização:
 *    - Se havia dados em modo guest → agenda um "guest merge" (junta os dados
 *      locais do guest com a conta nova).
 *    - Se não → faz apenas um sync normal.
 *
 * Em caso de erro, tenta ler a mensagem do corpo da resposta. Se não conseguir,
 * usa uma mensagem genérica.
 *
 * O logout simplesmente apaga o token e volta ao modo guest.
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApiService,
    private val prefs: UserPreferencesRepository,
    private val dao: VehicleDao
) {
    private val gson = Gson()

    /** True se existir token JWT guardado (utilizador autenticado). */
    val isLoggedIn: Flow<Boolean> = prefs.userPreferencesFlow.map { !it.authToken.isNullOrBlank() }
    /** Email do utilizador autenticado, ou null se estiver em modo guest. */
    val userEmail: Flow<String?> = prefs.userPreferencesFlow.map { it.userEmail }

    /**
     * Envia email + password para a API. Se ok, guarda token, perfil e agenda sync.
     * Se a conta já existia no servidor, faz push ou merge dos dados locais.
     */
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

    /**
     * Cria conta nova na API. Lógica pós-registo idêntica ao login:
     * guarda token, marca onboarding, agenda sync.
     */
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

    /** Apaga token e email do DataStore, voltando ao modo guest. :) */
    suspend fun logout() {
        prefs.clearAuth()
    }

    /**
     * Decide o tipo de sync pós-autenticação:
     * - Se havia veículos em modo guest → gera uma assinatura SHA-256 (nome + garagem)
     *   que serve como "impressão digital" da sessão guest. Isto garante que só os dados
     *   desta sessão específica são enviados no merge, evitando injeção de dados de outro
     *   dispositivo. Depois agenda o guest merge.
     * - Caso contrário → sync normal.
     */
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

    /** Gera hash SHA-256 de uma string (usado como assinatura dos dados de guest). */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Tenta extrair a mensagem de erro do JSON da resposta.
     * Se o corpo estiver vazio ou o parse falhar, usa a mensagem de fallback.
     */
    private fun parseError(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            val err = gson.fromJson(errorBody, ErrorResponse::class.java)
            err.error.ifBlank { fallback }
        } catch (_: Exception) {
            fallback
        }
    }

    /** Exceção lançada quando a autenticação falha (credenciais inválidas, rede, etc.). */
    class AuthException(message: String) : Exception(message)
}
