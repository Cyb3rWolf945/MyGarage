package pt.ipt.dama2026.mygarage.presentation.garage

data class GarageUiState(
    val garageName: String = "My Garage",
    val selectedImageUri: String? = null,
    val existingImageFileName: String? = null
)
