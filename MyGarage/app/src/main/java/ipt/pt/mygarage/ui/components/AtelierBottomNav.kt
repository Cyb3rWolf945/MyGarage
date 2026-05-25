package ipt.pt.mygarage.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ipt.pt.mygarage.Screen
import ipt.pt.mygarage.ui.theme.MyGarageColors

/**
 * The Mechanical Atelier bottom navigation bar.
 *
 * Design spec:
 *  - Racing Blue (#0040a1) active indicator, zero tonal elevation (no drop-shadow)
 *  - White icon on selected state; muted onSurfaceVariant when unselected
 *  - surfaceContainerLow background to obey the tonal layering hierarchy
 *
 * @param items       The ordered list of [Screen] destinations.
 * @param currentRoute The currently active back-stack route, used to compute selection state.
 * @param onItemClick  Called with the clicked [Screen] so the caller can drive navigation.
 */
@Composable
fun AtelierBottomNav(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MyGarageColors.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route

            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(screen) },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = stringResource(id = screen.labelResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.labelResId),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MyGarageColors.surfaceContainerLowest,
                    selectedTextColor = MyGarageColors.primary,
                    indicatorColor = MyGarageColors.primary,
                    unselectedIconColor = MyGarageColors.onSurfaceVariant,
                    unselectedTextColor = MyGarageColors.onSurfaceVariant
                )
            )
        }
    }
}
