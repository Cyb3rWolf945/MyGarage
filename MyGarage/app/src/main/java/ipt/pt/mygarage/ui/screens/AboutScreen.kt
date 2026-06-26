package ipt.pt.mygarage.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * Premium 'About' screen detailing the academic project, its architecture,
 * and the open-source libraries powering the application.
 *
 * Follows the 'Mechanical Atelier' design system: borderless cards,
 * tonal layering, elegant typography, no 1px borders.
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val versionName = "1.0.0"

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
                .padding(top = 8.dp, bottom = 48.dp)
        ) {
            // ── Back Row ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MyGarageColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.back_to_garage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Hero Section ────────────────────────────────────────────
            HeroSection(versionName = versionName)

            Spacer(modifier = Modifier.height(32.dp))

            // ── Academic Card ───────────────────────────────────────────
            AcademicCard()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Architecture Section ────────────────────────────────────
            ArchitectureSection()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Open Source Libraries ───────────────────────────────────
            LibrariesSection()

            Spacer(modifier = Modifier.height(32.dp))

            // ── Footer ──────────────────────────────────────────────────
            // Terms & Conditions link (required by Google Play policy)
            Text(
                text = stringResource(R.string.terms_nav),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTerms() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.about_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── HERO SECTION ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(versionName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Garage icon surface
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MyGarageColors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_garage),
                contentDescription = null,
                tint = MyGarageColors.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Name
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MyGarageColors.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Version badge — pill shape
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MyGarageColors.primaryContainer.copy(alpha = 0.4f))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.labelMedium,
                color = MyGarageColors.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── ACADEMIC CARD ────────────────────────────────────────────────────────────

@Composable
private fun AcademicCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLow)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Course
            AcademicRow(
                icon = Icons.Outlined.Star,
                label = stringResource(R.string.about_course_label),
                value = stringResource(R.string.about_course_value)
            )

            HorizontalDivider(color = MyGarageColors.surfaceContainerHigh)

            // Curricular Unit
            AcademicRow(
                icon = Icons.Outlined.Info,
                label = stringResource(R.string.about_curricular_unit_label),
                value = stringResource(R.string.about_unit_value)
            )

            HorizontalDivider(color = MyGarageColors.surfaceContainerHigh)

            // Author
            AcademicRow(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.about_author_label),
                value = stringResource(R.string.about_author_value)
            )
        }
    }
}

@Composable
private fun AcademicRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyGarageColors.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MyGarageColors.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── ARCHITECTURE SECTION ──────────────────────────────────────────────────────

@Composable
private fun ArchitectureSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLow)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.about_architecture_header),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.about_architecture_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

// ── LIBRARIES SECTION ─────────────────────────────────────────────────────────

@Composable
private fun LibrariesSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLow)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = stringResource(R.string.about_libraries_header),
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val libraries = listOf(
                R.string.about_lib_compose_name to R.string.about_lib_compose_desc,
                R.string.about_lib_room_name to R.string.about_lib_room_desc,
                R.string.about_lib_datastore_name to R.string.about_lib_datastore_desc,
                R.string.about_lib_navigation_name to R.string.about_lib_navigation_desc,
                R.string.about_lib_coil_name to R.string.about_lib_coil_desc,
                R.string.about_lib_camerax_name to R.string.about_lib_camerax_desc,
                R.string.about_lib_mlkit_name to R.string.about_lib_mlkit_desc,
                R.string.about_lib_maps_name to R.string.about_lib_maps_desc,
                R.string.about_lib_retrofit_name to R.string.about_lib_retrofit_desc
            )

            libraries.forEachIndexed { index, (nameRes, descRes) ->
                LibraryRow(
                    name = stringResource(nameRes),
                    description = stringResource(descRes)
                )
                if (index < libraries.lastIndex) {
                    HorizontalDivider(
                        color = MyGarageColors.surfaceContainerHigh,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    name: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            tint = MyGarageColors.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MyGarageColors.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}
