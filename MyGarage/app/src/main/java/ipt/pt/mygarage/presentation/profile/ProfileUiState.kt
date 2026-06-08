package ipt.pt.mygarage.presentation.profile

data class ProfileUiState(
    val userName: String = "Driver",
    val garageName: String = "My Garage",
    val isGuestMode: Boolean = true,
    val carsOwned: Int = 0,
    val totalMileage: Int = 0,
    val isEditing: Boolean = false,
    val formErrors: Map<String, Int> = emptyMap()
)
