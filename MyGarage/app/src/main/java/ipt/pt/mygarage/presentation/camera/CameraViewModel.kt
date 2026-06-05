package ipt.pt.mygarage.presentation.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

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
        _uiState.value = _uiState.value.copy(detectedPlate = plate)
        fetchCarInfo(plate)
    }

    fun fetchCarInfo(plate: String) {
        Log.d(TAG, "fetchCarInfo called for plate=$plate")
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}