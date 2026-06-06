package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.ui.theme.MyGarageColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Screen displaying selectable vehicles, their service history timeline, and a form to log new services.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    vehicles: List<VehicleEntity>,
    selectedVehicleId: String?,
    selectedVehicleWithServices: VehicleWithServices?,
    temporaryParts: List<PartEntity>,
    onVehicleSelected: (String) -> Unit,
    onLogService: (ServiceLogEntity) -> Unit,
    onLogServiceWithParts: (ServiceLogEntity) -> Unit,
    onAddTemporaryPart: (String, Int, String?) -> Unit,
    onRemoveTemporaryPart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Form inputs state
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val todayDateStr = remember { dateFormat.format(Date()) }
    var serviceDate by remember { mutableStateOf(todayDateStr) }
    var description by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    
    val serviceTypes = listOf("regular", "revision", "Inspection")
    var selectedType by remember { mutableStateOf("regular") }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            dateFormat.parse(serviceDate)?.time
        }.getOrNull() ?: System.currentTimeMillis()
    )

    // Parts Used: search query and add-part dialog state
    var partsSearchQuery by remember { mutableStateOf("") }
    var showAddPartDialog by remember { mutableStateOf(false) }

    // When the selected vehicle changes, pre-populate its current mileage as default for the service log
    LaunchedEffect(selectedVehicleId, selectedVehicleWithServices) {
        selectedVehicleWithServices?.vehicle?.mileage?.let {
            mileage = it
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        serviceDate = dateFormat.format(Date(millis))
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {            Spacer(modifier = Modifier.height(16.dp))            

            Text(
                text = stringResource(id = R.string.nav_service),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(vehicles) { vehicle ->
                    val isSelected = vehicle.id == selectedVehicleId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MyGarageColors.primary else MyGarageColors.surfaceContainerLowest)
                            .clickable { onVehicleSelected(vehicle.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MyGarageColors.surfaceContainerLowest else MyGarageColors.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Header
            Text(
                text = "SERVICE HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Items
            val services = selectedVehicleWithServices?.services ?: emptyList()
            if (services.isEmpty()) {
                Text(
                    text = "No service history found for the selected vehicle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MyGarageColors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    services.forEachIndexed { index, log ->
                        val displaySubtitle = buildString {
                            append("Completed at ")
                            append(log.mileage)
                            append(" - Date: ")
                            append(log.date)
                            append(" [Type: ")
                            append(log.type)
                            append("]")
                        }
                        TimelineItem(
                            title = log.description,
                            subtitle = displaySubtitle,
                            isLast = index == services.size - 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Service Log Form
            if (selectedVehicleId != null) {
                Text(
                    text = "LOG MAINTENANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MyGarageColors.surfaceContainerLowest)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mileage,
                        onValueChange = { mileage = it },
                        label = { Text("Mileage at Service") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Service Type Chips
                    Text(
                        text = "SERVICE TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.onSurfaceVariant
                    )

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
                                    .background(if (isSelected) MyGarageColors.primary else MyGarageColors.surfaceContainerLow)
                                    .clickable { selectedType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MyGarageColors.surfaceContainerLowest else MyGarageColors.onSurface
                                )
                            }
                        }
                    }

                    // Parts Used Section (for revision type)
                    if (selectedType == "revision") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PARTS USED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MyGarageColors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MyGarageColors.surfaceContainerLow)
                                    .padding(8.dp),
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

                    Button(
                        onClick = {
                            if (description.isNotBlank() && mileage.isNotBlank()) {
                                val log = ServiceLogEntity(
                                    id = UUID.randomUUID(),
                                    vehicleId = selectedVehicleId,
                                    date = serviceDate,
                                    description = description,
                                    mileage = if (mileage.contains("mi")) mileage else "$mileage mi",
                                    type = selectedType
                                )
                                if (selectedType == "revision") {
                                    onLogServiceWithParts(log)
                                } else {
                                    onLogService(log)
                                }
                                
                                // Reset form inputs (except date)
                                description = ""
                                partsSearchQuery = ""
                            }
                        },
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
                            text = "LOG MAINTENANCE SERVICE",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please select a vehicle to log services.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MyGarageColors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Part Dialog
    if (showAddPartDialog) {
        AddPartDialog(
            onDismiss = { showAddPartDialog = false },
            onConfirm = { name, quantity, reference ->
                onAddTemporaryPart(name, quantity, reference)
                showAddPartDialog = false
            }
        )
    }
}

/**
 * Elegant dialog for adding a new part to the temporary parts list.
 * Follows the "Mechanical Atelier" design system with rounded corners and tonal layering.
 */
@Composable
private fun AddPartDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, reference: String?) -> Unit
) {
    var partName by remember { mutableStateOf("") }
    var partQuantity by remember { mutableStateOf("") }
    var partReference by remember { mutableStateOf("") }

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
                onValueChange = { partName = it },
                label = { Text("Part Name") },
                placeholder = { Text("e.g. Oil Filter, Brake Pads...") },
                colors = dialogTextFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = partQuantity,
                onValueChange = { newValue ->
                    if (newValue.all { c -> c.isDigit() } && newValue.length <= 4) {
                        partQuantity = newValue
                    }
                },
                label = { Text("Quantity") },
                placeholder = { Text("1") },
                colors = dialogTextFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    Text(
                        "CANCEL",
                        color = MyGarageColors.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val qty = partQuantity.toIntOrNull() ?: 1
                        if (partName.isNotBlank() && qty > 0) {
                            onConfirm(partName.trim(), qty, partReference.trim().ifBlank { null })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary,
                        contentColor = MyGarageColors.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "ADD",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
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
