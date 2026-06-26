package pt.ipt.dama2026.mygarage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices
import pt.ipt.dama2026.mygarage.ui.components.ServiceLogActionDialog
import pt.ipt.dama2026.mygarage.ui.screens.servicelog.ServiceDialogMode
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors

/**
 * Maps a raw service type key (stored in Room as lowercase English, e.g. "regular")
 * to a localized, display-ready label by using string resources.
 */
@Composable
private fun serviceTypeLabel(type: String): String = when (type.lowercase()) {
    "revision"   -> stringResource(R.string.service_type_revision)
    "inspection" -> stringResource(R.string.service_type_inspection)
    else         -> stringResource(R.string.service_type_regular)
}

// ─────────────────────────────────────────────────────────────────────────────
//  ServiceScreen — refactored with unified multi-mode dialog
//  Follows RULES.md (UDF, formErrors, "N/A" alpha rule) and
//  DESIGN.md (tonal layering, no 1px borders, surface_container_low dialogs)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServiceScreen(
    vehicles: List<VehicleEntity>,
    selectedVehicleId: String?,
    selectedVehicleWithServices: VehicleWithServices?,
    temporaryParts: List<PartEntity>,
    onVehicleSelected: (String) -> Unit,
    onAddTemporaryPart: (String, Int, String?) -> Unit,
    onRemoveTemporaryPart: (String) -> Unit,
    // ── Unified Dialog State ────────────────────────────────────────────
    dialogMode: ServiceDialogMode = ServiceDialogMode.HIDDEN,
    selectedLog: ServiceLogEntity? = null,
    selectedLogParts: List<PartEntity> = emptyList(),
    // ── Form state driven by ViewModel (Add / Edit modes) ───────────────
    formErrors: Map<String, Int> = emptyMap(),
    onFieldChanged: (String) -> Unit = {},
    serviceDate: String = "",
    description: String = "",
    mileage: String = "",
    selectedType: String = "regular",
    onDateChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
    onMileageChanged: (String) -> Unit = {},
    onTypeChanged: (String) -> Unit = {},
    // ── Dialog Intents ──────────────────────────────────────────────────
    onAddFabClicked: () -> Unit = {},
    onLogClicked: (ServiceLogEntity) -> Unit = {},
    onSave: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    // ── Long-Press Options Menu (UDF) ───────────────────────────────────
    selectedLogForOptions: ServiceLogEntity? = null,
    onLogLongPressed: (ServiceLogEntity) -> Unit = {},
    onDismissOptionsMenu: () -> Unit = {},
    onSelectEdit: (ServiceLogEntity) -> Unit = {},
    onSelectDelete: (ServiceLogEntity) -> Unit = {},
    // ── Delete Confirmation Dialog (UDF) ────────────────────────────────
    logToDelete: ServiceLogEntity? = null,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDeleteLog: () -> Unit = {},
    resolvedDistanceUnit: String = "MILES",
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MyGarageColors.background,
        floatingActionButton = {
            AnimatedVisibility(
                visible = vehicles.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAddFabClicked,
                    containerColor = MyGarageColors.primary,
                    contentColor = MyGarageColors.surfaceContainerLowest
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.dialog_service_add_part)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── No-vehicle empty state ──────────────────────────────────
            if (vehicles.isEmpty()) {
                item(key = "no_vehicle_empty") {
                    Crossfade(targetState = true, label = "no_vehicle_crossfade") {
                        NoVehicleEmptyState(
                            modifier = Modifier.fillParentMaxHeight()
                        )
                    }
                }
            }

            // ── Vehicle Selector Chips ──────────────────────────────────
            if (vehicles.isNotEmpty()) {
                item(key = "vehicle_chips") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(vehicles) { vehicle ->
                            val isSelected = vehicle.id == selectedVehicleId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) MyGarageColors.primary
                                        else MyGarageColors.surfaceContainerLowest
                                    )
                                    .clickable { onVehicleSelected(vehicle.id) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = vehicle.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MyGarageColors.surfaceContainerLowest
                                    else MyGarageColors.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ── Timeline Items (or premium empty state) ─────────────────
            if (vehicles.isNotEmpty()) {
                val services = selectedVehicleWithServices?.services ?: emptyList()
                item(key = "timeline_content") {
                    AnimatedVisibility(
                        visible = services.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ServiceHistoryEmptyState()
                    }
                    AnimatedVisibility(
                        visible = services.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            for ((index, log) in services.withIndex()) {
                                val typeLabel = serviceTypeLabel(log.type)
                                TimelineItem(
                                    title = log.description,
                                    subtitle = stringResource(
                                        R.string.timeline_subtitle,
                                        log.mileage,
                                        log.date,
                                        typeLabel
                                    ),
                                    isLast = index == services.lastIndex,
                                    onClick = { onLogClicked(log) },
                                    onLongClick = { onLogLongPressed(log) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Bottom spacer so FAB does not obscure last item ─────────
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // ── Options Bottom Sheet ───────────────────────────────────────────────
    if (selectedLogForOptions != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = onDismissOptionsMenu,
            sheetState = sheetState,
            containerColor = MyGarageColors.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Edit option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                selectedLogForOptions?.let { onSelectEdit(it) }
                            },
                            onLongClick = null
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MyGarageColors.onSurface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.action_edit),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MyGarageColors.onSurface
                    )
                }

                HorizontalDivider(
                    color = MyGarageColors.surfaceContainerHigh,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                selectedLogForOptions?.let { onSelectDelete(it) }
                            },
                            onLongClick = null
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
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

    // ── Unified Service Log Dialog ─────────────────────────────────────────
    ServiceLogActionDialog(
        dialogMode = dialogMode,
        selectedLog = selectedLog,
        selectedLogParts = selectedLogParts,
        serviceDate = serviceDate,
        description = description,
        mileage = mileage,
        selectedType = selectedType,
        temporaryParts = temporaryParts,
        formErrors = formErrors,
        onDateChanged = onDateChanged,
        onDescriptionChanged = onDescriptionChanged,
        onMileageChanged = onMileageChanged,
        onTypeChanged = onTypeChanged,
        onSave = onSave,
        onDismiss = onDismissDialog,
        onAddTemporaryPart = onAddTemporaryPart,
        onRemoveTemporaryPart = onRemoveTemporaryPart,
        resolvedDistanceUnit = resolvedDistanceUnit
    )

    // ── Delete Confirmation Dialog ─────────────────────────────────────────
    if (logToDelete != null) {
        DeleteServiceDialog(
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDeleteLog
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TimelineItem — automotive vertical timeline per DESIGN.md
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    isLast: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { onLongClick?.invoke() }
                    )
                } else {
                    Modifier
                }
            )
    ) {
        // ── Timeline node & line ───────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MyGarageColors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MyGarageColors.primary)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MyGarageColors.primary)
                )
            }
        }

        // ── Text content ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ServiceHistoryEmptyState — premium 'clean log' placeholder per DESIGN.md
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ServiceHistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MyGarageColors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(id = R.string.service_history_empty_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.service_history_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  NoVehicleEmptyState — shown when the user has no registered vehicles
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoVehicleEmptyState(modifier: Modifier = Modifier) {
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
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.service_no_vehicle_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DeleteServiceDialog — "Mechanical Atelier" design per DESIGN.md
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeleteServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerLow)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.delete_service_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MyGarageColors.onSurface
            )

            Text(
                text = stringResource(id = R.string.delete_service_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(id = R.string.action_cancel),
                        color = MyGarageColors.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stringResource(id = R.string.action_delete),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
