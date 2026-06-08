package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtelierTopBar(
    garageName: String = "My Garage",
    onAvatarClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MyGarageColors.surfaceContainerHigh)
                        .then(
                            if (onAvatarClick != null) Modifier.clickable(onClick = onAvatarClick)
                            else Modifier
                        ),
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

                Text(
                    text = garageName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MyGarageColors.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MyGarageColors.surface.copy(alpha = 0.8f),
            titleContentColor = MyGarageColors.onBackground,
            actionIconContentColor = MyGarageColors.primary
        )
    )
}
