package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ipt.pt.mygarage.MyGarageApplication
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors
import ipt.pt.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.components.VehicleEditDialog
import ipt.pt.mygarage.ui.components.DeleteConfirmationDialog
import ipt.pt.mygarage.ui.components.rememberLocationPermissionHandler
import ipt.pt.mygarage.ui.components.LocationPermanentDenialDialog
import ipt.pt.mygarage.ui.components.FullScreenImageCarousel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VehicleProfileScreen(
    uiState: VehicleProfileUiState,
    vehicleEntity: VehicleEntity,
    onBackClick: () -> Unit,
    onNavigateToService: () -> Unit,
    onUpdateVehicle: (VehicleEntity) -> Unit,
    onDeleteVehicle: () -> Unit = {},
    showDeleteConfirmation: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onFieldChanged: (String) -> Unit = {},
    onFetchLocationClicked: () -> Unit = {},
    isCarouselVisible: Boolean = false,
    carouselStartIndex: Int = 0,
    onOpenCarousel: (Int) -> Unit = {},
    onCloseCarousel: () -> Unit = {},
    resolvedDistanceUnit: String = "MILES",
    modifier: Modifier = Modifier
) {
    val unitName = if (resolvedDistanceUnit == "KILOMETERS")
        stringResource(R.string.unit_kilometers)
    else stringResource(R.string.unit_miles)

    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val app = context.applicationContext as MyGarageApplication
    val imageStorageManager = app.imageStorageManager
    val resolvedImagePath = vehicleEntity.localImageFileNames.firstOrNull()?.let {
        imageStorageManager.getImagePath(it)
    }

    if (showEditDialog) {
        VehicleEditDialog(
            vehicle = vehicleEntity,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedVehicle ->
                onUpdateVehicle(updatedVehicle)
                showEditDialog = false
            },
            existingImageFileName = vehicleEntity.localImageFileNames.firstOrNull(),
            imageStorageManager = imageStorageManager,
            formErrors = uiState.formErrors,
            onFieldChanged = onFieldChanged
        )
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete
        )
    }

    if (isCarouselVisible && vehicleEntity.localImageFileNames.isNotEmpty()) {
        val imageStorageManager = app.imageStorageManager
        val resolvedPaths = vehicleEntity.localImageFileNames.mapNotNull { fileName ->
            imageStorageManager.getImagePath(fileName)
        }
        if (resolvedPaths.isNotEmpty()) {
            FullScreenImageCarousel(
                imageFilePaths = resolvedPaths,
                onDismiss = onCloseCarousel
            )
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
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clickable(enabled = vehicleEntity.localImageFileNames.isNotEmpty()) {
                        onOpenCarousel(0)
                    }
            ) {
                val imageFile = resolvedImagePath?.let { File(it) }
                if (imageFile != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = stringResource(R.string.vehicle_photo_cd),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                } else {
                    // Premium gradient placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MyGarageColors.surfaceContainerHigh,
                                        MyGarageColors.surfaceContainerLow
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_garage),
                            contentDescription = stringResource(R.string.add_vehicle_photo_cd),
                            tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MyGarageColors.background.copy(alpha = 0.5f),
                                    MyGarageColors.background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_garage),
                            contentDescription = stringResource(id = R.string.back_to_garage),
                            tint = MyGarageColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.back_to_garage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MyGarageColors.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.name,
                                style = MaterialTheme.typography.displayLarge,
                                color = MyGarageColors.onBackground
                            )
                        }
                        Row {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.action_edit),
                                    tint = MyGarageColors.primary
                                )
                            }
                            IconButton(onClick = onDeleteVehicle) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.action_delete),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BentoCell(
                        label = stringResource(id = R.string.stat_mileage, unitName),
                        value = uiState.mileage,
                        modifier = Modifier.weight(1f)
                    )
                    BentoCell(
                        label = stringResource(id = R.string.stat_inspection_date),
                        value = uiState.inspectionDate?.let { formatCompactDate(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BentoCell(
                        label = stringResource(id = R.string.stat_mileage_to_next_service, unitName),
                        value = uiState.mileageToNextService,
                        modifier = Modifier.weight(1f)
                    )
                    BentoCell(
                        label = stringResource(id = R.string.stat_iuc_value),
                        value = uiState.iucValue?.let {
                            stringResource(id = R.string.currency_euro_prefix, it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf(
                    stringResource(id = R.string.tab_specs),
                    stringResource(id = R.string.tab_history),
                    stringResource(id = R.string.tab_location)
                )
                tabs.forEachIndexed { index, tabTitle ->
                    val selected = pagerState.currentPage == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = tabTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MyGarageColors.primary else MyGarageColors.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .clip(CircleShape)
                                    .background(MyGarageColors.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) { page ->
                when (page) {
                    0 -> SpecsTabContent(uiState = uiState)
                    1 -> HistoryTabContent(uiState = uiState, onNavigateToService = onNavigateToService)
                    2 -> LocationTabContent(uiState = uiState, onFetchLocationClicked = onFetchLocationClicked)
                }
            }
        }
    }
}

@Composable
private fun SpecsTabContent(uiState: VehicleProfileUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SpecsGridRow(
                leftLabel = stringResource(id = R.string.spec_year),
                leftValue = uiState.year,
                rightLabel = stringResource(id = R.string.spec_fuel_type),
                rightValue = uiState.fuelType
            )
            SpecsGridRow(
                leftLabel = stringResource(id = R.string.spec_owner),
                leftValue = uiState.owner,
                rightLabel = stringResource(id = R.string.spec_engine_capacity),
                rightValue = uiState.engineCapacity
            )
            SpecsGridRow(
                leftLabel = stringResource(id = R.string.spec_oil_type),
                leftValue = uiState.oilType,
                rightLabel = stringResource(id = R.string.spec_seat_count),
                rightValue = uiState.seatCount
            )
            SpecsGridRow(
                leftLabel = stringResource(id = R.string.spec_door_count),
                leftValue = uiState.doorCount,
                rightLabel = "",
                rightValue = ""
            )
        }
    }
}

@Composable
private fun HistoryTabContent(
    uiState: VehicleProfileUiState,
    onNavigateToService: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val displayHistory = uiState.serviceHistory.take(2)
            displayHistory.forEachIndexed { index, item ->
                TimelineItem(
                    title = item.title,
                    subtitle = item.subtitle,
                    isFirst = index == 0,
                    isLast = index == displayHistory.size - 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToService,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MyGarageColors.primary,
                                MyGarageColors.primaryContainer
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.action_log_maintenance),
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.surfaceContainerLowest
                )
            }
        }
    }
}

