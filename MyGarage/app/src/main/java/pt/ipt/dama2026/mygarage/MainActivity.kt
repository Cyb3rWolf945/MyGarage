package pt.ipt.dama2026.mygarage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ipt.dama2026.mygarage.presentation.garage.GarageViewModel
import pt.ipt.dama2026.mygarage.presentation.main.MainViewModel
import pt.ipt.dama2026.mygarage.presentation.onboarding.OnboardingViewModel
import pt.ipt.dama2026.mygarage.presentation.profile.ProfileViewModel
import pt.ipt.dama2026.mygarage.presentation.profile.VehicleProfileViewModel
import pt.ipt.dama2026.mygarage.presentation.service.ServiceViewModel
import pt.ipt.dama2026.mygarage.data.sync.SyncWorker
import pt.ipt.dama2026.mygarage.domain.locale.DistanceFormatter
import kotlinx.coroutines.flow.firstOrNull
import pt.ipt.dama2026.mygarage.ui.components.AtelierBottomNav
import pt.ipt.dama2026.mygarage.ui.components.AtelierTopBar
import pt.ipt.dama2026.mygarage.ui.screens.AboutScreen
import pt.ipt.dama2026.mygarage.ui.screens.TermsScreen
import pt.ipt.dama2026.mygarage.ui.screens.AuthScreen
import pt.ipt.dama2026.mygarage.ui.screens.CameraScreen
import pt.ipt.dama2026.mygarage.ui.screens.GarageScreen
import pt.ipt.dama2026.mygarage.ui.screens.OnboardingScreen
import pt.ipt.dama2026.mygarage.ui.screens.ProfileScreen
import pt.ipt.dama2026.mygarage.ui.screens.ServiceScreen
import pt.ipt.dama2026.mygarage.ui.screens.SplashScreen
import pt.ipt.dama2026.mygarage.ui.screens.VehicleProfileScreen
import pt.ipt.dama2026.mygarage.ui.screens.vehicleprofile.ServiceHistoryItem
import pt.ipt.dama2026.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageColors
import pt.ipt.dama2026.mygarage.ui.theme.MyGarageTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelResId: Int, val iconResId: Int) {
    object Garage : Screen("garage", R.string.nav_garage, R.drawable.ic_garage)
    object Camera : Screen("camera", R.string.nav_camera, R.drawable.ic_camera)
    object Service : Screen("service", R.string.nav_service, R.drawable.ic_service)
}

