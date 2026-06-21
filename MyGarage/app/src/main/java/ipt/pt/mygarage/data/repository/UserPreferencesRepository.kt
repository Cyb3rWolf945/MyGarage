package ipt.pt.mygarage.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ipt.pt.mygarage.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_GARAGE_NAME = stringPreferencesKey("garage_name")
        val KEY_IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_AVATAR_FILE_NAME = stringPreferencesKey("avatar_file_name")
        val KEY_TOTAL_USER_MILEAGE = intPreferencesKey("total_user_mileage")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_DISTANCE_UNIT = stringPreferencesKey("distance_unit")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            userName = preferences[KEY_USER_NAME] ?: "Driver",
            garageName = preferences[KEY_GARAGE_NAME] ?: "My Garage",
            isGuestMode = preferences[KEY_IS_GUEST_MODE] ?: true,
            hasCompletedOnboarding = preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false,
            avatarFileName = preferences[KEY_AVATAR_FILE_NAME],
            totalUserMileage = preferences[KEY_TOTAL_USER_MILEAGE] ?: 0,
            appLanguage = preferences[KEY_APP_LANGUAGE] ?: "SYSTEM",
            distanceUnit = preferences[KEY_DISTANCE_UNIT] ?: "SYSTEM"
        )
    }

    /** Exposes the raw distance unit preference ("SYSTEM", "KILOMETERS", or "MILES"). */
    val distanceUnitFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISTANCE_UNIT] ?: "SYSTEM"
    }.distinctUntilChanged()

    /** Exposes the running total of miles driven by the user across all vehicles. */
    val totalUserMileageFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOTAL_USER_MILEAGE] ?: 0
    }.distinctUntilChanged()

    /** Adds [delta] miles to the user's lifetime driven total. */
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
        }
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
}
