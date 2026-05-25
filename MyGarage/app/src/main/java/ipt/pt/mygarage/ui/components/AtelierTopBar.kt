package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * The Mechanical Atelier top app bar.
 *
 * Design spec:
 *  - Profile avatar circle on the left, inside the title slot
 *  - 'ATELIER' brand word-mark next to it
 *  - Notification icon badge on the right (actions slot)
 *  - Glassmorphic container: surface colour at 80 % opacity / no elevation line
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtelierTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ── Profile avatar ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MyGarageColors.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = stringResource(id = R.string.profile_description),
                        tint = MyGarageColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── Brand word-mark ──────────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.app_header_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MyGarageColors.onBackground
                )
            }
        },
        actions = {
            // ── Notification badge ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MyGarageColors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = stringResource(id = R.string.notifications_description),
                    tint = MyGarageColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MyGarageColors.surface.copy(alpha = 0.8f),
            titleContentColor = MyGarageColors.onBackground,
            actionIconContentColor = MyGarageColors.primary
        )
    )
}
