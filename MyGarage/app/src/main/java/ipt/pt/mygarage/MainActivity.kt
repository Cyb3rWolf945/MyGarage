package ipt.pt.mygarage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ipt.pt.mygarage.presentation.garage.GarageViewModel
import ipt.pt.mygarage.presentation.main.MainViewModel
import ipt.pt.mygarage.presentation.onboarding.OnboardingViewModel
import ipt.pt.mygarage.presentation.auth.AuthViewModel
import ipt.pt.mygarage.presentation.profile.ProfileViewModel
import ipt.pt.mygarage.presentation.profile.VehicleProfileViewModel
import ipt.pt.mygarage.presentation.service.ServiceViewModel
import ipt.pt.mygarage.data.repository.UserPreferencesRepository
import ipt.pt.mygarage.data.repository.SyncRepository
import ipt.pt.mygarage.data.sync.SyncWorker
import ipt.pt.mygarage.domain.locale.DistanceFormatter
import kotlinx.coroutines.flow.firstOrNull
import ipt.pt.mygarage.ui.components.AtelierBottomNav
import ipt.pt.mygarage.ui.components.AtelierTopBar
import ipt.pt.mygarage.ui.screens.AboutScreen
import ipt.pt.mygarage.ui.screens.AuthScreen
import ipt.pt.mygarage.ui.screens.CameraScreen
import ipt.pt.mygarage.ui.screens.GarageScreen
import ipt.pt.mygarage.ui.screens.OnboardingScreen
import ipt.pt.mygarage.ui.screens.ProfileScreen
import ipt.pt.mygarage.ui.screens.ServiceScreen
import ipt.pt.mygarage.ui.screens.VehicleProfileScreen
import ipt.pt.mygarage.ui.screens.vehicleprofile.ServiceHistoryItem
import ipt.pt.mygarage.ui.screens.vehicleprofile.VehicleProfileUiState
import ipt.pt.mygarage.ui.theme.MyGarageColors
import ipt.pt.mygarage.ui.theme.MyGarageTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelResId: Int, val iconResId: Int) {
    object Garage : Screen("garage", R.string.nav_garage, R.drawable.ic_garage)
    object Camera : Screen("camera", R.string.nav_camera, R.drawable.ic_camera)
    object Service : Screen("service", R.string.nav_service, R.drawable.ic_service)
}

