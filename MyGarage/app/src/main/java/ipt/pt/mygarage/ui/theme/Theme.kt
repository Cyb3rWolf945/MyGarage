package ipt.pt.mygarage.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyGarageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = MyGarageColors.primary,
        onPrimary = MyGarageColors.surfaceContainerLowest, // White
        primaryContainer = MyGarageColors.primaryContainer,
        background = MyGarageColors.background,
        onBackground = MyGarageColors.onBackground,
        surface = MyGarageColors.surface,
        onSurface = MyGarageColors.onSurface,
        surfaceVariant = MyGarageColors.surfaceContainerHighest,
        onSurfaceVariant = MyGarageColors.onSurfaceVariant,
        outlineVariant = MyGarageColors.outlineVariant,
        inverseSurface = MyGarageColors.inverseSurface,
        error = MyGarageColors.error,
        onError = MyGarageColors.onError
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}