private val bottomNavItems = listOf(Screen.Garage, Screen.Camera, Screen.Service)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyGarageTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()

                Crossfade(targetState = isLoading, label = "splash_crossfade") { loading ->
                    if (loading) {
                        SplashScreen()
                    } else {
                        MainScreen(mainViewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val token = mainViewModel.authToken.firstOrNull()
        if (!token.isNullOrBlank()) {
            SyncWorker.enqueuePeriodicSync(context)
        }
    }

    val authToken by mainViewModel.authToken.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(authToken) {
        if (!authToken.isNullOrBlank()) {
            mainViewModel.pullUserProfile()
        }
    }

    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val topBarAvatarFileName by mainViewModel.avatarFileName.collectAsStateWithLifecycle()
    val topBarAvatarRemoteUrl by mainViewModel.avatarRemoteUrl.collectAsStateWithLifecycle()
    val topBarAvatarLocalFile by mainViewModel.avatarLocalFile.collectAsStateWithLifecycle()

    val garageViewModel: GarageViewModel = hiltViewModel()
    val serviceViewModel: ServiceViewModel = hiltViewModel()

    val garageState by garageViewModel.uiState.collectAsStateWithLifecycle()
    val vehicles by garageViewModel.vehiclesState.collectAsState()
    val garageFormErrors by garageViewModel.formErrors.collectAsState()
    val garageShowDelete by garageViewModel.showDeleteConfirmation.collectAsState()
    val garageVehicleToDelete by garageViewModel.vehicleToDelete.collectAsState()
    val garageSelectedForOptions by garageViewModel.selectedVehicleForOptions.collectAsState()
    val garageVehicleToEdit by garageViewModel.vehicleToEdit.collectAsState()
    val topBarAvatarModel: Any? = topBarAvatarLocalFile ?: topBarAvatarRemoteUrl
        ?.replace("\"", "")
        ?.let { pt.ipt.dama2026.mygarage.data.network.NetworkModule.buildImageProxyUrl(context, it) }


    val selectedVehicleId by serviceViewModel.selectedVehicleId.collectAsState()
    val selectedVehicleWithServices by serviceViewModel.selectedVehicleWithServices.collectAsState()
    val temporaryParts by serviceViewModel.temporaryParts.collectAsState()
    val serviceFormErrors by serviceViewModel.formErrors.collectAsState()
    val serviceSelectedLogForOptions by serviceViewModel.selectedLogForOptions.collectAsState()
    val serviceLogToDelete by serviceViewModel.logToDelete.collectAsState()

    val serviceDialogMode by serviceViewModel.dialogMode.collectAsState()
    val serviceSelectedLog by serviceViewModel.selectedLog.collectAsState()
    val serviceSelectedLogParts by serviceViewModel.selectedLogParts.collectAsState()

    val serviceDate by serviceViewModel.serviceDate.collectAsState()
    val serviceDescription by serviceViewModel.description.collectAsState()
    val serviceMileage by serviceViewModel.mileage.collectAsState()
    val serviceSelectedType by serviceViewModel.selectedType.collectAsState()

    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    val servicePageIndex = bottomNavItems.indexOf(Screen.Service)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(vehicles) {
        if (selectedVehicleId == null && vehicles.isNotEmpty()) {
            serviceViewModel.selectVehicle(vehicles.first().id)
        }
    }

    val resolvedStart = startDestination
    if (resolvedStart == null || isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MyGarageColors.primary,
                strokeWidth = 2.dp
            )
        }
        return
    }

    val isOnboardingRoute = currentRoute == MainViewModel.ROUTE_ONBOARDING_GRAPH || currentRoute == "auth_graph"

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isOnboardingRoute,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AtelierTopBar(
                    garageName = garageState.garageName,
                    avatarModel = topBarAvatarModel,
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
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute == MainViewModel.ROUTE_GARAGE_GRAPH || currentRoute == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AtelierBottomNav(
                    items = bottomNavItems,
                    pagerState = pagerState,
                    onItemClick = { screen ->
                        val pageIndex = bottomNavItems.indexOf(screen)
                        if (pageIndex >= 0) {
                            coroutineScope.launch {
                                if (currentRoute != MainViewModel.ROUTE_GARAGE_GRAPH && currentRoute != null) {
                                    navController.popBackStack(
                                        MainViewModel.ROUTE_GARAGE_GRAPH,
                                        inclusive = false
                                    )
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
            startDestination = resolvedStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(MainViewModel.ROUTE_ONBOARDING_GRAPH) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        navController.navigate(MainViewModel.ROUTE_GARAGE_GRAPH) {
                            popUpTo(MainViewModel.ROUTE_ONBOARDING_GRAPH) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate("auth_graph") {
                            popUpTo(MainViewModel.ROUTE_ONBOARDING_GRAPH) {
                                inclusive = true
                            }
                        }
                    }
                )
            }


            composable(
                route = "auth_graph?noBack={noBack}",
                arguments = listOf(
                    navArgument("noBack") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val noBack = backStackEntry.arguments?.getBoolean("noBack") ?: false
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(MainViewModel.ROUTE_GARAGE_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackClick = if (noBack) null else {
                        { navController.popBackStack() }
                    }
                )
            }


            composable(MainViewModel.ROUTE_GARAGE_GRAPH) {
                var duplicateVehicleFound by remember { mutableStateOf<pt.ipt.dama2026.mygarage.domain.model.Vehicle?>(null) }

                if (duplicateVehicleFound != null) {
                    val vehicleToView = duplicateVehicleFound
                    AlertDialog(
                        onDismissRequest = { duplicateVehicleFound = null },
                        title = { Text(stringResource(R.string.dialog_vehicle_already_exists_title)) },
                        text = { Text(stringResource(R.string.dialog_vehicle_already_exists_message, vehicleToView?.plate ?: "")) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val vehicleId = vehicleToView?.id ?: ""
                                    duplicateVehicleFound = null
                                    navController.navigate("vehicle_profile/$vehicleId")
                                }
                            ) {
                                Text(stringResource(R.string.dialog_action_view_vehicle), color = MyGarageColors.primary)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { duplicateVehicleFound = null }
                            ) {
                                Text(stringResource(R.string.dialog_action_cancel), color = MyGarageColors.onSurfaceVariant)
                            }
                        }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (bottomNavItems[page]) {
                        Screen.Garage -> {
                            val serviceResolvedUnit by serviceViewModel.resolvedDistanceUnit.collectAsState()
                            GarageScreen(
                                vehicles = vehicles,
                                onVehicleClick = { vehicleId ->
                                    navController.navigate("vehicle_profile/$vehicleId")
                                },
                                onAddVehicleClick = { newVehicle ->
                                    garageViewModel.insertVehicle(newVehicle)
                                },
                                onDeleteVehicle = { vehicle ->
                                    garageViewModel.showDeleteDialog(vehicle)
                                },
                                showDeleteConfirmation = garageShowDelete,
                                vehicleToDelete = garageVehicleToDelete,
                                onDismissDeleteDialog = garageViewModel::dismissDeleteDialog,
                                onConfirmDelete = garageViewModel::confirmDelete,
                                formErrors = garageFormErrors,
                                onFieldChanged = garageViewModel::clearFieldError,
                                selectedImageUri = garageState.selectedImageUri,
                                existingImageFileName = garageState.existingImageFileName,
                                onImageSelected = garageViewModel::onImageSelected,
                                imageStorageManager = garageViewModel.imageStorageManager,

                                selectedVehicleForOptions = garageSelectedForOptions,
                                onVehicleLongPressed = garageViewModel::onVehicleLongPressed,
                                onDismissOptionsMenu = garageViewModel::onDismissOptionsMenu,
                                onSelectEdit = garageViewModel::onSelectEdit,
                                onSelectDelete = garageViewModel::onSelectDelete,
                                vehicleToEdit = garageVehicleToEdit,
                                onDismissEditDialog = garageViewModel::onDismissEditDialog,
                                onConfirmEdit = garageViewModel::confirmEdit,
                                resolvedDistanceUnit = serviceResolvedUnit
                            )
                        }
                        Screen.Camera -> CameraScreen(
                            onVehicleDataReady = { vehicleData ->
                                val existingVehicle = vehicles.find {
                                    it.plate.equals(vehicleData.plate, ignoreCase = true)
                                }

                                if (existingVehicle != null) {
                                    duplicateVehicleFound = existingVehicle
                                } else {
                                    garageViewModel.openAddDialogWithData(
                                        plate = vehicleData.plate,
                                        name = vehicleData.vehicleModel ?: "",
                                        year = vehicleData.year ?: "",
                                        fuelType = vehicleData.fuelType ?: "",
                                        engineCapacity = vehicleData.engineCapacity ?: ""
                                    )
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                }
                            }
                        )
                        Screen.Service -> {
                            val serviceResolvedUnit by serviceViewModel.resolvedDistanceUnit.collectAsState()
                            ServiceScreen(
                            vehicles = vehicles,
                            selectedVehicleId = selectedVehicleId,
                            selectedVehicleWithServices = selectedVehicleWithServices,
                            resolvedDistanceUnit = serviceResolvedUnit,
                            temporaryParts = temporaryParts,
                            onVehicleSelected = { vehicleId ->
                                serviceViewModel.selectVehicle(vehicleId)
                            },
                            onAddTemporaryPart = { name, quantity, reference ->
                                serviceViewModel.addTemporaryPart(name, quantity, reference)
                            },
                            onRemoveTemporaryPart = { partId ->
                                serviceViewModel.removeTemporaryPart(partId)
                            },
                            dialogMode = serviceDialogMode,
                            selectedLog = serviceSelectedLog,
                            selectedLogParts = serviceSelectedLogParts,
                            formErrors = serviceFormErrors,
                            onFieldChanged = serviceViewModel::clearFieldError,
                            serviceDate = serviceDate,
                            description = serviceDescription,
                            mileage = serviceMileage,
                            selectedType = serviceSelectedType,
                            onDateChanged = serviceViewModel::onDateChanged,
                            onDescriptionChanged = serviceViewModel::onDescriptionChanged,
                            onMileageChanged = serviceViewModel::onMileageChanged,
                            onTypeChanged = serviceViewModel::onTypeChanged,
                            onAddFabClicked = serviceViewModel::onAddFabClicked,
                            onLogClicked = serviceViewModel::onLogClicked,
                            onSave = serviceViewModel::onSaveServiceLog,
                            onDismissDialog = serviceViewModel::onDismissDialog,
                            selectedLogForOptions = serviceSelectedLogForOptions,
                            onLogLongPressed = serviceViewModel::onLogLongPressed,
                            onDismissOptionsMenu = serviceViewModel::onDismissOptionsMenu,
                            onSelectEdit = serviceViewModel::onSelectEdit,
                            onSelectDelete = serviceViewModel::onSelectDelete,
                            logToDelete = serviceLogToDelete,
                            onDismissDeleteDialog = serviceViewModel::onDismissDeleteDialog,
                            onConfirmDeleteLog = serviceViewModel::onConfirmDeleteLog
                        )
                        }
                    }
                }
            }

            composable("vehicle_profile/{vehicleId}") { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                val profileViewModel: VehicleProfileViewModel = hiltViewModel()

                LaunchedEffect(vehicleId) {
                    profileViewModel.loadVehicle(vehicleId)
                }

                val vehicleWithServices by profileViewModel.uiState.collectAsState()
                val profileFormErrors by profileViewModel.formErrors.collectAsState()
                val profileShowDelete by profileViewModel.showDeleteConfirmation.collectAsState()
                val profileDeleteCompleted by profileViewModel.deleteCompleted.collectAsState()
                val isCarouselVisible by profileViewModel.isCarouselVisible.collectAsState()
                val carouselStartIndex by profileViewModel.carouselStartIndex.collectAsState()
                val profileResolvedUnit by profileViewModel.resolvedDistanceUnit.collectAsState()

                vehicleWithServices?.let { ws ->
                    val uiState = remember(ws, profileFormErrors, profileResolvedUnit) {
                        val displayMileage = if (ws.vehicle.mileageKm > 0.0) {
                            DistanceFormatter.formatDisplay(ws.vehicle.mileageKm, profileResolvedUnit)
                        } else ""

                        val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

                        VehicleProfileUiState(
                            name = ws.vehicle.name,
                            year = ws.vehicle.year,
                            mileage = displayMileage,
                            inspectionDate = ws.vehicle.inspectionDate,
                            oilType = ws.vehicle.oilType,
                            owner = ws.vehicle.owner,
                            seatCount = ws.vehicle.seatCount,
                            doorCount = ws.vehicle.doorCount,
                            fuelType = ws.vehicle.fuelType,
                            engineCapacity = ws.vehicle.engineCapacity,
                            iucValue = ws.vehicle.iucValue,

                            locationAddress = ws.vehicle.locationAddress,
                            latitude = ws.vehicle.latitude,
                            longitude = ws.vehicle.longitude,
                            serviceHistory = ws.services.map { log ->
                                val serviceDate = try {
                                    dateFormatter.parse(log.date)?.let {
                                        java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(it)
                                    } ?: log.date
                                } catch (e: Exception) {
                                    log.date
                                }
                                val historyMileage = DistanceFormatter.formatDisplay(log.mileageKm, profileResolvedUnit)
                                ServiceHistoryItem(
                                    title = log.description,
                                    subtitle = context.getString(
                                        R.string.timeline_subtitle,
                                        historyMileage,
                                        serviceDate,
                                        log.type
                                    )
                                )
                            },
                            formErrors = profileFormErrors
                        )
                    }

                    // Handle delete-completed navigation event
                    LaunchedEffect(profileDeleteCompleted) {
                        if (profileDeleteCompleted) {
                            profileViewModel.onDeleteCompletedHandled()
                            navController.popBackStack()
                        }
                    }

                    VehicleProfileScreen(
                        uiState = uiState,
                        vehicleEntity = ws.vehicle,
                        resolvedDistanceUnit = profileResolvedUnit,
                        isCarouselVisible = isCarouselVisible,
                        carouselStartIndex = carouselStartIndex,
                        onOpenCarousel = { index ->
                            profileViewModel.openCarousel(index)
                        },
                        onCloseCarousel = {
                            profileViewModel.closeCarousel()
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToService = {
                            serviceViewModel.selectVehicle(ws.vehicle.id)
                            coroutineScope.launch {
                                if (navController.currentDestination?.route != MainViewModel.ROUTE_GARAGE_GRAPH) {
                                    navController.popBackStack(
                                        MainViewModel.ROUTE_GARAGE_GRAPH,
                                        inclusive = false
                                    )
                                }
                                if (servicePageIndex >= 0) {
                                    pagerState.animateScrollToPage(servicePageIndex)
                                }
                            }
                        },
                        onUpdateVehicle = { updatedVehicle ->
                            profileViewModel.updateVehicle(updatedVehicle)
                        },
                        onDeleteVehicle = {
                            profileViewModel.showDeleteDialog()
                        },
                        showDeleteConfirmation = profileShowDelete,
                        onDismissDeleteDialog = profileViewModel::dismissDeleteDialog,
                        onConfirmDelete = profileViewModel::confirmDelete,
                        onFieldChanged = profileViewModel::clearFieldError,
                        onFetchLocationClicked = profileViewModel::onFetchLocationClicked,
                        imageStorageManager = profileViewModel.imageStorageManager,
                        locationManager = profileViewModel.locationManager
                    )
                }
            }

            composable("profile") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToGarage = {
                        navController.popBackStack(
                            MainViewModel.ROUTE_GARAGE_GRAPH,
                            inclusive = false
                        )
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onNavigateToAbout = {
                        navController.navigate("about") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate("auth_graph") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(MainViewModel.ROUTE_ONBOARDING_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }


            composable("about") {
                AboutScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToTerms = {
                        navController.navigate("terms")
                    }
                )
            }


            composable("terms") {
                TermsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
