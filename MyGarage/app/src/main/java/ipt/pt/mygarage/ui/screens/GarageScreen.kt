package ipt.pt.mygarage.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.components.DeleteConfirmationDialog
import ipt.pt.mygarage.ui.components.VehicleCard
import ipt.pt.mygarage.ui.components.VehicleEditDialog
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * Screen displaying the list of all registered vehicles with long-press options
 * and add/edit/delete capabilities via Unidirectional Data Flow (UDF).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    vehicles: List<VehicleEntity>,
    onVehicleClick: (String) -> Unit,
    onAddVehicleClick: (VehicleEntity) -> Unit,
    onDeleteVehicle: (VehicleEntity) -> Unit = {},
    showDeleteConfirmation: Boolean = false,
    vehicleToDelete: VehicleEntity? = null,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    formErrors: Map<String, Int> = emptyMap(),
    onFieldChanged: (String) -> Unit = {},
    // ── Long-Press Options Menu (UDF) ──────────────────────────────────────
    selectedVehicleForOptions: VehicleEntity? = null,
    onVehicleLongPressed: (VehicleEntity) -> Unit = {},
    onDismissOptionsMenu: () -> Unit = {},
    onSelectEdit: (VehicleEntity) -> Unit = {},
    onSelectDelete: (VehicleEntity) -> Unit = {},
    // ── Edit Dialog State (UDF) ────────────────────────────────────────────
    vehicleToEdit: VehicleEntity? = null,
    onDismissEditDialog: () -> Unit = {},
    onConfirmEdit: (VehicleEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // ── Add / Edit Dialog ──────────────────────────────────────────────────
    val isEditDialogVisible = vehicleToEdit != null || showAddDialog

    if (isEditDialogVisible) {
        VehicleEditDialog(
            vehicle = vehicleToEdit,
            onDismiss = {
                showAddDialog = false
                onDismissEditDialog()
            },
            onConfirm = { updatedVehicle ->
                if (vehicleToEdit != null) {
                    onConfirmEdit(updatedVehicle)
                    onDismissEditDialog()
                } else {
                    onAddVehicleClick(updatedVehicle)
                    showAddDialog = false
                }
            },
            formErrors = formErrors,
            onFieldChanged = onFieldChanged
        )
    }

    // ── Delete Confirmation Dialog ─────────────────────────────────────────
    if (showDeleteConfirmation && vehicleToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp)
        ) {
            Crossfade(targetState = vehicles.isEmpty(), label = "garage_empty_crossfade") { isEmpty ->
                if (isEmpty) {
                    GarageEmptyState(
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(vehicles) { vehicle ->
                            VehicleCard(
                                model = vehicle.name,
                                plate = vehicle.plate,
                                onClick = { onVehicleClick(vehicle.id) },
                                onLongClick = { onVehicleLongPressed(vehicle) }
                            )
                        }
                    }
                }
            }
        }

    // ── Long-Press Options Modal Bottom Sheet ──────────────────────────
    if (selectedVehicleForOptions != null) {
        val sheetState = rememberModalBottomSheetState()
        val vehicle = selectedVehicleForOptions!!

        ModalBottomSheet(
            onDismissRequest = onDismissOptionsMenu,
            sheetState = sheetState,
            containerColor = MyGarageColors.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Edit option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectEdit(vehicle)
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.action_edit),
                        tint = MyGarageColors.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.action_edit),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MyGarageColors.onSurface
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MyGarageColors.outlineVariant
                )

                // Delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectDelete(vehicle)
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.action_delete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MyGarageColors.primary,
            contentColor = MyGarageColors.surfaceContainerLowest,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Vehicle"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GarageEmptyState — premium placeholder per DESIGN.md
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GarageEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_garage),
                contentDescription = null,
                tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.garage_empty_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MyGarageColors.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.garage_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
