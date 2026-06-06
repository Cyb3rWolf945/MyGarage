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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors
import ipt.pt.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.ui.components.VehicleEditDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VehicleProfileScreen(
    uiState: VehicleProfileUiState,
    vehicleEntity: VehicleEntity,
    onBackClick: () -> Unit,
    onNavigateToService: () -> Unit,
    onUpdateVehicle: (VehicleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        VehicleEditDialog(
            vehicle = vehicleEntity,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedVehicle ->
                onUpdateVehicle(updatedVehicle)
                showEditDialog = false
            }
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
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
                                text = stringResource(id = R.string.app_header_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = MyGarageColors.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.name,
                                style = MaterialTheme.typography.displayLarge,
                                color = MyGarageColors.onBackground
                            )
                        }
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Vehicle",
                                tint = MyGarageColors.primary
                            )
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
                        label = stringResource(id = R.string.stat_mileage),
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
                        label = stringResource(id = R.string.stat_mileage_to_next_service),
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
                    val selected = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = tabTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MyGarageColors.primary else MyGarageColors.onSurfaceVariant
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> SpecsTabContent(uiState = uiState)
                    1 -> HistoryTabContent(uiState = uiState, onNavigateToService = onNavigateToService)
                    2 -> LocationTabContent(uiState = uiState)
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
            Text(
                text = stringResource(id = R.string.specifications_header),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
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
            Text(
                text = stringResource(id = R.string.service_history_header).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )

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
private fun LocationTabContent(uiState: VehicleProfileUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.live_location_header).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            val locationIsMissing = uiState.locationAddress.isNullOrBlank()
            Text(
                text = if (locationIsMissing) stringResource(id = R.string.not_available)
                    else uiState.locationAddress!!,
                modifier = if (locationIsMissing) Modifier.alpha(0.5f) else Modifier,
                style = MaterialTheme.typography.headlineLarge,
                color = MyGarageColors.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MyGarageColors.surfaceContainerLow,
                                MyGarageColors.surfaceContainerHigh
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MyGarageColors.primary.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MyGarageColors.primary)
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
                color = MyGarageColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayValue,
                modifier = if (isMissing) Modifier.alpha(0.5f) else Modifier,
                style = MaterialTheme.typography.headlineMedium,
                color = MyGarageColors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
