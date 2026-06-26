package pt.ipt.dama2026.mygarage.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.data.network.NetworkModule
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors
import java.io.File

/**
 * Premium vehicle card featuring an immersive photo header,
 * vehicle identity, and status badge — following the
 * "Mechanical Atelier" tonal-layering aesthetic.
 *
 * @param imagePath Absolute file path to the vehicle photo, or null.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehicleCard(
    model: String,
    plate: String,
    imagePath: String? = null,
    remoteImageUrl: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongClick?.invoke() }
            )
    ) {
        Column {
            // ── Photo Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                val remoteUrlClean = remoteImageUrl?.replace("\"", "")
                val proxyUrl = NetworkModule.buildImageProxyUrl(context, remoteUrlClean)
                val imageModel: Any? = when {
                    imagePath != null -> File(imagePath)
                    proxyUrl != null -> proxyUrl
                    else -> null
                }
                if (imageModel != null) {
                    SubcomposeAsyncImage(
                        model = imageModel,
                        contentDescription = stringResource(R.string.vehicle_photo_cd),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        loading = { ShimmerPlaceholder() },
                        error = { GradientPlaceholder() }
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
            }

            // ── Vehicle Identity ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MyGarageColors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatLicensePlate(plate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyGarageColors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatLicensePlate(raw: String): String {
    return buildString {
        raw.forEachIndexed { index, c ->
            if (index > 0 && index % 2 == 0) append('-')
            append(c)
        }
    }
}