private val bottomNavItems = listOf(Screen.Garage, Screen.Camera, Screen.Service)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val mainViewModel by lazy { MainViewModel(application) }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                mainViewModel.isLoading.value
            }
        }
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyGarageTheme {
                MainScreen(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyGarageApplication
    val repository = app.repository
    val prefsRepo = remember { UserPreferencesRepository(context) }

    // ── Periodic cloud sync for authenticated users ───────────────────────
    LaunchedEffect(Unit) {
        val token = prefsRepo.userAuthTokenFlow.firstOrNull()
        if (!token.isNullOrBlank()) {
            SyncWorker.enqueuePeriodicSync(context)
        }
    }

    // ── Pull profile (avatar) immediately on login ───────────────────────
    val authToken by prefsRepo.userAuthTokenFlow.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(authToken) {
        if (!authToken.isNullOrBlank()) {
            val syncRepo = SyncRepository(context)
            syncRepo.pullAndSyncUserProfile()
        }
    }

    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val topBarAvatarFileName by mainViewModel.avatarFileName.collectAsStateWithLifecycle()
    val topBarAvatarRemoteUrl by mainViewModel.avatarRemoteUrl.collectAsStateWithLifecycle()

    val garageViewModel: GarageViewModel = viewModel()
    val serviceViewModel: ServiceViewModel = viewModel(
        factory = ServiceViewModel.factory(
            repository,
            UserPreferencesRepository(context),
            app
        )
    )

    // ── Garage state ──────────────────────────────────────────────────────
    val vehicles by garageViewModel.vehiclesState.collectAsState()
    val garageFormErrors by garageViewModel.formErrors.collectAsState()
    val garageShowDelete by garageViewModel.showDeleteConfirmation.collectAsState()
    val garageVehicleToDelete by garageViewModel.vehicleToDelete.collectAsState()
    val garageSelectedForOptions by garageViewModel.selectedVehicleForOptions.collectAsState()
    val garageVehicleToEdit by garageViewModel.vehicleToEdit.collectAsState()
    val garageState by garageViewModel.uiState.collectAsStateWithLifecycle()
    val imageStorageManager = app.imageStorageManager
    val topBarAvatarFile = topBarAvatarFileName?.let { imageStorageManager.getImagePath(it) }?.let { java.io.File(it) }
    val topBarAvatarModel: Any? = topBarAvatarFile ?: topBarAvatarRemoteUrl
        ?.replace("\"", "")
        ?.let { ipt.pt.mygarage.data.network.NetworkModule.buildImageProxyUrl(context, it) }

    // ── Service state ─────────────────────────────────────────────────────
    val selectedVehicleId by serviceViewModel.selectedVehicleId.collectAsState()
    val selectedVehicleWithServices by serviceViewModel.selectedVehicleWithServices.collectAsState()
    val temporaryParts by serviceViewModel.temporaryParts.collectAsState()
    val serviceFormErrors by serviceViewModel.formErrors.collectAsState()
    val serviceSelectedLogForOptions by serviceViewModel.selectedLogForOptions.collectAsState()
    val serviceLogToDelete by serviceViewModel.logToDelete.collectAsState()

    // ── Unified Dialog state ──────────────────────────────────────────────
    val serviceDialogMode by serviceViewModel.dialogMode.collectAsState()
    val serviceSelectedLog by serviceViewModel.selectedLog.collectAsState()
    val serviceSelectedLogParts by serviceViewModel.selectedLogParts.collectAsState()

    // ── Form field state ──────────────────────────────────────────────────
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

    // Select the first vehicle by default once the vehicles list is populated
    LaunchedEffect(vehicles) {
        if (selectedVehicleId == null && vehicles.isNotEmpty()) {
            serviceViewModel.selectVehicle(vehicles.first().id)
        }
    }

    // Wait until MainViewModel resolves the start destination
    val resolvedStart = startDestination
    if (resolvedStart == null || isLoading) {
        // Show a minimal branded loading indicator while the splash is fading
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

    // Determine if we are on an onboarding or auth screen (hide chrome)
    val isOnboardingRoute = currentRoute == MainViewModel.ROUTE_ONBOARDING_GRAPH || currentRoute == "auth_graph"

    Scaffold(
        topBar = {
            // Hide the top bar during onboarding
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
            // Show bottom nav only on the garage graph pager
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
            // ── Onboarding Graph ─────────────────────────────────────────
            composable(MainViewModel.ROUTE_ONBOARDING_GRAPH) {
                val onboardingViewModel: OnboardingViewModel = viewModel()
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

            // ── Auth Graph ──────────────────────────────────────────────
            composable("auth_graph") {
                val authViewModel: AuthViewModel = viewModel()
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(MainViewModel.ROUTE_GARAGE_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Garage Graph (main pager) ─────────────────────────────────
            composable(MainViewModel.ROUTE_GARAGE_GRAPH) {
                var duplicateVehicleFound by remember { mutableStateOf<ipt.pt.mygarage.data.local.entity.VehicleEntity?>(null) }

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
                                imageStorageManager = imageStorageManager,
                                // ── Long-Press Options Menu ──────────────────
                                selectedVehicleForOptions = garageSelectedForOptions,
                                onVehicleLongPressed = garageViewModel::onVehicleLongPressed,
                                onDismissOptionsMenu = garageViewModel::onDismissOptionsMenu,
                                onSelectEdit = garageViewModel::onSelectEdit,
                                onSelectDelete = garageViewModel::onSelectDelete,
                                // ── Edit Dialog State ────────────────────────
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
                                    // Convert API data to Vehicle model with compatible fields only
                                    garageViewModel.openAddDialogWithData(
                                        plate = vehicleData.plate,
                                        name = vehicleData.vehicleModel ?: "",
                                        year = vehicleData.year ?: "",
                                        fuelType = vehicleData.fuelType ?: "",
                                        engineCapacity = vehicleData.engineCapacity ?: ""
                                    )
                                    // Navigate back to Garage tab
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
                            // ── Unified Dialog State ────────────────────
                            dialogMode = serviceDialogMode,
                            selectedLog = serviceSelectedLog,
                            selectedLogParts = serviceSelectedLogParts,
                            // ── Form state & validation ─────────────────
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
                            // ── Dialog Intents ──────────────────────────
                            onAddFabClicked = serviceViewModel::onAddFabClicked,
                            onLogClicked = serviceViewModel::onLogClicked,
                            onSave = serviceViewModel::onSaveServiceLog,
                            onDismissDialog = serviceViewModel::onDismissDialog,
                            // ── Long-Press Options Menu ─────────────────
                            selectedLogForOptions = serviceSelectedLogForOptions,
                            onLogLongPressed = serviceViewModel::onLogLongPressed,
                            onDismissOptionsMenu = serviceViewModel::onDismissOptionsMenu,
                            onSelectEdit = serviceViewModel::onSelectEdit,
                            onSelectDelete = serviceViewModel::onSelectDelete,
                            // ── Delete Confirmation Dialog ──────────────
                            logToDelete = serviceLogToDelete,
                            onDismissDeleteDialog = serviceViewModel::onDismissDeleteDialog,
                            onConfirmDeleteLog = serviceViewModel::onConfirmDeleteLog
                        )
                        }
                    }
                }
            }

            // ── Vehicle Profile ──────────────────────────────────────────
            composable("vehicle_profile/{vehicleId}") { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                val profileViewModel: VehicleProfileViewModel = viewModel(
                    factory = VehicleProfileViewModel.factory(
                        repository,
                        app.locationManager,
                        UserPreferencesRepository(context),
                        app
                    )
                )

                LaunchedEffect(vehicleId) {
                    profileViewModel.loadVehicle(vehicleId)
                }

                val vehicleWithServices by profileViewModel.uiState.collectAsState()
                val profileFormErrors by profileViewModel.formErrors.collectAsState()
                val profileShowDelete by profileViewModel.showDeleteConfirmation.collectAsState()
                val profileDeleteCompleted by profileViewModel.deleteCompleted.collectAsState()
                val isCarouselVisible by profileViewModel.isCarouselVisible.collectAsState()
                val carouselStartIndex by profileViewModel.carouselStartIndex.collectAsState()

                vehicleWithServices?.let { ws ->
                    val profileResolvedUnit by profileViewModel.resolvedDistanceUnit.collectAsState()

                    val uiState = remember(ws, profileFormErrors, profileResolvedUnit) {
                        val displayMileage = if (ws.vehicle.mileageKm > 0.0) {
                            String.format(java.util.Locale.US, "%,d",
                                DistanceFormatter.forDisplay(ws.vehicle.mileageKm, profileResolvedUnit).toLong()
                            )
                        } else ws.vehicle.mileage

                        val displayMileageToNextService = if (ws.vehicle.mileageToNextService != null) {
                            val nextServiceStr = ws.vehicle.mileageToNextService ?: ""
                            val parsed = DistanceFormatter.parseUserInput(nextServiceStr)
                            if (parsed > 0.0) {
                                String.format(java.util.Locale.US, "%,d",
                                    DistanceFormatter.forDisplay(parsed, profileResolvedUnit).toLong()
                                )
                            } else nextServiceStr
                        } else null

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
                            mileageToNextService = displayMileageToNextService,
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
                                ServiceHistoryItem(
                                    title = log.description,
                                    subtitle = context.getString(
                                        R.string.timeline_subtitle,
                                        log.mileage,
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
                        onFetchLocationClicked = profileViewModel::onFetchLocationClicked
                    )
                }
            }

            // ── Profile Screen ───────────────────────────────────────────
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel()
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
                    }
                )
            }

            // ── About Screen ─────────────────────────────────────────────
            composable("about") {
                AboutScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
