package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors

@Composable
fun ServiceScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "WORK ORDER UTILITY",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.nav_service),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SERVICE HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                TimelineItem(
                    title = "Engine Oil & Filter replacement",
                        subtitle = "Completed at 14,200 mi - MOTUL 8100 5W-40",
                    isLast = false
                )
                TimelineItem(
                    title = "Brake Pads & Rotors Inspection",
                    subtitle = "In progress - Brembo ceramic replacement",
                    isLast = false
                )
                TimelineItem(
                    title = "Tire Rotation & Balance",
                    subtitle = "Scheduled - Michelin Pilot Sport 4S",
                    isLast = true
                )
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
