package ipt.pt.mygarage.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ipt.pt.mygarage.MyGarageApplication
import ipt.pt.mygarage.R
import ipt.pt.mygarage.presentation.profile.ProfileUiState
import ipt.pt.mygarage.presentation.profile.ProfileViewModel
import ipt.pt.mygarage.ui.theme.MyGarageColors
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToGarage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val app = context.applicationContext as MyGarageApplication
    val imageStorageManager = app.imageStorageManager

    // Resolve avatar file path from stored filename
    val avatarPath = state.avatarFileName?.let { imageStorageManager.getImagePath(it) }

    // Photo picker launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onAvatarSelected(uri.toString())
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
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            if (state.isEditing) {
                EditModeContent(
                    state = state,
                    onUserNameChanged = viewModel::onUserNameChanged,
                    onGarageNameChanged = viewModel::onGarageNameChanged,
                    onSave = viewModel::onSaveProfile,
                    onCancel = viewModel::onEditToggled
                )
            } else {
                ViewModeContent(
                    state = state,
                    avatarPath = avatarPath,
                    onAvatarClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onEditClick = viewModel::onEditToggled,
                    onNavigateToGarage = onNavigateToGarage,
                    onAuthActionClicked = viewModel::onAuthActionClicked,
                    onLanguageChanged = viewModel::onLanguageChanged,
                    onDistanceUnitChanged = viewModel::onDistanceUnitChanged
                )
            }
        }
    }
}

// ── VIEW MODE ─────────────────────────────────────────────────────────────────

@Composable
private fun ViewModeContent(
    state: ProfileUiState,
    avatarPath: String?,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onAuthActionClicked: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onDistanceUnitChanged: (String) -> Unit
) {
    val unitName = if (state.resolvedDistanceUnit == "KILOMETERS")
        stringResource(R.string.unit_kilometers)
    else stringResource(R.string.unit_miles)

    // ═══ Hero Section ═══
    HeroSection(
        userName = state.userName,
        garageName = state.garageName,
        avatarPath = avatarPath,
        onAvatarClick = onAvatarClick
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Edit Profile button — primary
    Button(
        onClick = onEditClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MyGarageColors.primary
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 24.dp,
            vertical = 16.dp
        )
    ) {
        Text(
            text = stringResource(id = R.string.profile_action_edit),
            style = MaterialTheme.typography.labelSmall,
            color = MyGarageColors.surfaceContainerLowest
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // ═══ Bento Stats Row ═══
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1 — Cars Owned (clickable → Garage)
        BentoStatCard(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onNavigateToGarage),
            label = stringResource(id = R.string.profile_stat_cars_owned),
            value = state.carsOwned.toString(),
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MyGarageColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Card 2 — Total Mileage
        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(id = R.string.profile_stat_total_mileage, unitName),
            value = formatMileage(state.totalMileage),
            icon = {
                Text(
                    text = if (state.resolvedDistanceUnit == "KILOMETERS") stringResource(id = R.string.unit_label_km)
                           else stringResource(id = R.string.unit_label_mi),
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // ═══ Auth Action ═══
    AuthActionCard(
        isGuestMode = state.isGuestMode,
        onClick = onAuthActionClicked
    )

    Spacer(modifier = Modifier.height(32.dp))

    // ═══ Settings Section ═══
    SettingsSection(
        appLanguage = state.appLanguage,
        distanceUnit = state.distanceUnit,
        onLanguageChanged = onLanguageChanged,
        onDistanceUnitChanged = onDistanceUnitChanged
    )
}

// ── HERO SECTION ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    userName: String,
    garageName: String,
    avatarPath: String?,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Interactive Circular Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = avatarPath,
                label = "avatar_crossfade"
            ) { path ->
                val file = path?.let { File(it) }
                if (file != null) {
                    AsyncImage(
                        model = file,
                        contentDescription = stringResource(R.string.profile_avatar_description),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                } else {
                    // Centered person icon placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MyGarageColors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile_camera_badge_description),
                            tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName.ifBlank { "N/A" },
            style = MaterialTheme.typography.headlineLarge,
            color = MyGarageColors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = garageName.ifBlank { "N/A" },
            style = MaterialTheme.typography.bodyMedium,
            color = MyGarageColors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── BENTO STAT CARD ───────────────────────────────────────────────────────────

@Composable
private fun BentoStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLow)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            icon()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}

// ── AUTH ACTION CARD ──────────────────────────────────────────────────────────

@Composable
private fun AuthActionCard(
    isGuestMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isGuestMode) MyGarageColors.surfaceContainerLow
                else MyGarageColors.error.copy(alpha = 0.08f)
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isGuestMode) MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f)
                       else MyGarageColors.error.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isGuestMode) stringResource(id = R.string.profile_guest_mode_title)
                       else stringResource(id = R.string.profile_signed_in_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MyGarageColors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isGuestMode) stringResource(id = R.string.profile_guest_mode_description)
                       else stringResource(id = R.string.profile_signed_in_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = if (isGuestMode) {
                    ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.error.copy(alpha = 0.12f),
                        contentColor = MyGarageColors.error
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
            ) {
                if (!isGuestMode) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MyGarageColors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = if (isGuestMode) stringResource(id = R.string.profile_action_sign_up)
                           else stringResource(id = R.string.profile_action_sign_out),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGuestMode) MyGarageColors.surfaceContainerLowest
                            else MyGarageColors.error
                )
            }
        }
    }
}

