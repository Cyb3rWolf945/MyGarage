package pt.ipt.dama2026.mygarage.presentation.camera

import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiResult

/**
 * Estado do ecrã da câmara (leitura de matrículas).
 *
 * Fluxo:
 * 1. isCameraPermissionGranted → mostra câmara.
 * 2. LicensePlateAnalyzer deteta matrícula → detectedPlate.
 * 3. Utilizador confirma → isPlateConfirmed = true.
 * 4. API consulta matrícula → isLoading → licensePlateApiResult.
 * 5. Resultado mostrado em diálogo → showLookupResultDialog.
 */
data class CameraUiState(
    val isCameraPermissionGranted: Boolean = false,
    val isCameraActive: Boolean = false,
    val detectedPlate: String? = null,
    val isPlateConfirmed: Boolean = false,
    val isLoading: Boolean = false,
    val licensePlateApiResult: LicensePlateApiResult? = null,
    val showLookupResultDialog: Boolean = false
)