package pt.ipt.dama2026.mygarage.presentation.profile

data class ProfileUiState(
    val userName: String = "Driver",
    val garageName: String = "My Garage",
    val isGuestMode: Boolean = true,
    val carsOwned: Int = 0,
    val totalMileage: Int = 0,
    val isEditing: Boolean = false,
    val formErrors: Map<String, Int> = emptyMap(),
    val avatarFileName: String? = null,
    val appLanguage: String = "SYSTEM",
    val distanceUnit: String = "SYSTEM",
    val resolvedDistanceUnit: String = "KILOMETERS",
    val userEmail: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val isUploadingAvatar: Boolean = false,
    val avatarUploadError: String? = null,
    val avatarRemoteUrl: String? = null
)
