package pt.ipt.dama2026.mygarage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import pt.ipt.dama2026.mygarage.R
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors

@OptIn(ExperimentalMaterial3Api::class)
/** Barra superior com avatar e nome da garagem. */
@Composable
fun AtelierTopBar(
    garageName: String = "My Garage",
    avatarModel: Any? = null,
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
                    if (avatarModel != null) {
                        SubcomposeAsyncImage(
                            model = avatarModel,
                            contentDescription = stringResource(id = R.string.profile_avatar_description),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            loading = { ShimmerPlaceholder() },
                            error = { GradientPlaceholder() }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(id = R.string.profile_description),
                            tint = MyGarageColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
