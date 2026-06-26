package pt.ipt.dama2026.mygarage.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.network.NetworkModule
import pt.ipt.dama2026.mygarage.domain.locale.DistanceFormatter
import pt.ipt.dama2026.mygarage.domain.locale.LocaleManager
import pt.ipt.dama2026.mygarage.MyGarageApplication
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors
import pt.ipt.dama2026.mygarage.ui.components.rememberLocationPermissionHandler
import pt.ipt.dama2026.mygarage.ui.components.LocationPermanentDenialDialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    imageStorageManager: pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager? = null,
    onImageSelected: (String) -> Unit = {},
    formErrors: Map<String, Int> = emptyMap(),
    onFieldChanged: (String) -> Unit = {},
    resolvedDistanceUnit: String = "MILES",
    existingVehicles: List<VehicleEntity> = emptyList()
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
    var latitude by remember { mutableStateOf(vehicle?.latitude) }
    var longitude by remember { mutableStateOf(vehicle?.longitude) }

    // Local validation errors merged with ViewModel errors for display
    var localErrors by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val allErrors = localErrors + formErrors

    // Track existing images that should be kept (for progressive deletion)
    var keptImageFileNames by remember {
        mutableStateOf(vehicle?.localImageFileNames ?: emptyList())
    }

    // Track newly picked images (temporary) before save
    var newlyAddedImageFileNames by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    // Snapshot initial image count so we can detect user-initiated deletions.
    // When the user deletes images, we suppress the remote-URL fallback —
    // otherwise the same image would reappear from S3 via proxy.
    val initialTotalImages = remember {
        (vehicle?.localImageFileNames?.size ?: 0)
    }
    // Track whether the user explicitly deleted the remote-only image
    var userMarkedRemoteDeleted by remember { mutableStateOf(false) }
    val hasDeletedImages = userMarkedRemoteDeleted ||
        ((newlyAddedImageFileNames.size + keptImageFileNames.size) < initialTotalImages)

    fun clearFieldError(field: String) {
        if (localErrors.containsKey(field)) localErrors = localErrors - field
        onFieldChanged(field)
    }

    fun onDeleteNewlyAddedImage(index: Int) {
        if (index >= 0 && index < newlyAddedImageFileNames.size) {
            newlyAddedImageFileNames = newlyAddedImageFileNames.filterIndexed { i, _ -> i != index }
        }
    }

    fun onDeleteExistingImage(index: Int) {
        if (index >= 0 && index < keptImageFileNames.size) {
            keptImageFileNames = keptImageFileNames.filterIndexed { i, _ -> i != index }
            // If this was the last local image and a remote copy exists,
            // auto-mark it deleted too — one tap removes both.
            if (keptImageFileNames.isEmpty() && newlyAddedImageFileNames.isEmpty() &&
                !vehicle?.remoteImageUrl.isNullOrEmpty()
            ) {
                userMarkedRemoteDeleted = true
            }
        }
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

    // Track last processed URI to prevent duplicates
    var lastProcessedUri by remember { mutableStateOf<Uri?>(null) }

    // Save picked image immediately so the filename is ready when Save is tapped
    LaunchedEffect(pickedUri) {
        val uri = pickedUri
        if (uri != null && uri != lastProcessedUri && imageStorageManager != null) {
            val fileName = withContext(Dispatchers.IO) {
                imageStorageManager.saveImage(uri.toString())
            }
            if (fileName != null) {
                lastProcessedUri = uri
                // Add to newly picked images list (not replacing)
                newlyAddedImageFileNames = newlyAddedImageFileNames + fileName
                pickedUri = null // Clear to allow picking again
            }
        }
    }

    fun parseMileageNumeric(raw: String): Double {
        return DistanceFormatter.parseUserInput(raw)
    }

    fun autoCalcNextService(currentMileageStr: String) {
        val numeric = parseMileageNumeric(currentMileageStr)
        if (numeric > 0) {
            val next = numeric + 10000.0
            val unitLabel = LocaleManager.unitLabel(resolvedDistanceUnit)
            mileageToNextService = String.format(Locale.US, "%,.0f %s", next, unitLabel)
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
                    Text(stringResource(R.string.dialog_action_ok), color = MyGarageColors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_action_cancel), color = MyGarageColors.onSurfaceVariant)
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
            val context = LocalContext.current
            val imageModel = remember(newlyAddedImageFileNames, keptImageFileNames, vehicle?.remoteImageUrl, hasDeletedImages) {
                // Resolve the best available image to display, trying each source
                // and falling through if the local file doesn't actually exist on disk.
                fun resolvePath(fileName: String?): java.io.File? {
                    if (fileName == null) return null
                    val path = imageStorageManager?.getImagePath(fileName) ?: return null
                    val file = java.io.File(path)
                    return if (file.exists()) file else null
                }

                // 1. Newly picked images (saved in this edit session)
                resolvePath(newlyAddedImageFileNames.firstOrNull())
                // 2. Kept images from previous sessions (may be stale after reinstall)
                ?: resolvePath(keptImageFileNames.firstOrNull())
                // 3. Remote URL via proxy — only if user hasn't just deleted local images
                ?: if (!hasDeletedImages) {
                    vehicle?.remoteImageUrl
                        ?.replace("\"", "")
                        ?.let { NetworkModule.buildImageProxyUrl(context, it) }
                } else null
            }

            Column(modifier = Modifier.fillMaxWidth()) {
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
                            SubcomposeAsyncImage(
                                model = model,
                                contentDescription = stringResource(R.string.vehicle_photo_cd),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                loading = { ShimmerPlaceholder() },
                                error = {
                                    GradientPlaceholder()
                                }
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

                // ── Helpful hint text ────────────────────────────────────
                if (newlyAddedImageFileNames.isNotEmpty() || keptImageFileNames.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.add_more_photos_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tap_to_add_photos),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Image Thumbnails with Delete Buttons ─────────────────────
            // Show thumbnails if we have newly added images, kept images,
            // OR a remote URL (as fallback thumbnail when local files are stale
            // AND the user hasn't just deleted those local images)
            val remoteThumbCount = if (!hasDeletedImages && !vehicle?.remoteImageUrl.isNullOrEmpty() && keptImageFileNames.isEmpty()) 1 else 0
            val thumbnailCount = newlyAddedImageFileNames.size + keptImageFileNames.size + remoteThumbCount
            if (thumbnailCount > 0) {
                // Build proxy URL once for remote fallback thumbnails
                val remoteThumbUrl = remember(vehicle?.remoteImageUrl) {
                    vehicle?.remoteImageUrl
                        ?.replace("\"", "")
                        ?.let { NetworkModule.buildImageProxyUrl(context, it) }
                }
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    // Newly added images (with green accent)
                    itemsIndexed(newlyAddedImageFileNames) { index, fileName ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MyGarageColors.surfaceContainerLow)
                                .border(2.dp, MyGarageColors.primary, RoundedCornerShape(12.dp))
                        ) {
                            val imagePath = imageStorageManager?.getImagePath(fileName)
                            if (imagePath != null) {
                                AsyncImage(
                                    model = java.io.File(imagePath),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                IconButton(
                                    onClick = { onDeleteNewlyAddedImage(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_delete),
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Existing images (no accent) — fall back to proxy URL
                    // if the local file doesn't exist (e.g., after reinstall)
                    itemsIndexed(keptImageFileNames) { index, fileName ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MyGarageColors.surfaceContainerLow)
                        ) {
                            val imagePath = imageStorageManager?.getImagePath(fileName)
                            val thumbModel: Any? = if (imagePath != null) {
                                java.io.File(imagePath)
                            } else if (index == 0 && remoteThumbUrl != null && !hasDeletedImages) {
                                remoteThumbUrl
                            } else {
                                null
                            }
                            if (thumbModel != null) {
                                AsyncImage(
                                    model = thumbModel,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                IconButton(
                                    onClick = { onDeleteExistingImage(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_delete),
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Remote-only thumbnail: deletable — marks the remote image
                    // as removed so it won't reappear after save.
                    if (keptImageFileNames.isEmpty() && remoteThumbUrl != null && !hasDeletedImages) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MyGarageColors.surfaceContainerLow)
                            ) {
                                AsyncImage(
                                    model = remoteThumbUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    IconButton(
                                        onClick = {
                                            userMarkedRemoteDeleted = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.action_delete),
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                placeholder = { Text(stringResource(R.string.dialog_vehicle_name_label)) },
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
                    val isDuplicatePlate = existingVehicles.any {
                        it.plate.equals(plate, ignoreCase = true) && it.id != vehicle?.id
                    }
                    if (isDuplicatePlate && plate.isNotBlank()) {
                        localErrors = localErrors + ("plate" to R.string.error_license_plate_already_exists)
                    } else {
                        clearFieldError("plate")
                    }
                },
                placeholder = { Text(stringResource(R.string.dialog_vehicle_plate_placeholder)) },
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
                placeholder = { Text(stringResource(R.string.dialog_vehicle_year_label)) },
                isError = allErrors.containsKey("year"),
                supportingText = { allErrors["year"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Mileage with auto-calc
            val mileageUnitName = if (resolvedDistanceUnit == "KILOMETERS")
                stringResource(R.string.unit_kilometers)
            else stringResource(R.string.unit_miles)
            OutlinedTextField(
                value = mileage,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() }
                    mileage = filtered
                    autoCalcNextService(filtered)
                    clearFieldError("mileage")
                },
                placeholder = { Text(stringResource(R.string.dialog_vehicle_mileage_label, mileageUnitName)) },
                visualTransformation = MileageVisualTransformation,
                isError = allErrors.containsKey("mileage"),
                supportingText = { allErrors["mileage"]?.let { Text(stringResource(it)) } },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Inspection Date - read-only, taps to open DatePickerDialog
            val todayFormatted = remember {
                java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date())
            }
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = inspectionDate,
                    onValueChange = {},
                    placeholder = {
                        Text(
                            text = if (inspectionDate.isBlank()) todayFormatted else stringResource(R.string.dialog_vehicle_inspection_date_label),
                            color = MyGarageColors.onSurfaceVariant
                        )
                    },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.dialog_vehicle_tap_date_hint),
                            tint = MyGarageColors.primary
                        )
                    },
                    supportingText = { Text(stringResource(R.string.dialog_vehicle_tap_date_hint), style = MaterialTheme.typography.labelSmall) }
                )
            }

            // Oil Type
            OutlinedTextField(
                value = oilType,
                placeholder = { Text(stringResource(R.string.dialog_vehicle_oil_type_label)) },
                onValueChange = {
                    oilType = it
                    clearFieldError("oilType")
                },
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
                placeholder = { Text(stringResource(R.string.dialog_vehicle_owner_label)) },
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
                    placeholder = { Text(stringResource(R.string.dialog_vehicle_seat_count_label)) },
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
                    placeholder = { Text(stringResource(R.string.dialog_vehicle_door_count_label)) },
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
                    placeholder = { Text(stringResource(id = R.string.fuel_type)) },
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
                    placeholder = { Text(stringResource(R.string.dialog_vehicle_engine_capacity_label)) },
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
                placeholder = { Text(stringResource(R.string.dialog_vehicle_iuc_value_label)) },
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
                placeholder = {
                    val unitName = if (resolvedDistanceUnit == "KILOMETERS")
                        stringResource(R.string.unit_kilometers)
                    else stringResource(R.string.unit_miles)
                    Text(stringResource(R.string.dialog_vehicle_mileage_next_service_label, unitName))
                },
                isError = allErrors.containsKey("mileageToNextService"),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                supportingText = {
                    allErrors["mileageToNextService"]?.let { Text(stringResource(it)) }
                        ?: run {
                            val unitName = if (resolvedDistanceUnit == "KILOMETERS")
                                stringResource(R.string.unit_kilometers)
                            else stringResource(R.string.unit_miles)
                            Text(stringResource(R.string.dialog_vehicle_auto_calc_hint, unitName), style = MaterialTheme.typography.labelSmall)
                        }
                }
            )

            // ── Location Section (GPS-Only) ──────────────────────────────
            val scope = rememberCoroutineScope()
            val app = context.applicationContext as MyGarageApplication
            val locationPermission = rememberLocationPermissionHandler(
                onGranted = {
                    scope.launch {
                        when (val result = app.locationManager.getCurrentLocation()) {
                            is pt.ipt.dama2026.mygarage.domain.location.LocationResult.Success -> {
                                latitude = result.lat
                                longitude = result.lng
                                android.util.Log.d("MyGarage.Location", "Dialog GPS success: lat=${result.lat} lng=${result.lng}")
                            }
                            is pt.ipt.dama2026.mygarage.domain.location.LocationResult.Error -> {
                                android.util.Log.e("MyGarage.Location", "Dialog GPS error: ${result.message}")
                            }
                        }
                    }
                }
            )
            LocationPermanentDenialDialog(
                showDialog = locationPermission.showSettingsDialog,
                onDismiss = locationPermission.dismissSettingsDialog,
                onOpenSettings = {
                    locationPermission.dismissSettingsDialog()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(4.dp))

            val hasCoordinates = latitude != null && longitude != null

            android.util.Log.d("MyGarage.Location", "VehicleEditDialog location section: lat=$latitude lng=$longitude hasCoordinates=$hasCoordinates")

            if (hasCoordinates) {
                // ── Google Map with Marker + Re-update via GPS ────────────
                val cameraPositionState = rememberCameraPositionState(
                    key = "${latitude}_${longitude}"
                ) {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(latitude!!, longitude!!), 15f
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { locationPermission.launchPermissionRequest() }
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = false,
                            rotationGesturesEnabled = false,
                            tiltGesturesEnabled = false,
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        Marker(
                            state = MarkerState(
                                position = LatLng(latitude!!, longitude!!)
                            )
                        )
                    }
                }
            } else {
                // ── Clickable Empty State Placeholder ─────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MyGarageColors.surfaceContainerLowest)
                        .clickable { locationPermission.launchPermissionRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.vehicle_location_unknown),
                            tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.vehicle_location_unknown),
                            style = MaterialTheme.typography.titleMedium,
                            color = MyGarageColors.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.update_via_gps),
                            style = MaterialTheme.typography.labelSmall,
                            color = MyGarageColors.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

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
                    Text(stringResource(R.string.dialog_action_cancel))
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

                        // Check for duplicate license plate
                        val isDuplicatePlate = existingVehicles.any {
                            it.plate.equals(plate, ignoreCase = true) && it.id != vehicle?.id
                        }
                        if (isDuplicatePlate) {
                            errors["plate"] = R.string.error_license_plate_already_exists
                        }

                        localErrors = errors
                        if (errors.isEmpty()) {
                            val rawMileageKm = DistanceFormatter.forStorage(
                                parseMileageNumeric(mileage), resolvedDistanceUnit
                            )
                            val result = VehicleEntity(
                                id = vehicle?.id ?: java.util.UUID.randomUUID().toString(),
                                plate = plate,
                                name = name,
                                year = year,
                                mileage = mileage,
                                mileageKm = rawMileageKm,
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
                                latitude = latitude,
                                longitude = longitude,
                                localImageFileNames = newlyAddedImageFileNames + keptImageFileNames,
                                remoteImageUrl = if (hasDeletedImages && newlyAddedImageFileNames.isEmpty() && keptImageFileNames.isEmpty()) {
                                    null // User removed all images — clear remote too
                                } else {
                                    vehicle?.remoteImageUrl
                                }
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
                    Text(stringResource(R.string.dialog_action_save))
                }
            }
        }
    }
}
