package ipt.pt.mygarage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    onVehicleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MyGarageColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "VEHICLE LOG & OVERVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MyGarageColors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.nav_garage),
                style = MaterialTheme.typography.displayLarge,
                color = MyGarageColors.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VehicleCard(
                        model = "Porsche 911 GT3 RS",
                        plate = "911-GT3-RS",
                        status = "READY",
                        statusColor = MyGarageColors.primary,
                        onClick = { onVehicleClick("Porsche 911 GT3 RS") }
                    )
                }

                item {
                    VehicleCard(
                        model = "BMW M4 Competition",
                        plate = "BMW-M4-COMP",
                        status = "IN SERVICE",
                        statusColor = MyGarageColors.onSurfaceVariant,
                        onClick = { onVehicleClick("BMW M4 Competition") }
                    )
                }
            }
        }
    }
}

