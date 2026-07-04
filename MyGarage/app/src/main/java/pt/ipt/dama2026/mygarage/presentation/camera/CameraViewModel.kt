package pt.ipt.dama2026.mygarage.presentation.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiResult
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel do ecrã da câmara para leitura de matrículas.
 *
 * Fluxo:
 * 1. User ativa câmara → LicensePlateAnalyzer deteta matrícula.
 * 2. onPlateDetected → guarda matrícula e para câmara.
 * 3. User confirma → lookupVehicle chama API SOAP.
 * 4. Resultado mostrado em diálogo com opção de voltar a tentar.
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val licensePlateApiService: LicensePlateApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /** Guarda o resultado do pedido de permissão da câmara. */
    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            isCameraPermissionGranted = granted,
            isCameraActive = if (granted) _uiState.value.isCameraActive else false
        )
    }

    /** Ativa a câmara (se a permissão já tiver sido concedida). */
    fun onActivateCameraTapped() {
        if (!_uiState.value.isCameraPermissionGranted) {
            return
        }

        _uiState.value = _uiState.value.copy(isCameraActive = true)
    }

    /** Chamado pelo LicensePlateAnalyzer quando uma matrícula é detetada. */
    fun onPlateDetected(plate: String) {
        if (_uiState.value.isPlateConfirmed) return
        _uiState.value = _uiState.value.copy(
            detectedPlate = plate,
            isPlateConfirmed = false,
            licensePlateApiResult = null
        )
    }

    /** User confirmou a matrícula. Chama a API para obter dados do veículo. */
    fun onConfirmPlate() {
        val plate = _uiState.value.detectedPlate ?: return

        _uiState.value = _uiState.value.copy(
            isPlateConfirmed = true,
            isLoading = true
        )
        fetchCarInfo(plate)
    }

    /** User cancelou — limpa a matrícula detetada e volta a ativar a câmara. */
    fun onCancelPlate() {
        _uiState.value = _uiState.value.copy(
            detectedPlate = null,
            isPlateConfirmed = false,
            licensePlateApiResult = null,
            showLookupResultDialog = false
        )
    }

    /** Fecha o diálogo de resultado da consulta. */
    fun onResultDialogDismissed() {
        _uiState.value = _uiState.value.copy(showLookupResultDialog = false)
    }

    /** Chama a API SOAP e atualiza o estado com o resultado (sucesso ou erro). */
    private fun fetchCarInfo(plate: String) {
        viewModelScope.launch {
            try {
                val result = licensePlateApiService.lookupVehicle(plate)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    licensePlateApiResult = result,
                    showLookupResultDialog = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching vehicle info", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    licensePlateApiResult = LicensePlateApiResult.Error(
                        e.message ?: "Unknown error",
                        pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.UNKNOWN
                    ),
                    showLookupResultDialog = true
                )
            }
        }
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}