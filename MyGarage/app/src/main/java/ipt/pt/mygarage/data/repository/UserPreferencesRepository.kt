package ipt.pt.mygarage.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ipt.pt.mygarage.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_GARAGE_NAME = stringPreferencesKey("garage_name")
        val KEY_IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            userName = preferences[KEY_USER_NAME] ?: "Driver",
            garageName = preferences[KEY_GARAGE_NAME] ?: "My Garage",
            isGuestMode = preferences[KEY_IS_GUEST_MODE] ?: true,
            hasCompletedOnboarding = preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false
        )
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
}
