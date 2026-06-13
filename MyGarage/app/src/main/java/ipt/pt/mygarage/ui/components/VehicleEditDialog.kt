package ipt.pt.mygarage.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import ipt.pt.mygarage.R
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.theme.MyGarageColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    onConfirm: (VehicleEntity) -> Unit,
    selectedImageUri: String? = null,
    existingImageFileName: String? = null,
    imageStorageManager: ipt.pt.mygarage.domain.repository.ImageStorageManager? = null,
    onImageSelected: (String) -> Unit = {},
    formErrors: Map<String, Int> = emptyMap(),
    onFieldChanged: (String) -> Unit = {}
) {
    // Local picked Uri — avoids string round-trip permission loss
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
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

    // Local validation errors merged with ViewModel errors for display
    var localErrors by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val allErrors = localErrors + formErrors

    fun clearFieldError(field: String) {
        if (localErrors.containsKey(field)) localErrors = localErrors - field
        onFieldChanged(field)
    }

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

    // ── Photo Picker ─────────────────────────────────────────────────────
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedUri = uri
            onImageSelected(uri.toString())
        }
    }

    // Save picked image immediately so the filename is ready when Save is tapped
    var savedImageFileName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pickedUri) {
        val uri = pickedUri
        if (uri != null && imageStorageManager != null) {
            savedImageFileName = withContext(Dispatchers.IO) {
                try {
                    val fileName = "${java.util.UUID.randomUUID()}.jpg"
                    val targetFile = java.io.File(context.filesDir, "vehicle_images/$fileName")
                    targetFile.parentFile?.mkdirs()
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    fileName
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

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
            // ── Image Placeholder ─────────────────────────────────────────
            val imageModel = remember(pickedUri, existingImageFileName) {
                if (pickedUri != null) {
                    pickedUri
                } else if (existingImageFileName != null) {
                    imageStorageManager?.getImagePath(existingImageFileName)?.let { java.io.File(it) }
                } else {
                    null
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MyGarageColors.surfaceContainerLowest)
                    .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = imageModel,
                    label = "vehicle_photo_crossfade"
                ) { model ->
                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = stringResource(R.string.vehicle_photo_cd),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    } else {
                            // Premium empty-state placeholder
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = stringResource(R.string.add_vehicle_photo_cd),
                                    tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.add_vehicle_photo),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyGarageColors.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

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
                onValueChange = {
                    name = it
                    clearFieldError("name")
                },
                label = { Text("Name") },
                isError = allErrors.containsKey("name"),
                supportingText = { allErrors["name"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // License Plate — raw storage, hyphens injected via VisualTransformation
            OutlinedTextField(
                value = plate,
                onValueChange = { raw ->
                    plate = raw.filter { it.isLetterOrDigit() }.take(6).uppercase()
                    clearFieldError("plate")
                },
                label = { Text("License Plate") },
                placeholder = { Text("XX-XX-XX") },
                visualTransformation = LicensePlateVisualTransformation,
                isError = allErrors.containsKey("plate"),
                supportingText = { allErrors["plate"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                )
            )

            // Year - numeric only, 4-char limit
            OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        year = it
                        clearFieldError("year")
                    }
                },
                label = { Text("Year") },
                isError = allErrors.containsKey("year"),
                supportingText = { allErrors["year"]?.let { Text(stringResource(it)) } },
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
                    clearFieldError("mileage")
                },
                label = { Text("Mileage (e.g. 12,450 mi)") },
                isError = allErrors.containsKey("mileage"),
                supportingText = { allErrors["mileage"]?.let { Text(stringResource(it)) } },
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
                onValueChange = {
                    oilType = it
                    clearFieldError("oilType")
                },
                label = { Text("Oil Type") },
                isError = allErrors.containsKey("oilType"),
                supportingText = { allErrors["oilType"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Owner
            OutlinedTextField(
                value = owner,
                onValueChange = {
                    owner = it
                    clearFieldError("owner")
                },
                label = { Text("Owner") },
                isError = allErrors.containsKey("owner"),
                supportingText = { allErrors["owner"]?.let { Text(stringResource(it)) } },
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
                    isError = allErrors.containsKey("seatCount"),
                    supportingText = { allErrors["seatCount"]?.let { Text(stringResource(it)) } },
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
                    isError = allErrors.containsKey("doorCount"),
                    supportingText = { allErrors["doorCount"]?.let { Text(stringResource(it)) } },
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
                    isError = allErrors.containsKey("fuelType"),
                    supportingText = { allErrors["fuelType"]?.let { Text(stringResource(it)) } },
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
                                clearFieldError("fuelType")
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
                    isError = allErrors.containsKey("engineCapacity"),
                    supportingText = { allErrors["engineCapacity"]?.let { Text(stringResource(it)) } },
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
                                clearFieldError("engineCapacity")
                            }
                        )
                    }
                }
            }

            // IUC Value
            OutlinedTextField(
                value = iucValue,
                onValueChange = {
                    iucValue = it
                    clearFieldError("iucValue")
                },
                label = { Text("IUC Value (€)") },
                isError = allErrors.containsKey("iucValue"),
                supportingText = { allErrors["iucValue"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Mileage to Next Service - auto-calculated, read-only
            OutlinedTextField(
                value = mileageToNextService,
                onValueChange = {},
                label = { Text("Mileage to Next Service") },
                isError = allErrors.containsKey("mileageToNextService"),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                supportingText = {
                    allErrors["mileageToNextService"]?.let { Text(stringResource(it)) }
                        ?: Text("Auto-calculated (mileage + 10,000)", style = MaterialTheme.typography.labelSmall)
                }
            )

            // Location Address
            OutlinedTextField(
                value = locationAddress,
                onValueChange = {
                    locationAddress = it
                    clearFieldError("locationAddress")
                },
                label = { Text("Location Address") },
                isError = allErrors.containsKey("locationAddress"),
                supportingText = { allErrors["locationAddress"]?.let { Text(stringResource(it)) } },
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
                        val errors = mutableMapOf<String, Int>()
                        if (name.isBlank()) errors["name"] = R.string.error_field_required
                        if (plate.isBlank()) errors["plate"] = R.string.error_field_required
                        if (year.isBlank()) errors["year"] = R.string.error_field_required
                        if (mileage.isBlank()) errors["mileage"] = R.string.error_field_required
                        if (owner.isBlank()) errors["owner"] = R.string.error_field_required
                        if (fuelType.isBlank()) errors["fuelType"] = R.string.error_field_required
                        if (engineCapacity.isBlank()) errors["engineCapacity"] = R.string.error_field_required
                        localErrors = errors
                        if (errors.isEmpty()) {
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
                                mileageToNextService = mileageToNextService.ifBlank { null },
                                locationAddress = locationAddress.ifBlank { null },
                                localImageFileName = savedImageFileName ?: vehicle?.localImageFileName,
                                remoteImageUrl = vehicle?.remoteImageUrl
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
