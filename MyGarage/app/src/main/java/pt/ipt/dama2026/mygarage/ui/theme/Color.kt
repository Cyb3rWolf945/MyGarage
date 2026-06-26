package pt.ipt.dama2026.mygarage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import pt.ipt.dama2026.mygarage.R
object MyGarageColors {
    val primary: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.primary)

    val primaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.primary_container)

    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.background)

    val onBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.on_background)

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface)

    val onSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.on_surface)

    val surfaceContainerLowest: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface_container_lowest)

    val surfaceContainerLow: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface_container_low)

    val surfaceContainerHigh: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface_container_high)

    val surfaceContainerHighest: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface_container_highest)

    val surfaceDim: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.surface_dim)

    val outlineVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.outline_variant)

    val onSurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.on_surface_variant)

    val inverseSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.inverse_surface)

    val error: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.error)

    val onError: Color
        @Composable
        @ReadOnlyComposable
        get() = colorResource(id = R.color.on_error)
}