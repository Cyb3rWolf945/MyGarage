package ipt.pt.mygarage.data.model

data class UserPreferences(
    val userName: String = "Driver",
    val garageName: String = "My Garage",
    val isGuestMode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val avatarFileName: String? = null
)
