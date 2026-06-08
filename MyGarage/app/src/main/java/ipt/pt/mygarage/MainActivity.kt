package ipt.pt.mygarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ipt.pt.mygarage.presentation.garage.GarageViewModel
import ipt.pt.mygarage.presentation.profile.ProfileViewModel
import ipt.pt.mygarage.ui.components.AtelierBottomNav
import ipt.pt.mygarage.ui.components.AtelierTopBar
import ipt.pt.mygarage.ui.screens.CameraScreen
import ipt.pt.mygarage.ui.screens.GarageScreen
import ipt.pt.mygarage.ui.screens.ProfileScreen
import ipt.pt.mygarage.ui.screens.ServiceScreen
import ipt.pt.mygarage.ui.screens.VehicleProfileScreen
import ipt.pt.mygarage.ui.screens.vehicleprofile.ServiceHistoryItem
import ipt.pt.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState
import ipt.pt.mygarage.ui.theme.MyGarageColors
import ipt.pt.mygarage.ui.theme.MyGarageTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelResId: Int, val iconResId: Int) {
    object Garage : Screen("garage", R.string.nav_garage, R.drawable.ic_garage)
    object Camera : Screen("camera", R.string.nav_camera, R.drawable.ic_camera)
    object Service : Screen("service", R.string.nav_service, R.drawable.ic_service)
}

private val bottomNavItems = listOf(Screen.Garage, Screen.Camera, Screen.Service)

class MainActivity : ComponentActivity() {
    private var isReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { !isReady }
        }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(2_000)
            isReady = true
        }

        enableEdgeToEdge()
        setContent {
            MyGarageTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    val servicePageIndex = bottomNavItems.indexOf(Screen.Service)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val garageViewModel: GarageViewModel = viewModel()
    val garageState by garageViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AtelierTopBar(
                garageName = garageState.garageName,
                onAvatarClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentRoute == null || currentRoute == "main_pager") {
                AtelierBottomNav(
                    items = bottomNavItems,
                    pagerState = pagerState,
                    onItemClick = { screen ->
                        val pageIndex = bottomNavItems.indexOf(screen)
                        if (pageIndex >= 0) {
                            coroutineScope.launch {
                                if (currentRoute != "main_pager" && currentRoute != null) {
                                    navController.popBackStack("main_pager", inclusive = false)
                                }
                                pagerState.animateScrollToPage(pageIndex)
                            }
                        }
                    }
                )
            }
        },
        containerColor = MyGarageColors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main_pager",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("main_pager") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (bottomNavItems[page]) {
                        Screen.Garage -> GarageScreen(
                            onVehicleClick = { vehicleName ->
                                navController.navigate("vehicle_profile/$vehicleName")
                            }
                        )
                        Screen.Camera -> CameraScreen()
                        Screen.Service -> ServiceScreen()
                    }
                }
            }
            composable("vehicle_profile/{vehicleName}") { backStackEntry ->
                val vehicleName = backStackEntry.arguments?.getString("vehicleName") ?: "Porsche 911"

                val uiState = remember(vehicleName) {
                    val isPorsche = vehicleName.contains("Porsche", ignoreCase = true)
                    if (isPorsche) {
                        VehicleProfileUiState(
                            modelName = "Porsche 911",
                            year = "2024",
                            mileage = "12,450 mi",
                            inspectionDate = "15/11/2026",
                            oilType = "0W-40 Synthetic",
                            owner = "Private Owner",
                            seatCount = "4",
                            doorCount = "2",
                            fuelType = "Petrol",
                            engineCapacity = "3,000 cc",
                            iucValue = "218",
                            mileageToNextService = "8,200 mi",
                            serviceHistory = listOf(
                                ServiceHistoryItem("Full Service & Oil Change", "Atelier Stuttgart Service Center"),
                                ServiceHistoryItem("Tire Rotation & Balance", "Michelin Certified Partner")
                            ),
                            locationAddress = "Porscheplatz 1, 70435 Stuttgart, Germany"
                        )
                    } else {
                        VehicleProfileUiState(
                            modelName = "BMW M4 Competition",
                            year = "2023",
                            mileage = "8,920 mi",
                            inspectionDate = "02/09/2026",
                            oilType = "5W-30 Synthetic",
                            owner = "Private Owner",
                            seatCount = "4",
                            doorCount = "2",
                            fuelType = "Petrol",
                            engineCapacity = "3,000 cc",
                            iucValue = "196",
                            mileageToNextService = "6,500 mi",
                            serviceHistory = listOf(
                                ServiceHistoryItem("Break-in Service", "Atelier Munich Service Center"),
                                ServiceHistoryItem("Brake Fluid Flush", "BMW Certified Service")
                            ),
                            locationAddress = "Petuelring 130, 80809 Munich, Germany"
                        )
                    }
                }

                VehicleProfileScreen(
                    uiState = uiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToService = {
                        coroutineScope.launch {
                            if (currentRoute != "main_pager" && currentRoute != null) {
                                navController.popBackStack("main_pager", inclusive = false)
                            }
                            if (servicePageIndex >= 0) {
                                pagerState.animateScrollToPage(servicePageIndex)
                            }
                        }
                    }
                )
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToGarage = {
                        navController.popBackStack("main_pager", inclusive = false)
                    }
                )
            }
        }
    }
}