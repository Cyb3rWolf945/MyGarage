package ipt.pt.mygarage.presentation.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipt.pt.mygarage.domain.licenseplates.LicensePlateApiResult
import ipt.pt.mygarage.domain.licenseplates.LicensePlateApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    private val licensePlateApiService: LicensePlateApiService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            isCameraPermissionGranted = granted,
            isCameraActive = if (granted) _uiState.value.isCameraActive else false
        )
    }

    fun onActivateCameraTapped() {
        if (!_uiState.value.isCameraPermissionGranted) {
            return
        }

        _uiState.value = _uiState.value.copy(isCameraActive = true)
    }

    fun onPlateDetected(plate: String) {
        _uiState.value = _uiState.value.copy(
            detectedPlate = plate,
            isPlateConfirmed = false,
            licensePlateApiResult = LicensePlateApiResult.Idle
        )
    }

    fun onConfirmPlate() {
        val plate = _uiState.value.detectedPlate ?: return
        if (licensePlateApiService == null) {
            Log.w(TAG, "License plate API service not initialized")
            return
        }

        _uiState.value = _uiState.value.copy(
            isPlateConfirmed = true,
            licensePlateApiResult = LicensePlateApiResult.Loading
        )
        fetchCarInfo(plate)
    }

    fun onCancelPlate() {
        _uiState.value = _uiState.value.copy(
            detectedPlate = null,
            isPlateConfirmed = false,
            licensePlateApiResult = LicensePlateApiResult.Idle,
            showLookupResultDialog = false
        )
    }

    fun onResultDialogDismissed() {
        _uiState.value = _uiState.value.copy(showLookupResultDialog = false)
    }

    private fun fetchCarInfo(plate: String) {
        if (licensePlateApiService == null) {
            return
        }

        viewModelScope.launch {
            try {
                val result = licensePlateApiService.lookupVehicle(plate)
                _uiState.value = _uiState.value.copy(
                    licensePlateApiResult = result,
                    showLookupResultDialog = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching vehicle info", e)
                _uiState.value = _uiState.value.copy(
                    licensePlateApiResult = LicensePlateApiResult.Error(
                        e.message ?: "Unknown error",
                        ipt.pt.mygarage.domain.licenseplates.ErrorType.UNKNOWN
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