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

import androidx.compose.foundation.pager.PagerState
@Composable
fun AtelierBottomNav(
    items: List<Screen>,
    pagerState: PagerState,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MyGarageColors.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, screen ->
            val selected = pagerState.currentPage == index

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
