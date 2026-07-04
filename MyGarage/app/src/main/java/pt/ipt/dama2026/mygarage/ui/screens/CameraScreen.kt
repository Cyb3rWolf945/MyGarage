package pt.ipt.dama2026.mygarage.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ipt.dama2026.mygarage.data.camera.LicensePlateAnalyzer
import pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateApiResult
import pt.ipt.dama2026.mygarage.presentation.camera.CameraUiState
import pt.ipt.dama2026.mygarage.presentation.camera.CameraViewModel
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Ecrã da câmara para leitura de matrículas via ML Kit e consulta SOAP. */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onVehicleDataReady: (pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateVehicleData) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CameraViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    CameraScreenContent(
        modifier = modifier,
        uiState = uiState,
        onPermissionRequested = {
            viewModel.onPermissionResult(it)
            if (it) {
                viewModel.onActivateCameraTapped()
            }
        },
        onActivateCameraTapped = viewModel::onActivateCameraTapped,
        onPlateDetected = viewModel::onPlateDetected,
        onConfirmPlate = viewModel::onConfirmPlate,
        onCancelPlate = viewModel::onCancelPlate,
        onResultDialogDismissed = viewModel::onResultDialogDismissed,
        onUseVehicleData = onVehicleDataReady
    )
}

@Composable
private fun CameraScreenContent(
    modifier: Modifier = Modifier,
    uiState: CameraUiState,
    onPermissionRequested: (Boolean) -> Unit,
    onActivateCameraTapped: () -> Unit,
    onPlateDetected: (String) -> Unit,
    onConfirmPlate: () -> Unit,
    onCancelPlate: () -> Unit,
    onResultDialogDismissed: () -> Unit,
    onUseVehicleData: (pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateVehicleData) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraExecutor = rememberCameraExecutor()
    val plateAnalyzer = remember(onPlateDetected) {
        LicensePlateAnalyzer(onPlateFound = onPlateDetected)
    }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }
    val activity = context as? android.app.Activity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isPermanentlyDenied = false
        } else {
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: true
            if (!shouldShowRationale) {
                isPermanentlyDenied = true
            }
        }
        onPermissionRequested(granted)
    }

    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    DisposableEffect(plateAnalyzer) {
        onDispose {
            plateAnalyzer.close()
        }
    }

    DisposableEffect(cameraProvider, uiState.isCameraPermissionGranted, uiState.isCameraActive, previewView) {
        val provider = cameraProvider
        if (provider == null || !uiState.isCameraPermissionGranted || !uiState.isCameraActive) {
            provider?.unbindAll()
            onDispose { }
        } else {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, plateAnalyzer)
                }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )

            onDispose {
                provider.unbindAll()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MyGarageColors.surfaceContainerLowest)
                    .clickable {
                        if (isPermanentlyDenied) {
                            isPermanentlyDenied = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else if (!uiState.isCameraPermissionGranted) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        } else if (!uiState.isCameraActive) {
                            onActivateCameraTapped()
                        }
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = uiState.isCameraPermissionGranted && uiState.isCameraActive,
                    label = "camera_active_crossfade"
                ) { isActive ->
                    if (isActive) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { previewView }
                        )
                    } else {
                        if (isPermanentlyDenied) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MyGarageColors.error.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(id = R.string.scanner_permanently_blocked),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyGarageColors.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = null,
                                    tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(id = R.string.scanner_tap_to_activate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyGarageColors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetectedPlateBanner(
                detectedPlate = uiState.detectedPlate,
                isConfirmed = uiState.isPlateConfirmed,
                isLoading = uiState.isLoading,
                onConfirmTapped = onConfirmPlate,
                onCancelTapped = onCancelPlate
            )

            if (uiState.showLookupResultDialog && uiState.licensePlateApiResult != null) {
                LicensePlateLookupResultDialog(
                    result = uiState.licensePlateApiResult!!,
                    onDismiss = onResultDialogDismissed,
                    onUseData = { vehicleData ->
                        onResultDialogDismissed()
                        onUseVehicleData(vehicleData)
                    }
                )
            }
        }
    }
}

@Composable
private fun DetectedPlateBanner(
    detectedPlate: String?,
    isConfirmed: Boolean,
    isLoading: Boolean = false,
    onConfirmTapped: () -> Unit,
    onCancelTapped: () -> Unit
) {
    if (detectedPlate == null) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MyGarageColors.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MyGarageColors.primary, RoundedCornerShape(99.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.scanner_plate_detected_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MyGarageColors.onSurface
                    )
                    Text(
                        text = detectedPlate,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MyGarageColors.onSurface
                    )
                }
            }

            if (!isConfirmed) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCancelTapped,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.btn_cancel))
                    }
                    Button(
                        onClick = onConfirmTapped,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(id = R.string.btn_confirm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicensePlateLookupResultDialog(
    result: LicensePlateApiResult,
    onDismiss: () -> Unit,
    onUseData: (pt.ipt.dama2026.mygarage.domain.licenseplates.LicensePlateVehicleData) -> Unit = {}
) {
    when (result) {
        is LicensePlateApiResult.Success -> {
            AlertDialog(
                title = { Text(stringResource(id = R.string.dialog_vehicle_found)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(id = R.string.scanner_plate_label_colon) + " ${result.vehicleData.plate}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        result.vehicleData.vehicleModel?.let {
                            Text(
                                stringResource(id = R.string.spec_model_label) + ": $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        result.vehicleData.year?.let {
                            Text(
                                stringResource(id = R.string.spec_year) + ": $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        result.vehicleData.fuelType?.let {
                            Text(
                                stringResource(id = R.string.spec_fuel_type) + ": $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        result.vehicleData.engineCapacity?.let {
                            Text(
                                stringResource(id = R.string.spec_engine_capacity) + ": $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.btn_cancel))
                    }
                },
                confirmButton = {
                    Button(onClick = { onUseData(result.vehicleData) }) {
                        Text(stringResource(id = R.string.btn_use_data))
                    }
                },
                onDismissRequest = onDismiss
            )
        }
        is LicensePlateApiResult.Error -> {
            AlertDialog(
                title = { Text(stringResource(id = R.string.error_lookup_failed)) },
                text = {
                    Text(
                        when (result.errorType) {
                            pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.NETWORK_ERROR ->
                                stringResource(id = R.string.error_network_connection)
                            pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.INVALID_PLATE ->
                                stringResource(id = R.string.error_invalid_plate)
                            pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.NOT_FOUND ->
                                stringResource(id = R.string.error_vehicle_not_found)
                            pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.API_UNAVAILABLE ->
                                stringResource(id = R.string.error_api_unavailable)
                            pt.ipt.dama2026.mygarage.domain.licenseplates.ErrorType.UNKNOWN ->
                                stringResource(id = R.string.error_unknown)
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = onDismiss) {
                        Text(stringResource(id = R.string.btn_ok))
                    }
                },
                onDismissRequest = onDismiss
            )
        }
        else -> {}
    }
}

@Composable
private fun rememberCameraExecutor(): ExecutorService {
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(executor) {
        onDispose {
            executor.shutdown()
        }
    }

    return executor
}
