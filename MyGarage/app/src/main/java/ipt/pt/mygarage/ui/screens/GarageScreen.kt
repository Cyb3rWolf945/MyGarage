package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.components.VehicleCard
import ipt.pt.mygarage.ui.components.VehicleEditDialog
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * Screen displaying the list of all registered vehicles with insertion capabilities.
 */
@Composable
fun GarageScreen(
    vehicles: List<VehicleEntity>,
    onVehicleClick: (String) -> Unit,
    onAddVehicleClick: (VehicleEntity) -> Unit,
    formErrors: Map<String, Int> = emptyMap(),
    onFieldChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        VehicleEditDialog(
            vehicle = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newVehicle ->
                onAddVehicleClick(newVehicle)
                showAddDialog = false
            },
            formErrors = formErrors,
            onFieldChanged = onFieldChanged
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
                .padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.nav_garage),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No vehicles in your garage. Tap '+' to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MyGarageColors.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp), // Extra padding to clear FAB
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vehicles) { vehicle ->
                        val isBmw = vehicle.name.contains("BMW", ignoreCase = true)
                        VehicleCard(
                            model = vehicle.name,
                            plate = vehicle.plate,
                            status = if (isBmw) "IN SERVICE" else "READY",
                            statusColor = if (isBmw) MyGarageColors.onSurfaceVariant else MyGarageColors.primary,
                            onClick = { onVehicleClick(vehicle.id) }
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
