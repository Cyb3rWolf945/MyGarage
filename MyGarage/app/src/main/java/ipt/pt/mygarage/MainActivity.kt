package ipt.pt.mygarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ipt.pt.mygarage.ui.components.AtelierBottomNav
import ipt.pt.mygarage.ui.components.AtelierTopBar
import ipt.pt.mygarage.ui.screens.CameraScreen
import ipt.pt.mygarage.ui.screens.GarageScreen
import ipt.pt.mygarage.ui.screens.ServiceScreen
import ipt.pt.mygarage.ui.theme.MyGarageColors
import ipt.pt.mygarage.ui.theme.MyGarageTheme

// ── Destinations ──────────────────────────────────────────────────────────────

sealed class Screen(val route: String, val labelResId: Int, val iconResId: Int) {
    object Garage : Screen("garage", R.string.nav_garage, R.drawable.ic_garage)
    object Camera : Screen("camera", R.string.nav_camera, R.drawable.ic_camera)
    object Service : Screen("service", R.string.nav_service, R.drawable.ic_service)
}

private val bottomNavItems = listOf(Screen.Garage, Screen.Camera, Screen.Service)

// ── Activity ──────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGarageTheme {
                MainScreen()
            }
        }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────

/**
 * Single entry-point composable. Owns the NavController and the Scaffold shell.
 * All UI is delegated to the dedicated component and screen composables.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            AtelierTopBar()
        },
        bottomBar = {
            AtelierBottomNav(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = MyGarageColors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Garage.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Garage.route)  { GarageScreen() }
            composable(Screen.Camera.route)  { CameraScreen() }
            composable(Screen.Service.route) { ServiceScreen() }
        }
    }
}