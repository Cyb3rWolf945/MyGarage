package pt.ipt.dama2026.mygarage.presentation.garage

/** Estado do ecrã da garagem (lista de veículos). */
data class GarageUiState(
    val garageName: String = "My Garage",
    val selectedImageUri: String? = null,
    val existingImageFileName: String? = null
)
