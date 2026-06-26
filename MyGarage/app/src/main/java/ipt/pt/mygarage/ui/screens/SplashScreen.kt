package ipt.pt.mygarage.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipt.pt.mygarage.R
import ipt.pt.mygarage.ui.theme.MyGarageColors

@Composable
fun SplashScreen() {
    val ctx = LocalContext.current
    val icon = remember {
        BitmapFactory.decodeResource(ctx.resources, R.mipmap.ic_launcher_foreground)
            ?.asImageBitmap()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MyGarageColors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.requiredSize(170.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "MY GARAGE",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MyGarageColors.primary,
                letterSpacing = 4.sp
            )
        }
    }
}
