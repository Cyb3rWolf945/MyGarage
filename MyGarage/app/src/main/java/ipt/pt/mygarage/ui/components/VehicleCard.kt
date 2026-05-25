package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * A single vehicle entry card.
 *
 * Design spec (Mechanical Atelier):
 *  - Tonal layering: surfaceContainerLowest (white) lifted over the background surface
 *  - No borders, no drop-shadows — depth is achieved purely through colour shift
 *  - Status chip uses the supplied [statusColor] at 10 % alpha so it feels lightweight
 *
 * @param model       Vehicle model name shown as the primary headline.
 * @param plate       Registration / plate string shown as a spec-sheet label below the model.
 * @param status      Short status label (e.g. "READY", "IN SERVICE") inside the chip.
 * @param statusColor Colour applied to the chip text and its translucent background tint.
 */
@Composable
fun VehicleCard(
    model: String,
    plate: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyGarageColors.surfaceContainerLowest)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Vehicle info ────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MyGarageColors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MyGarageColors.onSurfaceVariant
                )
            }

            // ── Status chip ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}
