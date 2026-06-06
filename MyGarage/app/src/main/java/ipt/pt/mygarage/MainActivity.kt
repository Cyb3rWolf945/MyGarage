package ipt.pt.mygarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ipt.pt.mygarage.presentation.garage.GarageViewModel
import ipt.pt.mygarage.presentation.profile.VehicleProfileViewModel
import ipt.pt.mygarage.presentation.service.ServiceViewModel
import ipt.pt.mygarage.ui.components.AtelierBottomNav
import ipt.pt.mygarage.ui.components.AtelierTopBar
import ipt.pt.mygarage.ui.screens.CameraScreen
import ipt.pt.mygarage.ui.screens.GarageScreen
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
    val context = LocalContext.current
    val app = context.applicationContext as MyGarageApplication
    val repository = app.repository

    val garageViewModel: GarageViewModel = viewModel(factory = GarageViewModel.factory(repository))
    val serviceViewModel: ServiceViewModel = viewModel(factory = ServiceViewModel.factory(repository))

    val vehicles by garageViewModel.vehiclesState.collectAsState()
    val selectedVehicleId by serviceViewModel.selectedVehicleId.collectAsState()
    val selectedVehicleWithServices by serviceViewModel.selectedVehicleWithServices.collectAsState()
    val temporaryParts by serviceViewModel.temporaryParts.collectAsState()

    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    val servicePageIndex = bottomNavItems.indexOf(Screen.Service)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Select the first vehicle by default once vehicles list is populated
    LaunchedEffect(vehicles) {
        if (selectedVehicleId == null && vehicles.isNotEmpty()) {
            serviceViewModel.selectVehicle(vehicles.first().id)
        }
    }

    Scaffold(
        topBar = {
            AtelierTopBar()
        },
        bottomBar = {
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
                            vehicles = vehicles,
                            onVehicleClick = { vehicleId ->
                                navController.navigate("vehicle_profile/$vehicleId")
                            },
                            onAddVehicleClick = { newVehicle ->
                                garageViewModel.insertVehicle(newVehicle)
                            }
                        )
                        Screen.Camera -> CameraScreen()
                        Screen.Service -> ServiceScreen(
                            vehicles = vehicles,
                            selectedVehicleId = selectedVehicleId,
                            selectedVehicleWithServices = selectedVehicleWithServices,
                            temporaryParts = temporaryParts,
                            onVehicleSelected = { vehicleId ->
                                serviceViewModel.selectVehicle(vehicleId)
                            },
                            onLogService = { serviceLog ->
                                serviceViewModel.insertServiceLog(serviceLog)
                            },
                            onLogServiceWithParts = { serviceLog ->
                                serviceViewModel.insertServiceLogWithParts(serviceLog)
                            },
                            onAddTemporaryPart = { name, quantity, reference ->
                                serviceViewModel.addTemporaryPart(name, quantity, reference)
                            },
                            onRemoveTemporaryPart = { partId ->
                                serviceViewModel.removeTemporaryPart(partId)
                            }
                        )
                    }
                }
            }
            composable("vehicle_profile/{vehicleId}") { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                val profileViewModel: VehicleProfileViewModel = viewModel(
                    factory = VehicleProfileViewModel.factory(repository)
                )

                LaunchedEffect(vehicleId) {
                    profileViewModel.loadVehicle(vehicleId)
                }

                val vehicleWithServices by profileViewModel.uiState.collectAsState()

                vehicleWithServices?.let { ws ->
                    val uiState = remember(ws) {
                        VehicleProfileUiState(
                            name = ws.vehicle.name,
                            year = ws.vehicle.year,
                            mileage = ws.vehicle.mileage,
                            inspectionDate = ws.vehicle.inspectionDate,
                            oilType = ws.vehicle.oilType,
                            owner = ws.vehicle.owner,
                            seatCount = ws.vehicle.seatCount,
                            doorCount = ws.vehicle.doorCount,
                            fuelType = ws.vehicle.fuelType,
                            engineCapacity = ws.vehicle.engineCapacity,
                            iucValue = ws.vehicle.iucValue,
                            mileageToNextService = ws.vehicle.mileageToNextService,
                            locationAddress = ws.vehicle.locationAddress,
                            serviceHistory = ws.services.map { log ->
                                ServiceHistoryItem(
                                    title = log.description,
                                    subtitle = "Completed at ${log.mileage} - Date: ${log.date} [Type: ${log.type}]"
                                )
                            }
                        )
                    }

                    VehicleProfileScreen(
                        uiState = uiState,
                        vehicleEntity = ws.vehicle,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToService = {
                            serviceViewModel.selectVehicle(ws.vehicle.id)
                            coroutineScope.launch {
                                if (navController.currentDestination?.route != "main_pager") {
                                    navController.popBackStack("main_pager", inclusive = false)
                                }
                                if (servicePageIndex >= 0) {
                                    pagerState.animateScrollToPage(servicePageIndex)
                                }
                            }
                        },
                        onUpdateVehicle = { updatedVehicle ->
                            profileViewModel.updateVehicle(updatedVehicle)
                        }
                    )
                }
            }
        }
    }
}