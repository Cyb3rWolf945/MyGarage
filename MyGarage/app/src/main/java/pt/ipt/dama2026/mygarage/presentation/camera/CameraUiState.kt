package pt.ipt.dama2026.mygarage.presentation.camera

import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiResult

data class CameraUiState(
    val isCameraPermissionGranted: Boolean = false,
    val isCameraActive: Boolean = false,
    val detectedPlate: String? = null,
    val isPlateConfirmed: Boolean = false,
    val licensePlateApiResult: LicensePlateApiResult = LicensePlateApiResult.Idle,
    val showLookupResultDialog: Boolean = false
)