// ── EDIT MODE ─────────────────────────────────────────────────────────────────

@Composable
private fun EditModeContent(
    state: ProfileUiState,
    onUserNameChanged: (String) -> Unit,
    onGarageNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.userName,
                onValueChange = onUserNameChanged,
                placeholder = {
                    Text(stringResource(id = R.string.profile_user_name_label))
                },
                isError = state.formErrors.containsKey("userName"),
                supportingText = state.formErrors["userName"]?.let { resId ->
                    { Text(text = stringResource(id = resId)) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MyGarageColors.surfaceContainerLow,
                    unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                    errorContainerColor = MyGarageColors.surfaceContainerHighest,
                    focusedBorderColor = MyGarageColors.primary,
                    unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                    errorBorderColor = MyGarageColors.error,
                    errorSupportingTextColor = MyGarageColors.error
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.garageName,
                onValueChange = onGarageNameChanged,
                placeholder = {
                    Text(stringResource(id = R.string.profile_garage_name_label))
                },
                isError = state.formErrors.containsKey("garageName"),
                supportingText = state.formErrors["garageName"]?.let { resId ->
                    { Text(text = stringResource(id = resId)) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MyGarageColors.surfaceContainerLow,
                    unfocusedContainerColor = MyGarageColors.surfaceContainerLow,
                    errorContainerColor = MyGarageColors.surfaceContainerHighest,
                    focusedBorderColor = MyGarageColors.primary,
                    unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                    errorBorderColor = MyGarageColors.error,
                    errorSupportingTextColor = MyGarageColors.error
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.surfaceContainerLowest,
                        contentColor = MyGarageColors.primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MyGarageColors.outlineVariant.copy(alpha = 0.15f),
                                MyGarageColors.outlineVariant.copy(alpha = 0.15f)
                            )
                        )
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_action_cancel),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Save
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyGarageColors.primary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_action_save),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.surfaceContainerLowest
                    )
                }
            }
        }
    }
}

// ── SETTINGS SECTION ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSection(
    appLanguage: String,
    distanceUnit: String,
    onLanguageChanged: (String) -> Unit,
    onDistanceUnitChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Language Selector ──────────────────────────────────────
        LanguageSelector(
            currentLanguage = appLanguage,
            onLanguageChanged = onLanguageChanged
        )

        // ── Distance Unit Selector ──────────────────────────────────
        DistanceUnitSelector(
            currentUnit = distanceUnit,
            onUnitChanged = onDistanceUnitChanged
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "SYSTEM" to stringResource(id = R.string.language_system_default),
        "en" to stringResource(id = R.string.language_english),
        "pt-PT" to stringResource(id = R.string.language_portuguese)
    )
    val selectedLabel = options.firstOrNull { it.first == currentLanguage }?.second
        ?: stringResource(id = R.string.language_system_default)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.profile_settings_language_label),
            style = MaterialTheme.typography.bodySmall,
            color = MyGarageColors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MyGarageColors.surfaceContainerLowest,
                    unfocusedContainerColor = MyGarageColors.surfaceContainerLowest,
                    focusedBorderColor = MyGarageColors.primary,
                    unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            onLanguageChanged(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceUnitSelector(
    currentUnit: String,
    onUnitChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "SYSTEM" to stringResource(id = R.string.distance_unit_system_default),
        "KILOMETERS" to stringResource(id = R.string.distance_unit_kilometers),
        "MILES" to stringResource(id = R.string.distance_unit_miles)
    )
    val selectedLabel = options.firstOrNull { it.first == currentUnit }?.second
        ?: stringResource(id = R.string.distance_unit_system_default)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.profile_settings_distance_unit_label),
            style = MaterialTheme.typography.bodySmall,
            color = MyGarageColors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MyGarageColors.surfaceContainerLowest,
                    unfocusedContainerColor = MyGarageColors.surfaceContainerLowest,
                    focusedBorderColor = MyGarageColors.primary,
                    unfocusedBorderColor = MyGarageColors.outlineVariant.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            onUnitChanged(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── FORMATTING HELPERS ────────────────────────────────────────────────────────

private fun formatMileage(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(value)
}
