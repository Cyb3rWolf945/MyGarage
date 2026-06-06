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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.theme.MyGarageColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val engineCapacityOptions = listOf(
    "1000 cc", "1200 cc", "1400 cc", "1600 cc",
    "2000 cc", "2500 cc", "3000 cc", "3500 cc", "4000 cc"
)
private val doorCountOptions = listOf("2", "3", "4", "5")
private val seatCountOptions = listOf("2", "4", "5", "7")
private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

/**
 * Formats a raw license plate input into the Portuguese XX-XX-XX format.
 * Strips non-alphanumeric characters, uppercases, and inserts hyphens
 * after every second character (max 8 chars including hyphens).
 */
private fun formatPortuguesePlate(raw: String): String {
    val alphanumeric = raw.filter { it.isLetterOrDigit() }.take(6).uppercase()
    return buildString {
        alphanumeric.forEachIndexed { index, c ->
            if (index > 0 && index % 2 == 0) append('-')
            append(c)
        }
    }
}

/**
 * Dialog for adding a new vehicle or editing properties of an existing one.
 * Follows the "Mechanical Atelier" design system: surface_container_lowest cards,
 * borderless aesthetic, tonal layering. Uses DatePickerDialog for dates,
 * ExposedDropdownMenuBox for enumerable fields, and numeric keyboard for year.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleEditDialog(
    vehicle: VehicleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (VehicleEntity) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var plate by remember { mutableStateOf(vehicle?.plate ?: "") }
    var year by remember { mutableStateOf(vehicle?.year ?: "") }
    var mileage by remember { mutableStateOf(vehicle?.mileage ?: "") }
    var inspectionDate by remember { mutableStateOf(vehicle?.inspectionDate ?: "") }
    var oilType by remember { mutableStateOf(vehicle?.oilType ?: "") }
    var owner by remember { mutableStateOf(vehicle?.owner ?: "") }
    var seatCount by remember { mutableStateOf(vehicle?.seatCount ?: "") }
    var doorCount by remember { mutableStateOf(vehicle?.doorCount ?: "") }
    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: "") }
    var engineCapacity by remember { mutableStateOf(vehicle?.engineCapacity ?: "") }
    var iucValue by remember { mutableStateOf(vehicle?.iucValue ?: "") }
    var mileageToNextService by remember { mutableStateOf(vehicle?.mileageToNextService ?: "") }
    var locationAddress by remember { mutableStateOf(vehicle?.locationAddress ?: "") }

    var engineCapacityExpanded by remember { mutableStateOf(false) }
    var doorCountExpanded by remember { mutableStateOf(false) }
    var seatCountExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            dateFormat.parse(vehicle?.inspectionDate ?: "")?.time
        }.getOrDefault(System.currentTimeMillis())
    )

    val scrollState = rememberScrollState()

    fun parseMileageNumeric(raw: String): Double {
        val clean = raw.replace(",", "").replace(Regex("[^0-9.]"), "")
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun autoCalcNextService(currentMileageStr: String) {
        val numeric = parseMileageNumeric(currentMileageStr)
        if (numeric > 0) {
            val next = numeric + 10000.0
            mileageToNextService = String.format(Locale.US, "%,.0f mi", next)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        inspectionDate = dateFormat.format(Date(millis))
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

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MyGarageColors.surfaceContainerLowest)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (vehicle == null) "INSERT VEHICLE" else "EDIT VEHICLE PROPERTIES",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MyGarageColors.primary,
                unfocusedBorderColor = MyGarageColors.surfaceContainerHigh,
                focusedLabelColor = MyGarageColors.primary,
                unfocusedLabelColor = MyGarageColors.onSurfaceVariant,
                focusedTextColor = MyGarageColors.onSurface,
                unfocusedTextColor = MyGarageColors.onSurface,
                focusedContainerColor = MyGarageColors.surfaceContainerLow,
                unfocusedContainerColor = MyGarageColors.surfaceContainerLow
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // License Plate — auto-formatted to Portuguese XX-XX-XX
            OutlinedTextField(
                value = plate,
                onValueChange = { raw ->
                    plate = formatPortuguesePlate(raw)
                },
                label = { Text("License Plate") },
                placeholder = { Text("XX-XX-XX") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Year - numeric only, 4-char limit
            OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        year = it
                    }
                },
                label = { Text("Year") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Mileage with auto-calc
            OutlinedTextField(
                value = mileage,
                onValueChange = {
                    mileage = it
                    autoCalcNextService(it)
                },
                label = { Text("Mileage (e.g. 12,450 mi)") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Inspection Date - read-only, taps to open DatePickerDialog
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = inspectionDate,
                    onValueChange = {},
                    label = { Text("Inspection Date") },
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
                    supportingText = { Text("Tap to select date", style = MaterialTheme.typography.labelSmall) }
                )
            }

            // Oil Type
            OutlinedTextField(
                value = oilType,
                onValueChange = { oilType = it },
                label = { Text("Oil Type") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Owner
            OutlinedTextField(
                value = owner,
                onValueChange = { owner = it },
                label = { Text("Owner") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Seat Count - Dropdown
            ExposedDropdownMenuBox(
                expanded = seatCountExpanded,
                onExpandedChange = { seatCountExpanded = it }
            ) {
                OutlinedTextField(
                    value = seatCount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seat Count") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seatCountExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = seatCountExpanded,
                    onDismissRequest = { seatCountExpanded = false }
                ) {
                    seatCountOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                seatCount = option
                                seatCountExpanded = false
                            }
                        )
                    }
                }
            }

            // Door Count - Dropdown
            ExposedDropdownMenuBox(
                expanded = doorCountExpanded,
                onExpandedChange = { doorCountExpanded = it }
            ) {
                OutlinedTextField(
                    value = doorCount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Door Count") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doorCountExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = doorCountExpanded,
                    onDismissRequest = { doorCountExpanded = false }
                ) {
                    doorCountOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                doorCount = option
                                doorCountExpanded = false
                            }
                        )
                    }
                }
            }

            // Fuel Type — Dropdown with string resource options
            var fuelTypeExpanded by remember { mutableStateOf(false) }
            val fuelTypeOptions = listOf(
                stringResource(id = R.string.fuel_gasoline),
                stringResource(id = R.string.fuel_diesel),
                stringResource(id = R.string.fuel_electric)
            )
            ExposedDropdownMenuBox(
                expanded = fuelTypeExpanded,
                onExpandedChange = { fuelTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = fuelType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.fuel_type)) },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelTypeExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = fuelTypeExpanded,
                    onDismissRequest = { fuelTypeExpanded = false }
                ) {
                    fuelTypeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                fuelType = option
                                fuelTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Engine Capacity - Dropdown
            ExposedDropdownMenuBox(
                expanded = engineCapacityExpanded,
                onExpandedChange = { engineCapacityExpanded = it }
            ) {
                OutlinedTextField(
                    value = engineCapacity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Engine Capacity") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineCapacityExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = engineCapacityExpanded,
                    onDismissRequest = { engineCapacityExpanded = false }
                ) {
                    engineCapacityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                engineCapacity = option
                                engineCapacityExpanded = false
                            }
                        )
                    }
                }
            }

            // IUC Value
            OutlinedTextField(
                value = iucValue,
                onValueChange = { iucValue = it },
                label = { Text("IUC Value (€)") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Mileage to Next Service - auto-calculated, read-only
            OutlinedTextField(
                value = mileageToNextService,
                onValueChange = {},
                label = { Text("Mileage to Next Service") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                supportingText = { Text("Auto-calculated (mileage + 10,000)", style = MaterialTheme.typography.labelSmall) }
            )

            // Location Address
            OutlinedTextField(
                value = locationAddress,
                onValueChange = { locationAddress = it },
                label = { Text("Location Address") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.surfaceContainerLow,
                        contentColor = MyGarageColors.onSurface
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (name.isNotBlank() && plate.isNotBlank()) {
                            val result = VehicleEntity(
                                id = vehicle?.id ?: java.util.UUID.randomUUID().toString(),
                                plate = plate,
                                name = name,
                                year = year,
                                mileage = mileage,
                                inspectionDate = inspectionDate.ifBlank { null },
                                oilType = oilType.ifBlank { null },
                                owner = owner,
                                seatCount = seatCount.ifBlank { null },
                                doorCount = doorCount.ifBlank { null },
                                fuelType = fuelType,
                                engineCapacity = engineCapacity,
                                iucValue = iucValue.ifBlank { null },
                                mileageToNextService = mileageToNextService,
                                locationAddress = locationAddress.ifBlank { null }
                            )
                            onConfirm(result)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary,
                        contentColor = MyGarageColors.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
