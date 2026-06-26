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

    suspend fun incrementUserMileage(delta: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_TOTAL_USER_MILEAGE] ?: 0
            preferences[KEY_TOTAL_USER_MILEAGE] = current + delta
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun updateGarageName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GARAGE_NAME] = name
        }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_GUEST_MODE] = isGuest
            if (isGuest) {
                preferences.remove(KEY_AUTH_TOKEN)
                preferences.remove(KEY_USER_EMAIL)
            }
        }
    }

    suspend fun saveAuth(token: String, email: String, name: String? = null, garageName: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_IS_GUEST_MODE] = false
            if (!name.isNullOrBlank()) preferences[KEY_USER_NAME] = name
            if (!garageName.isNullOrBlank()) preferences[KEY_GARAGE_NAME] = garageName
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN)
            preferences.remove(KEY_USER_EMAIL)
            preferences[KEY_IS_GUEST_MODE] = true
            // Note: hasCompletedOnboarding is NOT cleared - user should not see onboarding again
        }
    }

    suspend fun setLastSyncTimestamp(timestampMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestampMillis
        }
    }

    suspend fun markVehicleDeleted(vehicleId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_DELETED_VEHICLE_IDS] ?: ""
            val ids = current.split(",").filter { it.isNotBlank() }.toMutableSet()
            ids.add(vehicleId)
            preferences[KEY_DELETED_VEHICLE_IDS] = ids.joinToString(",")
        }
    }

    suspend fun getDeletedVehicleIds(): Set<String> {
        val raw = context.dataStore.data.map { preferences ->
            preferences[KEY_DELETED_VEHICLE_IDS] ?: ""
        }.first()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    suspend fun completeOnboarding(userName: String, garageName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = userName
            preferences[KEY_GARAGE_NAME] = garageName
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun updateAvatarFileName(fileName: String?) {
        context.dataStore.edit { preferences ->
            if (fileName != null) {
                preferences[KEY_AVATAR_FILE_NAME] = fileName
            } else {
                preferences.remove(KEY_AVATAR_FILE_NAME)
            }
        }
    }

    suspend fun updateAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language
        }
    }

    suspend fun updateDistanceUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISTANCE_UNIT] = unit
        }
    }

    suspend fun setGuestDataSignature(signature: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_DATA_SIGNATURE] = signature
        }
    }

    suspend fun setRequiresGuestMerge(required: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REQUIRES_GUEST_MERGE] = required
        }
    }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = true
        }
    }

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
     * Clears all user data from DataStore. Used after account deletion.
     * Note: hasCompletedOnboarding is intentionally NOT cleared here —
     * the app will show onboarding again because isGuestMode becomes true
     * and hasCompletedOnboarding is set to false.
     */
    suspend fun clearAllUserData() {
        context.dataStore.edit { it.clear() }
    }
}
