package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import pt.ipt.dama2026.mygarage.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Gere as preferências do utilizador com Jetpack DataStore.
 *
 * A UI observa as preferências e atualiza-se automaticamente quando mudam.
 * As gravações são feitas de uma só vez e correm em background sem bloquear a interface.
 *
 * Usado por:
 * - AuthRepository: token, email, modo guest.
 * - SyncRepository: timestamp de sync, IDs apagados, flag de merge.
 * - LocaleManager: idioma e unidade de distância.
 * - ProfileViewModel: nome, garagem, avatar.
 */
class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_GARAGE_NAME = stringPreferencesKey("garage_name")
        val KEY_IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_AVATAR_FILE_NAME = stringPreferencesKey("avatar_file_name")
        val KEY_AVATAR_REMOTE_URL = stringPreferencesKey("avatar_remote_url")
        val KEY_TOTAL_USER_MILEAGE = intPreferencesKey("total_user_mileage")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val KEY_DELETED_VEHICLE_IDS = stringPreferencesKey("deleted_vehicle_ids")
        val KEY_REQUIRES_GUEST_MERGE = booleanPreferencesKey("requires_guest_merge")
        val KEY_GUEST_DATA_SIGNATURE = stringPreferencesKey("guest_data_signature")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            userName = preferences[KEY_USER_NAME] ?: "Driver",
            garageName = preferences[KEY_GARAGE_NAME] ?: "My Garage",
            isGuestMode = preferences[KEY_IS_GUEST_MODE] ?: true,
            hasCompletedOnboarding = preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false,
            avatarFileName = preferences[KEY_AVATAR_FILE_NAME],
            avatarRemoteUrl = preferences[KEY_AVATAR_REMOTE_URL],
            totalUserMileage = preferences[KEY_TOTAL_USER_MILEAGE] ?: 0,
            appLanguage = preferences[KEY_APP_LANGUAGE] ?: "SYSTEM",
            distanceUnit = preferences[KEY_DISTANCE_UNIT] ?: "SYSTEM",
            authToken = preferences[KEY_AUTH_TOKEN],
            userEmail = preferences[KEY_USER_EMAIL],
            lastSyncTimestamp = preferences[KEY_LAST_SYNC_TIMESTAMP],
            requiresGuestMerge = preferences[KEY_REQUIRES_GUEST_MERGE] ?: false,
            guestDataSignature = preferences[KEY_GUEST_DATA_SIGNATURE]
        )
    }

    val userAuthTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTH_TOKEN]
    }.distinctUntilChanged()

    val lastSyncTimestampFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_SYNC_TIMESTAMP]
    }.distinctUntilChanged()

    val distanceUnitFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISTANCE_UNIT] ?: "SYSTEM"
    }.distinctUntilChanged()

    val totalUserMileageFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOTAL_USER_MILEAGE] ?: 0
    }.distinctUntilChanged()

    /** Soma delta à quilometragem total acumulada do utilizador. */
    suspend fun incrementUserMileage(delta: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_TOTAL_USER_MILEAGE] ?: 0
            preferences[KEY_TOTAL_USER_MILEAGE] = current + delta
        }
    }

    /** Atualiza o nome de perfil do utilizador. */
    suspend fun updateUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    /** Atualiza o nome da garagem. */
    suspend fun updateGarageName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GARAGE_NAME] = name
        }
    }

    /** Ativa/desativa modo guest. Se ativar, remove token e email. */
    suspend fun setGuestMode(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_GUEST_MODE] = isGuest
            if (isGuest) {
                preferences.remove(KEY_AUTH_TOKEN)
                preferences.remove(KEY_USER_EMAIL)
            }
        }
    }

    /** Guarda token, email, nome e garagem. Força isGuestMode = false. */
    suspend fun saveAuth(token: String, email: String, name: String? = null, garageName: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_IS_GUEST_MODE] = false
            if (!name.isNullOrBlank()) preferences[KEY_USER_NAME] = name
            if (!garageName.isNullOrBlank()) preferences[KEY_GARAGE_NAME] = garageName
        }
    }

    /** Remove token e email. Volta a isGuestMode = true. */
    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN)
            preferences.remove(KEY_USER_EMAIL)
            preferences[KEY_IS_GUEST_MODE] = true
        }
    }

    /** Guarda o timestamp do último sync bem-sucedido. */
    suspend fun setLastSyncTimestamp(timestampMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestampMillis
        }
    }

    /** Adiciona o ID à lista de veículos apagados (separados por vírgula). Evita que o pull os restaure. */
    suspend fun markVehicleDeleted(vehicleId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_DELETED_VEHICLE_IDS] ?: ""
            val ids = current.split(",").filter { it.isNotBlank() }.toMutableSet()
            ids.add(vehicleId)
            preferences[KEY_DELETED_VEHICLE_IDS] = ids.joinToString(",")
        }
    }

    /** Devolve os IDs dos veículos apagados localmente como um Set. */
    suspend fun getDeletedVehicleIds(): Set<String> {
        val raw = context.dataStore.data.map { preferences ->
            preferences[KEY_DELETED_VEHICLE_IDS] ?: ""
        }.first()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    /** Guarda nome e garagem e marca hasCompletedOnboarding = true. */
    suspend fun completeOnboarding(userName: String, garageName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = userName
            preferences[KEY_GARAGE_NAME] = garageName
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = true
        }
    }

    /** Guarda ou remove o nome do ficheiro do avatar local. */
    suspend fun updateAvatarFileName(fileName: String?) {
        context.dataStore.edit { preferences ->
            if (fileName != null) {
                preferences[KEY_AVATAR_FILE_NAME] = fileName
            } else {
                preferences.remove(KEY_AVATAR_FILE_NAME)
            }
        }
    }

    /** Guarda o idioma escolhido ("SYSTEM", "PORTUGUESE", "ENGLISH"). */
    suspend fun updateAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language
        }
    }

    /** Guarda a unidade de distância ("SYSTEM", "KILOMETERS", "MILES"). */
    suspend fun updateDistanceUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISTANCE_UNIT] = unit
        }
    }

    /** Guarda a assinatura SHA-256 da sessão guest para o merge. */
    suspend fun setGuestDataSignature(signature: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_DATA_SIGNATURE] = signature
        }
    }

    /** Marca se é preciso fazer guest merge no próximo sync. */
    suspend fun setRequiresGuestMerge(required: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REQUIRES_GUEST_MERGE] = required
        }
    }

    /** Marca hasCompletedOnboarding = true (sem alterar nome/garagem). */
    suspend fun markOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = true
        }
    }

    /** Guarda ou remove o URL remoto do avatar. */
    suspend fun updateAvatarRemoteUrl(url: String?) {
        context.dataStore.edit { preferences ->
            if (url != null) {
                preferences[KEY_AVATAR_REMOTE_URL] = url
            } else {
                preferences.remove(KEY_AVATAR_REMOTE_URL)
            }
        }
    }

    /**
     * Apaga TODOS os dados do DataStore (usado ao eliminar conta).
     * Com isto, isGuestMode volta ao default (true) e o onboarding será
     * mostrado novamente no próximo arranque.
     */
    suspend fun clearAllUserData() {
        context.dataStore.edit { it.clear() }
    }
}