@Composable
private fun LocationTabContent(
    uiState: VehicleProfileUiState,
    onFetchLocationClicked: () -> Unit
) {
    val context = LocalContext.current
    val lat = uiState.latitude
    val lng = uiState.longitude
    val hasCoordinates = lat != null && lng != null

    android.util.Log.d("MyGarage.Location", "LocationTabContent composing: lat=$lat lng=$lng hasCoordinates=$hasCoordinates")

    val locationPermission = rememberLocationPermissionHandler(
        onGranted = onFetchLocationClicked
    )

    // Show settings-redirect dialog when permanently denied
    LocationPermanentDenialDialog(
        showDialog = locationPermission.showSettingsDialog,
        onDismiss = locationPermission.dismissSettingsDialog,
        onOpenSettings = {
            locationPermission.dismissSettingsDialog()
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        if (hasCoordinates) {
            // ── Google Map with Marker ───────────────────────────────────
            val cameraPositionState = rememberCameraPositionState(
                key = "${lat}_${lng}"
            ) {
                position = CameraPosition.fromLatLngZoom(
                    LatLng(lat!!, lng!!), 15f
                )
            }
            Column {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(lat!!, lng!!))
                    )
                }
            }
        } else {
            // ── Clickable Empty State Placeholder ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                        tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.vehicle_location_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        color = MyGarageColors.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.update_via_gps).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoCell(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    val isMissing = value.isNullOrBlank()
    val displayValue = if (isMissing) stringResource(id = R.string.not_available) else value!!
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayValue,
                modifier = (if (isMissing) Modifier.alpha(0.5f) else Modifier).fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                color = MyGarageColors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SpecsGridRow(
    leftLabel: String,
    leftValue: String?,
    rightLabel: String,
    rightValue: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SpecGridItem(
            label = leftLabel,
            value = leftValue,
            modifier = Modifier.weight(1f)
        )
        SpecGridItem(
            label = rightLabel,
            value = rightValue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SpecGridItem(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    val isMissing = value.isNullOrBlank()
    val displayValue = if (isMissing) stringResource(id = R.string.not_available) else value!!
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
            Text(
                text = displayValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isMissing) Modifier.alpha(0.5f) else Modifier),
                style = MaterialTheme.typography.titleLarge,
                color = MyGarageColors.onSurface,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatCompactDate(rawDate: String): String {
    val inputFormats = listOf("dd/MM/yyyy", "dd/MM/yy", "yyyy-MM-dd")
    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    for (pattern in inputFormats) {
        runCatching {
            val parsed = SimpleDateFormat(pattern, Locale.getDefault()).parse(rawDate)
            if (parsed != null) {
                return outputFormat.format(parsed)
            }
        }
    }

    return rawDate
}

@Composable
private fun TimelineItem(
    title: String,
    subtitle: String,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            val lineTopPadding = if (isFirst) 8.dp else 0.dp
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = lineTopPadding)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MyGarageColors.primary)
                )
            } else if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(MyGarageColors.primary)
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MyGarageColors.surfaceContainerHighest)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MyGarageColors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}
