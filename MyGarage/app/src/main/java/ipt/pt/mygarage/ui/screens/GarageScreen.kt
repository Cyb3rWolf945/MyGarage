package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.components.VehicleCard
import ipt.pt.mygarage.ui.theme.MyGarageColors

@Composable
fun GarageScreen(
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
            // Editorial subtitle — spec-sheet label style
            Text(
                text = "VEHICLE LOG & OVERVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Asymmetric off-centre screen title
            Text(
                text = stringResource(id = R.string.nav_garage),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            VehicleCard(
                model = "Porsche 911 GT3 RS",
                plate = "911-GT3-RS",
                status = "READY",
                statusColor = MyGarageColors.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            VehicleCard(
                model = "BMW M4 Competition",
                plate = "BMW-M4-COMP",
                status = "IN SERVICE",
                statusColor = MyGarageColors.onSurfaceVariant
            )
        }
    }
}
