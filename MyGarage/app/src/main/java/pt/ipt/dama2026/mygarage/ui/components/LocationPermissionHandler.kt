package pt.ipt.dama2026.mygarage.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.core.app.ActivityCompat
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors

private const val TAG = "MyGarage.Location"

/**
 * Holds location-permission state for the UI to consume.
 *
 * @param isGranted Whether location permission is currently held.
 * @param showSettingsDialog Whether the "Go to Settings" dialog should be visible.
 * @param launchPermissionRequest Always launches the OS permission prompt first.
 * @param dismissSettingsDialog Closes the settings-redirect dialog.
 */
class LocationPermissionState(
    val isGranted: Boolean,
    val showSettingsDialog: Boolean,
    val launchPermissionRequest: () -> Unit,
    val dismissSettingsDialog: () -> Unit
)

/**
 * Composable that remembers location-permission state.
 *
 * @param onGranted Called when the user grants location permission via the OS prompt.
 */

@Composable
fun rememberLocationPermissionHandler(
    onGranted: () -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var isGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permission result: $permissions")
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            Log.d(TAG, "Permission GRANTED → calling onGranted()")
            isGranted = true
            showSettingsDialog = false
            onGranted()
        } else {
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: true
            Log.d(TAG, "Permission DENIED, shouldShowRationale=$shouldShowRationale")
            if (!shouldShowRationale) {
                Log.d(TAG, "Permanently denied → showSettingsDialog=true")
                showSettingsDialog = true
            }
        }
    }

    val launchPermissionRequest: () -> Unit = {
        Log.d(TAG, "launchPermissionRequest() → launching OS permission prompt")
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    return LocationPermissionState(
        isGranted = isGranted,
        showSettingsDialog = showSettingsDialog,
        launchPermissionRequest = launchPermissionRequest,
        dismissSettingsDialog = { showSettingsDialog = false }
    )
}

/**
 * Premium dialog shown when the user has permanently denied location permission.
 * The user can dismiss it or tap "Open Settings" to navigate to the app's system settings.
 *
 * @param showDialog Controls visibility of the dialog.
 * @param onDismiss Sets [showDialog] to false (wired to Cancel button and back-press).
 * @param onOpenSettings Opens the Android OS Application Settings for this app.
 */
@Composable
fun LocationPermanentDenialDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (!showDialog) return

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerLowest)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MyGarageColors.error.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.location_permission_required_title),
                style = MaterialTheme.typography.titleMedium,
                color = MyGarageColors.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.location_permission_required_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MyGarageColors.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.action_cancel).uppercase())
            }
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyGarageColors.primary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.location_open_settings).uppercase(),
                    color = MyGarageColors.surfaceContainerLowest
                )
            }
        }
    }
}
