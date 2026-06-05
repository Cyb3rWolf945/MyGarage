package ipt.pt.mygarage.presentation.camera

data class CameraUiState(
    val isCameraPermissionGranted: Boolean = false,
    val isCameraActive: Boolean = false,
    val detectedPlate: String? = null
)