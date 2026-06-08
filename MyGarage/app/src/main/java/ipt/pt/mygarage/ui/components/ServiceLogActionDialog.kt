package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.ui.screens.servicelog.ServiceDialogMode
import ipt.pt.mygarage.ui.theme.MyGarageColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unified, multi-mode dialog for viewing, adding, or editing a service log.
 *
 * Its internal layout dynamically changes based on [dialogMode]:
 * - VIEW  → read-only summary with a Close button.
 * - ADD   → editable form with Save + Cancel buttons.
 * - EDIT  → editable form pre-populated with existing data + Save + Cancel.
 *
 * Visibility is driven by [dialogMode] — when it is [ServiceDialogMode.HIDDEN]
 * the dialog is not rendered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceLogActionDialog(
    dialogMode: ServiceDialogMode,
    selectedLog: ServiceLogEntity?,
    selectedLogParts: List<PartEntity>,
    // ── Form state (driven by ViewModel) ──────────────────────────────────
    serviceDate: String,
    description: String,
    mileage: String,
    selectedType: String,
    temporaryParts: List<PartEntity>,
    formErrors: Map<String, Int>,
    // ── Intents (ViewModel callbacks) ──────────────────────────────────────
    onDateChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onMileageChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onAddTemporaryPart: (String, Int, String?) -> Unit,
    onRemoveTemporaryPart: (String) -> Unit
) {
    if (dialogMode == ServiceDialogMode.HIDDEN) return

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // ── Local UI state (date picker, parts search, add-part mini-dialog) ──
    var showDatePicker by remember { mutableStateOf(false) }
    var partsSearchQuery by remember { mutableStateOf("") }
    var showAddPartDialog by remember { mutableStateOf(false) }

    val isEditable = dialogMode == ServiceDialogMode.ADD || dialogMode == ServiceDialogMode.EDIT
    val isViewOnly = dialogMode == ServiceDialogMode.VIEW

    // Shared text-field colors per DESIGN.md tonal layering
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MyGarageColors.primary,
        unfocusedBorderColor = MyGarageColors.surfaceContainerHigh,
        focusedLabelColor = MyGarageColors.primary,
        unfocusedLabelColor = MyGarageColors.onSurfaceVariant,
        focusedTextColor = MyGarageColors.onSurface,
        unfocusedTextColor = MyGarageColors.onSurface,
        focusedContainerColor = MyGarageColors.background,
        unfocusedContainerColor = MyGarageColors.background
    )

    // ── Date Picker sub-dialog ────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching {
                dateFormat.parse(serviceDate)?.time
            }.getOrNull() ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateChanged(dateFormat.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = MyGarageColors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MyGarageColors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Add-Part mini-dialog (revision type, editable modes only) ──────────
    if (showAddPartDialog) {
        AddPartDialogInline(
            onDismiss = { showAddPartDialog = false },
            onConfirm = { name, qty, ref ->
                onAddTemporaryPart(name, qty, ref)
                showAddPartDialog = false
            }
        )
    }

    // ── Main Dialog ───────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerLow)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                // ── VIEW mode: read-only layout ──────────────────────────
                isViewOnly -> {
                    val log = selectedLog
                    if (log != null) {
                        ViewField(label = "Date", value = log.date)
                        ViewField(label = "Description", value = log.description)
                        ViewField(label = "Mileage", value = log.mileage)
                        ViewField(label = "Service Type", value = log.type.uppercase())

                        // Parts used (if any)
                        if (selectedLogParts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PARTS USED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MyGarageColors.onSurfaceVariant
                            )
                            selectedLogParts.forEach { part ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MyGarageColors.surfaceContainerLowest)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = part.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MyGarageColors.onSurface
                                        )
                                        Row {
                                            Text(
                                                text = "Qty: ${part.quantity}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MyGarageColors.onSurfaceVariant
                                            )
                                            if (!part.reference.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Ref: ${part.reference}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MyGarageColors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── ADD / EDIT mode: editable form layout ──────────────────
                isEditable -> {
                    // Service Date — tap to open DatePickerDialog
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        OutlinedTextField(
                            value = serviceDate,
                            onValueChange = {},
                            label = { Text("Service Date") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Pick date",
                                    tint = MyGarageColors.onSurfaceVariant
                                )
                            },
                            supportingText = {
                                Text(
                                    "Tap to select date",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChanged,
                        label = { Text("Description") },
                        isError = formErrors.containsKey("description"),
                        supportingText = {
                            formErrors["description"]?.let { Text(stringResource(it)) }
                        },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mileage,
                        onValueChange = onMileageChanged,
                        label = { Text("Mileage at Service") },
                        isError = formErrors.containsKey("mileage"),
                        supportingText = {
                            formErrors["mileage"]?.let { Text(stringResource(it)) }
                        },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Service Type Chips
                    Text(
                        text = "SERVICE TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.onSurfaceVariant
                    )

                    val serviceTypes = listOf("regular", "revision", "Inspection")
                    val isAddMode = dialogMode == ServiceDialogMode.ADD
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        serviceTypes.forEach { type ->
                            val isSelected = type == selectedType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MyGarageColors.primary
                                        else MyGarageColors.surfaceContainerLow
                                    )
                                    .then(
                                        if (isAddMode) Modifier.clickable { onTypeChanged(type) }
                                        else Modifier
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MyGarageColors.surfaceContainerLowest
                                    else MyGarageColors.onSurface,
                                    modifier = if (!isAddMode && !isSelected)
                                        Modifier.alpha(0.4f) else Modifier
                                )
                            }
                        }
                    }

                    // Parts Used Section (revision type only)
                    if (selectedType == "revision") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PARTS USED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MyGarageColors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = partsSearchQuery,
                            onValueChange = { partsSearchQuery = it },
                            placeholder = { Text("Search parts...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MyGarageColors.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (partsSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { partsSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MyGarageColors.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filtered Parts List
                        val filteredParts = if (partsSearchQuery.isBlank()) {
                            temporaryParts
                        } else {
                            temporaryParts.filter {
                                it.name.contains(partsSearchQuery, ignoreCase = true)
                            }
                        }

                        if (filteredParts.isEmpty()) {
                            Text(
                                text = if (temporaryParts.isEmpty()) "No parts added yet."
                                else "No parts match \"$partsSearchQuery\".",
                                style = MaterialTheme.typography.bodySmall,
                                color = MyGarageColors.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MyGarageColors.surfaceContainerLow)
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                filteredParts.forEach { part ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MyGarageColors.surfaceContainerLowest)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = part.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MyGarageColors.onSurface
                                            )
                                            Text(
                                                text = "Qty: ${part.quantity}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MyGarageColors.onSurfaceVariant
                                            )
                                            if (!part.reference.isNullOrBlank()) {
                                                Text(
                                                    text = "Ref: ${part.reference}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MyGarageColors.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onRemoveTemporaryPart(part.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove ${part.name}",
                                                tint = MyGarageColors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Part Button
                        Button(
                            onClick = { showAddPartDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyGarageColors.surfaceContainerLow
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MyGarageColors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ADD PART",
                                style = MaterialTheme.typography.labelSmall,
                                color = MyGarageColors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Save Button ───────────────────────────────────────
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MyGarageColors.primary,
                            contentColor = MyGarageColors.surfaceContainerLowest
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (dialogMode == ServiceDialogMode.EDIT)
                                    R.string.action_update_service
                                else R.string.action_log_maintenance
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // ── Cancel Button ─────────────────────────────────────
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.action_cancel),
                            color = MyGarageColors.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Close button (VIEW mode only) ─────────────────────────────
            if (isViewOnly) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary,
                        contentColor = MyGarageColors.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.action_close),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ─── View-Mode Helper ────────────────────────────────────────────────────────

/**
 * Renders a label-value pair for the read-only VIEW dialog.
 * Applies the "N/A" alpha rule from DESIGN.md when [value] is blank.
 */
@Composable
private fun ViewField(label: String, value: String) {
    val displayValue = value.ifBlank { stringResource(id = R.string.not_available) }
    val isNA = value.isBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MyGarageColors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyLarge,
            color = MyGarageColors.onSurface,
            modifier = if (isNA) Modifier.alpha(0.5f) else Modifier
        )
    }
}

// ─── Inline Add-Part Mini-Dialog ─────────────────────────────────────────────

/**
 * Small dialog for adding a part while inside the main service-log dialog.
 * Follows the same "Mechanical Atelier" design system.
 */
@Composable
private fun AddPartDialogInline(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, reference: String?) -> Unit
) {
    var partName by remember { mutableStateOf("") }
    var partQuantity by remember { mutableStateOf("") }
    var partReference by remember { mutableStateOf("") }
    var partErrors by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerLowest)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ADD PART",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            val dialogTextFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.surfaceContainerHigh,
                focusedLabelColor = MyGarageColors.primary,
                unfocusedLabelColor = MyGarageColors.onSurfaceVariant,
                focusedTextColor = MyGarageColors.onSurface,
                unfocusedTextColor = MyGarageColors.onSurface,
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow
            )

            OutlinedTextField(
                value = partName,
                onValueChange = {
                    partName = it
                    if (partErrors.containsKey("partName")) partErrors = partErrors - "partName"
                },
                label = { Text("Part Name") },
                placeholder = { Text("e.g. Oil Filter, Brake Pads...") },
                isError = partErrors.containsKey("partName"),
                supportingText = { partErrors["partName"]?.let { Text(stringResource(it)) } },
                colors = dialogTextFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = partQuantity,
                onValueChange = { newValue ->
                    if (newValue.all { c -> c.isDigit() } && newValue.length <= 4) {
                        partQuantity = newValue
                        if (partErrors.containsKey("partQuantity")) partErrors = partErrors - "partQuantity"
                    }
                },
                label = { Text("Quantity") },
                placeholder = { Text("1") },
                isError = partErrors.containsKey("partQuantity"),
                supportingText = { partErrors["partQuantity"]?.let { Text(stringResource(it)) } },
                colors = dialogTextFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            OutlinedTextField(
                value = partReference,
                onValueChange = { partReference = it },
                label = { Text("Reference") },
                placeholder = { Text("e.g. OEM Part Number, SKU...") },
                colors = dialogTextFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = MyGarageColors.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val qty = partQuantity.toIntOrNull() ?: 0
                        val errors = mutableMapOf<String, Int>()
                        if (partName.isBlank()) errors["partName"] = R.string.error_field_required
                        if (partQuantity.isBlank() || qty <= 0) errors["partQuantity"] = R.string.error_field_required
                        partErrors = errors
                        if (errors.isEmpty()) {
                            onConfirm(partName.trim(), qty, partReference.trim().ifBlank { null })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary,
                        contentColor = MyGarageColors.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("ADD", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
