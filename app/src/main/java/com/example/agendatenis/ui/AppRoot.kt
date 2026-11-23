package com.example.agendatenis.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.di.DefaultAppContainer
import com.example.agendatenis.di.ViewModelFactory
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.TemaApp
import com.example.agendatenis.model.esCoachPrincipal
import com.example.agendatenis.navigation.*
import com.example.agendatenis.ui.screens.admin.*
import com.example.agendatenis.ui.screens.auth.LoginScreen
import com.example.agendatenis.ui.screens.auth.RegistroScreen
import com.example.agendatenis.ui.screens.auth.SplashScreen
import com.example.agendatenis.ui.screens.coach.*
import com.example.agendatenis.ui.theme.AgendaTenisTheme
import com.example.agendatenis.ui.viewmodel.admin.*
import com.example.agendatenis.ui.viewmodel.auth.AuthViewModel
import com.example.agendatenis.ui.viewmodel.coach.*

@Composable
fun AppRoot(
    repository: IAgendaTenisRepository = DefaultAppContainer.getInstance().repository,
    navController: NavHostController = rememberNavController()
) {
    val viewModelFactory = remember(repository) { ViewModelFactory(repository) }

    val usuarioActual by repository.usuarioActual.collectAsStateWithLifecycle()
    val configs by repository.configs.collectAsStateWithLifecycle()

    val configUsuario = usuarioActual?.let { configs[it.id] }
    val isDark = when (configUsuario?.tema) {
        TemaApp.OSCURO -> true
        TemaApp.CLARO -> false
        null -> isSystemInDarkTheme()
    }

    AgendaTenisTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val gestionCoachesContent: @Composable (() -> Unit) -> Unit = { onBack ->
                val gestionViewModel: GestionCoachesPrincipalViewModel = viewModel(factory = viewModelFactory)
                val gestionUiState by gestionViewModel.uiState.collectAsStateWithLifecycle()

                GestionCoachesPrincipalScreen(
                    uiState = gestionUiState,
                    onBack = onBack,
                    onNavigateToCrearHorarioCoach = { cId ->
                        navController.navigate(Routes.formHorario(coachId = cId))
                    },
                    onNavigateToEditarHorarioCoach = { cId, hId ->
                        navController.navigate(Routes.formHorario(coachId = cId, horarioId = hId))
                    },
                    onNavigateToDetalleJugador = { jId ->
                        navController.navigate(Routes.detalleJugador(jId))
                    },
                    onSetJugadorParaReasignar = gestionViewModel::setJugadorParaReasignar,
                    onReasignarCoach = gestionViewModel::reasignarCoach,
                    onSetHorarioParaEliminar = gestionViewModel::setHorarioParaEliminar,
                    onConfirmarEliminarHorario = gestionViewModel::eliminarHorarioConfirmado
                )
            }

            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH
            ) {
                // 1. Splash
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onTimeout = {
                            val user = repository.usuarioActual.value
                            val destination = if (user != null) {
                                if (user.rol == Rol.ADMIN) Routes.adminMain() else Routes.coachMain()
                            } else {
                                Routes.LOGIN
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Login
                composable(Routes.LOGIN) {
                    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                    val loginUiState by authViewModel.loginUiState.collectAsStateWithLifecycle()

                    LoginScreen(
                        uiState = loginUiState,
                        onCorreoChange = authViewModel::onCorreoChange,
                        onContrasenaChange = authViewModel::onContrasenaChange,
                        onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
                        onRecordarSesionChange = authViewModel::onRecordarSesionChange,
                        onLoginClick = {
                            authViewModel.iniciarSesion(
                                onSuccess = { user ->
                                    val destination = if (user.rol == Rol.ADMIN) Routes.adminMain() else Routes.coachMain()
                                    navController.navigate(destination) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        },
                        onNavigateToRegistro = {
                            authViewModel.resetForms()
                            navController.navigate(Routes.REGISTRO)
                        },
                        onQuickLoginCoach = {
                            authViewModel.quickLoginCoach { user ->
                                val destination = if (user.rol == Rol.ADMIN) Routes.adminMain() else Routes.coachMain()
                                navController.navigate(destination) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        },
                        onQuickLoginAdmin = {
                            authViewModel.quickLoginAdmin { user ->
                                val destination = if (user.rol == Rol.ADMIN) Routes.adminMain() else Routes.coachMain()
                                navController.navigate(destination) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                // 3. Registro
                composable(Routes.REGISTRO) {
                    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                    val registroUiState by authViewModel.registroUiState.collectAsStateWithLifecycle()

                    RegistroScreen(
                        uiState = registroUiState,
                        onNombreChange = authViewModel::onNombreRegistroChange,
                        onCorreoChange = authViewModel::onCorreoRegistroChange,
                        onContrasenaChange = authViewModel::onContrasenaRegistroChange,
                        onConfirmarContrasenaChange = authViewModel::onConfirmarContrasenaRegistroChange,
                        onTogglePasswordVisibility = authViewModel::toggleRegistroPasswordVisibility,
                        onToggleConfirmPasswordVisibility = authViewModel::toggleRegistroConfirmPasswordVisibility,
                        onRegistroClick = {
                            authViewModel.registrar(
                                onSuccess = {
                                    navController.navigate(Routes.coachMain()) {
                                        popUpTo(Routes.REGISTRO) { inclusive = true }
                                    }
                                }
                            )
                        },
                        onNavigateToLogin = {
                            authViewModel.resetForms()
                            navController.popBackStack()
                        }
                    )
                }

                // 4. Coach Main Scaffold
                composable(
                    route = Routes.COACH_MAIN,
                    arguments = listOf(
                        navArgument("tab") {
                            type = NavType.StringType
                            defaultValue = CoachTab.INICIO.name
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val tabName = backStackEntry.arguments?.getString("tab")
                    val initialTab = CoachTab.entries.firstOrNull { it.name == tabName } ?: CoachTab.INICIO
                    val coachId = usuarioActual?.id ?: "user_coach_1"
                    val isPrincipal = usuarioActual?.esCoachPrincipal == true
                    var selectedTab by remember(tabName) { mutableStateOf(initialTab) }

                    val tabsDisponibles = remember(isPrincipal) {
                        if (isPrincipal) {
                            listOf(
                                CoachTab.INICIO,
                                CoachTab.COACHES,
                                CoachTab.JUGADORES,
                                CoachTab.AGENDA,
                                CoachTab.AJUSTES
                            )
                        } else {
                            listOf(
                                CoachTab.INICIO,
                                CoachTab.JUGADORES,
                                CoachTab.AGENDA,
                                CoachTab.AJUSTES
                            )
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                tabsDisponibles.forEach { tab ->
                                    NavigationBarItem(
                                        selected = selectedTab == tab,
                                        onClick = { selectedTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Surface(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                CoachTab.INICIO -> {
                                    val homeViewModel: CoachHomeViewModel = viewModel(factory = viewModelFactory)
                                    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                                    CoachHomeScreen(
                                        uiState = homeUiState,
                                        onDismissBanner = homeViewModel::dismissBanner,
                                        onSetMostrarDialogRecordatorios = homeViewModel::setMostrarDialogRecordatorios,
                                        onNavigateToDetalleJugador = { jId ->
                                            navController.navigate(Routes.detalleJugador(jId))
                                        },
                                        onNavigateToRegistrarPago = { jId ->
                                            navController.navigate(Routes.formPago(jId))
                                        },
                                        onNavigateToGestionCoaches = {
                                            navController.navigate(Routes.GESTION_COACHES_PRINCIPAL)
                                        }
                                    )
                                }

                                CoachTab.COACHES -> {
                                    if (isPrincipal) {
                                        gestionCoachesContent {
                                            selectedTab = CoachTab.INICIO
                                        }
                                    }
                                }

                                CoachTab.JUGADORES -> {
                                    val jugadoresViewModel: JugadoresViewModel = viewModel(factory = viewModelFactory)
                                    LaunchedEffect(coachId) {
                                        jugadoresViewModel.init(coachId)
                                    }
                                    val jugadoresUiState by jugadoresViewModel.uiState.collectAsStateWithLifecycle()

                                    JugadoresListScreen(
                                        uiState = jugadoresUiState,
                                        onSearchQueryChange = jugadoresViewModel::onSearchQueryChange,
                                        onCoachFiltroChange = jugadoresViewModel::onCoachFiltroChange,
                                        onSeleccionarJugadorParaReasignar = jugadoresViewModel::onSeleccionarJugadorParaReasignar,
                                        onReasignarCoach = jugadoresViewModel::reasignarCoach,
                                        onNavigateToDetalleJugador = { jId ->
                                            navController.navigate(Routes.detalleJugador(jId))
                                        },
                                        onNavigateToCrearJugador = {
                                            navController.navigate(Routes.formJugador())
                                        }
                                    )
                                }

                                CoachTab.AGENDA -> {
                                    val agendaViewModel: AgendaCoachViewModel = viewModel(factory = viewModelFactory)
                                    LaunchedEffect(coachId) {
                                        agendaViewModel.init(coachId)
                                    }
                                    val agendaUiState by agendaViewModel.uiState.collectAsStateWithLifecycle()

                                    AgendaCoachScreen(
                                        uiState = agendaUiState,
                                        coachId = coachId,
                                        onCoachFiltroChange = agendaViewModel::onCoachFiltroChange,
                                        onSeleccionarHorarioParaEliminar = agendaViewModel::onSeleccionarHorarioParaEliminar,
                                        onConfirmarEliminarHorario = agendaViewModel::eliminarHorarioConfirmado,
                                        onNavigateToCrearHorario = {
                                            navController.navigate(Routes.formHorario(coachId = coachId))
                                        },
                                        onNavigateToEditarHorario = { hId ->
                                            navController.navigate(Routes.formHorario(coachId = coachId, horarioId = hId))
                                        },
                                        onNavigateToVerCanchas = {
                                            navController.navigate(Routes.CANCHAS_COACH)
                                        },
                                        onNavigateToGestionCoaches = {
                                            navController.navigate(Routes.GESTION_COACHES_PRINCIPAL)
                                        }
                                    )
                                }

                                CoachTab.AJUSTES -> {
                                    val configCoachViewModel: ConfiguracionCoachViewModel = viewModel(factory = viewModelFactory)
                                    LaunchedEffect(coachId) {
                                        configCoachViewModel.init(coachId)
                                    }
                                    val configCoachUiState by configCoachViewModel.uiState.collectAsStateWithLifecycle()

                                    ConfiguracionCoachScreen(
                                        uiState = configCoachUiState,
                                        onCambiarTema = configCoachViewModel::cambiarTema,
                                        onCambiarRecordarSesion = configCoachViewModel::cambiarRecordarSesion,
                                        onCambiarDiasAnticipacion = configCoachViewModel::cambiarDiasAnticipacion,
                                        onSetMostrarConfirmarLogout = configCoachViewModel::setMostrarConfirmarLogout,
                                        onCerrarSesion = {
                                            configCoachViewModel.cerrarSesion {
                                                navController.navigate(Routes.LOGIN) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Coach Principal (Jorge): Gestión y asignación de coaches
                composable(Routes.GESTION_COACHES_PRINCIPAL) {
                    gestionCoachesContent { navController.popBackStack() }
                }

                // 6. Admin Main Scaffold
                composable(
                    route = Routes.ADMIN_MAIN,
                    arguments = listOf(
                        navArgument("tab") {
                            type = NavType.StringType
                            defaultValue = AdminTab.INICIO.name
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val tabName = backStackEntry.arguments?.getString("tab")
                    val initialTab = AdminTab.entries.firstOrNull { it.name == tabName } ?: AdminTab.INICIO
                    val adminId = usuarioActual?.id ?: "user_admin_1"
                    var selectedTab by remember(tabName) { mutableStateOf(initialTab) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                AdminTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = selectedTab == tab,
                                        onClick = { selectedTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Surface(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                AdminTab.INICIO -> {
                                    val adminHomeViewModel: AdminHomeViewModel = viewModel(factory = viewModelFactory)
                                    val adminHomeUiState by adminHomeViewModel.uiState.collectAsStateWithLifecycle()

                                    AdminHomeScreen(
                                        uiState = adminHomeUiState,
                                        onNavigateToCoaches = { selectedTab = AdminTab.COACHES },
                                        onNavigateToJugadores = { selectedTab = AdminTab.JUGADORES },
                                        onNavigateToCatalogos = { selectedTab = AdminTab.CATALOGOS }
                                    )
                                }

                                AdminTab.COACHES -> {
                                    val adminCoachesViewModel: AdminCoachesViewModel = viewModel(factory = viewModelFactory)
                                    val adminCoachesUiState by adminCoachesViewModel.uiState.collectAsStateWithLifecycle()

                                    AdminCoachesScreen(
                                        uiState = adminCoachesUiState,
                                        onSearchQueryChange = adminCoachesViewModel::onSearchQueryChange,
                                        onToggleEstadoCoach = adminCoachesViewModel::toggleEstadoCoach,
                                        onNavigateToCrearCoach = {
                                            navController.navigate(Routes.adminFormCoach())
                                        },
                                        onNavigateToEditarCoach = { cId ->
                                            navController.navigate(Routes.adminFormCoach(cId))
                                        }
                                    )
                                }

                                AdminTab.JUGADORES -> {
                                    val adminJugadoresViewModel: AdminJugadoresViewModel = viewModel(factory = viewModelFactory)
                                    val adminJugadoresUiState by adminJugadoresViewModel.uiState.collectAsStateWithLifecycle()

                                    AdminJugadoresScreen(
                                        uiState = adminJugadoresUiState,
                                        onSearchQueryChange = adminJugadoresViewModel::onSearchQueryChange,
                                        onCoachFiltroChange = adminJugadoresViewModel::onCoachFiltroChange,
                                        onSeleccionarJugadorParaReasignar = adminJugadoresViewModel::onSeleccionarJugadorParaReasignar,
                                        onConfirmarReasignarCoach = adminJugadoresViewModel::reasignarCoach,
                                        onNavigateToDetalleJugador = { jId ->
                                            navController.navigate(Routes.detalleJugador(jId))
                                        },
                                        onNavigateToCrearJugador = {
                                            navController.navigate(Routes.formJugador())
                                        }
                                    )
                                }

                                AdminTab.CATALOGOS -> {
                                    val adminCatalogosViewModel: AdminCatalogosViewModel = viewModel(factory = viewModelFactory)
                                    val adminCatalogosUiState by adminCatalogosViewModel.uiState.collectAsStateWithLifecycle()

                                    AdminCatalogosScreen(
                                        uiState = adminCatalogosUiState,
                                        onGuardarCiudad = adminCatalogosViewModel::guardarCiudad,
                                        onEliminarCiudad = adminCatalogosViewModel::eliminarCiudad,
                                        onGuardarCancha = adminCatalogosViewModel::guardarCancha,
                                        onEliminarCancha = adminCatalogosViewModel::eliminarCancha,
                                        onLimpiarError = adminCatalogosViewModel::limpiarError
                                    )
                                }

                                AdminTab.AJUSTES -> {
                                    val adminConfigViewModel: AdminConfiguracionViewModel = viewModel(factory = viewModelFactory)
                                    LaunchedEffect(adminId) {
                                        adminConfigViewModel.init(adminId)
                                    }
                                    val adminConfigUiState by adminConfigViewModel.uiState.collectAsStateWithLifecycle()

                                    AdminConfiguracionScreen(
                                        uiState = adminConfigUiState,
                                        onCambiarTema = adminConfigViewModel::cambiarTema,
                                        onSetMostrarConfirmarLogout = adminConfigViewModel::setMostrarConfirmarLogout,
                                        onLogoutConfirmado = {
                                            adminConfigViewModel.cerrarSesion {
                                                navController.navigate(Routes.LOGIN) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Detalle de Jugador
                composable(
                    route = Routes.DETALLE_JUGADOR,
                    arguments = listOf(
                        navArgument("jugadorId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val jugadorId = backStackEntry.arguments?.getString("jugadorId") ?: ""
                    val detalleViewModel: DetalleJugadorViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(jugadorId) {
                        detalleViewModel.init(jugadorId)
                    }
                    val detalleUiState by detalleViewModel.uiState.collectAsStateWithLifecycle()

                    DetalleJugadorScreen(
                        uiState = detalleUiState,
                        onBack = { navController.popBackStack() },
                        onNavigateToEditarJugador = { jId ->
                            navController.navigate(Routes.formJugador(jId))
                        },
                        onNavigateToRegistrarPago = { jId ->
                            navController.navigate(Routes.formPago(jId))
                        },
                        onNavigateToEditarPago = { jId, pId ->
                            navController.navigate(Routes.formPago(jId, pId))
                        },
                        onSetMostrarConfirmarEliminar = detalleViewModel::setMostrarConfirmarEliminar,
                        onConfirmarEliminarJugador = {
                            detalleViewModel.eliminarJugadorConfirmado {
                                navController.popBackStack()
                            }
                        },
                        onSetPagoAEliminar = detalleViewModel::setPagoAEliminar,
                        onConfirmarEliminarPago = detalleViewModel::eliminarPagoConfirmado
                    )
                }

                // 8. Formulario de Jugador (Crear / Editar)
                composable(
                    route = Routes.FORM_JUGADOR,
                    arguments = listOf(
                        navArgument("jugadorId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val jugadorId = backStackEntry.arguments?.getString("jugadorId")
                    val coachId = usuarioActual?.id ?: "user_coach_1"
                    val formJugadorViewModel: FormJugadorViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(jugadorId, coachId) {
                        formJugadorViewModel.init(coachId, jugadorId)
                    }
                    val formJugadorUiState by formJugadorViewModel.uiState.collectAsStateWithLifecycle()

                    FormJugadorScreen(
                        uiState = formJugadorUiState,
                        isCoachPrincipal = usuarioActual?.esCoachPrincipal == true,
                        isAdmin = usuarioActual?.rol == Rol.ADMIN,
                        onNombreChange = formJugadorViewModel::onNombreChange,
                        onTelefonoChange = formJugadorViewModel::onTelefonoChange,
                        onCorreoChange = formJugadorViewModel::onCorreoChange,
                        onFechaInicioChange = formJugadorViewModel::onFechaInicioChange,
                        onValorMensualChange = formJugadorViewModel::onValorMensualChange,
                        onCoachIdChange = formJugadorViewModel::onCoachIdChange,
                        onToggleHorario = formJugadorViewModel::toggleHorario,
                        onGuardarClick = {
                            formJugadorViewModel.guardar {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 9. Formulario de Pago (Registrar / Editar)
                composable(
                    route = Routes.FORM_PAGO,
                    arguments = listOf(
                        navArgument("jugadorId") { type = NavType.StringType },
                        navArgument("pagoId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val jugadorId = backStackEntry.arguments?.getString("jugadorId") ?: ""
                    val pagoId = backStackEntry.arguments?.getString("pagoId")
                    val formPagoViewModel: FormPagoViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(jugadorId, pagoId) {
                        formPagoViewModel.init(jugadorId, pagoId)
                    }
                    val formPagoUiState by formPagoViewModel.uiState.collectAsStateWithLifecycle()

                    FormPagoScreen(
                        uiState = formPagoUiState,
                        onFechaChange = formPagoViewModel::onFechaChange,
                        onMontoChange = formPagoViewModel::onMontoChange,
                        onObservacionChange = formPagoViewModel::onObservacionChange,
                        onGuardarClick = {
                            formPagoViewModel.guardar {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 10. Formulario de Horario (Crear / Editar)
                composable(
                    route = Routes.FORM_HORARIO,
                    arguments = listOf(
                        navArgument("coachId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("horarioId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val paramCoachId = backStackEntry.arguments?.getString("coachId")
                    val coachId = paramCoachId ?: (usuarioActual?.id ?: "user_coach_1")
                    val horarioId = backStackEntry.arguments?.getString("horarioId")
                    val formHorarioViewModel: FormHorarioViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(coachId, horarioId) {
                        formHorarioViewModel.init(coachId, horarioId)
                    }
                    val formHorarioUiState by formHorarioViewModel.uiState.collectAsStateWithLifecycle()

                    FormHorarioScreen(
                        uiState = formHorarioUiState,
                        isCoachPrincipal = usuarioActual?.esCoachPrincipal == true,
                        coachId = formHorarioUiState.coachId,
                        onCoachIdChange = formHorarioViewModel::onCoachIdChange,
                        onCanchaIdChange = formHorarioViewModel::onCanchaIdChange,
                        onDiaSemanaChange = formHorarioViewModel::onDiaSemanaChange,
                        onHoraInicioChange = formHorarioViewModel::onHoraInicioChange,
                        onHoraFinChange = formHorarioViewModel::onHoraFinChange,
                        onGuardarClick = {
                            formHorarioViewModel.guardar {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 11. Visualización de Canchas para Coach
                composable(Routes.CANCHAS_COACH) {
                    val canchasViewModel: CanchasCoachViewModel = viewModel(factory = viewModelFactory)
                    val canchasUiState by canchasViewModel.uiState.collectAsStateWithLifecycle()

                    CanchasCoachScreen(
                        uiState = canchasUiState,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 12. Formulario de Coach en Admin (Crear / Editar)
                composable(
                    route = Routes.ADMIN_FORM_COACH,
                    arguments = listOf(
                        navArgument("coachId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val coachId = backStackEntry.arguments?.getString("coachId")
                    val formCoachViewModel: AdminFormCoachViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(coachId) {
                        formCoachViewModel.init(coachId)
                    }
                    val formCoachUiState by formCoachViewModel.uiState.collectAsStateWithLifecycle()

                    AdminFormCoachScreen(
                        uiState = formCoachUiState,
                        onNombreChange = formCoachViewModel::onNombreChange,
                        onCorreoChange = formCoachViewModel::onCorreoChange,
                        onContrasenaChange = formCoachViewModel::onContrasenaChange,
                        onEstadoChange = formCoachViewModel::onEstadoChange,
                        onGuardarClick = {
                            formCoachViewModel.guardar {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
