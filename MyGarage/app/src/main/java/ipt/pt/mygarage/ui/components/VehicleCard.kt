package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.ui.theme.MyGarageColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehicleCard(
    model: String,
    plate: String,
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
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = model,
                style = MaterialTheme.typography.headlineLarge,
                color = MyGarageColors.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = plate,
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.onSurfaceVariant
            )
        }
    }
}
