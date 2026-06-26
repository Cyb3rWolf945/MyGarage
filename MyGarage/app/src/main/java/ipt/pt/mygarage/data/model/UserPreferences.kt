package ipt.pt.mygarage.data.model

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
