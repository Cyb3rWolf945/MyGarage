package pt.ipt.dama2026.mygarage.data.model

/**
 * Estado completo das preferências do utilizador, guardado no DataStore do Jetpack.
 *
 * Inclui:
 * - Perfil (nome, garagem, avatar).
 * - Autenticação (token JWT, email, modo guest).
 * - Definições da app (idioma, unidade de distância, onboarding).
 * - Sincronização (timestamp do último sync, merge de dados guest).
 *
 * O UserPreferencesRepository lê/escreve este modelo e expõe flows reativos
 * para a UI reagir a alterações (ex.: login/logout, mudança de idioma).
 */
data class UserPreferences(
    val userName: String = "Driver",
    val garageName: String = "My Garage",
    val isGuestMode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val avatarFileName: String? = null,
    val avatarRemoteUrl: String? = null,
    val totalUserMileage: Int = 0,
    val appLanguage: String = "SYSTEM",
    val distanceUnit: String = "SYSTEM",
    val authToken: String? = null,
    val userEmail: String? = null,
    val lastSyncTimestamp: Long? = null,
    val requiresGuestMerge: Boolean = false,
    val guestDataSignature: String? = null
)
