package ipt.pt.mygarage.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ipt.pt.mygarage.domain.camera.LicensePlateAnalyzer
import ipt.pt.mygarage.presentation.camera.CameraUiState
import ipt.pt.mygarage.presentation.camera.CameraViewModel
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
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
        onPlateDetected = viewModel::onPlateDetected
    )
}

@Composable
private fun CameraScreenContent(
    modifier: Modifier = Modifier,
    uiState: CameraUiState,
    onPermissionRequested: (Boolean) -> Unit,
    onActivateCameraTapped: () -> Unit,
    onPlateDetected: (String) -> Unit
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // The OS PackageManager securely persists permission state, so this callback is the source of truth.
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
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "PLATE SCANNER COCKPIT",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.nav_camera),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MyGarageColors.surfaceContainerLowest)
                    .clickable {
                        if (!uiState.isCameraPermissionGranted) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        } else if (!uiState.isCameraActive) {
                            onActivateCameraTapped()
                        }
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isCameraPermissionGranted && uiState.isCameraActive -> {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { previewView }
                        )
                    }

                    uiState.isCameraPermissionGranted -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCANNER READY",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MyGarageColors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to start live plate recognition",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MyGarageColors.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCANNER OFFLINE",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MyGarageColors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to initialize vehicle tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MyGarageColors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetectedPlateBanner(detectedPlate = uiState.detectedPlate)
        }
    }
}

@Composable
private fun DetectedPlateBanner(
    detectedPlate: String?
) {
    if (detectedPlate == null) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MyGarageColors.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MyGarageColors.primary, RoundedCornerShape(99.dp))
            )
            Column {
                Text(
                    text = "Plate detected",
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
