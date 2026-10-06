package com.example.agendatenis.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.TemaApp
import com.example.agendatenis.navigation.*
import com.example.agendatenis.repository.AgendaTenisRepository
import com.example.agendatenis.ui.screens.admin.*
import com.example.agendatenis.ui.screens.auth.LoginScreen
import com.example.agendatenis.ui.screens.auth.RegistroScreen
import com.example.agendatenis.ui.screens.auth.SplashScreen
import com.example.agendatenis.ui.screens.coach.*
import com.example.agendatenis.ui.theme.AgendaTenisTheme

@Composable
fun AppRoot(
    repository: AgendaTenisRepository = AgendaTenisRepository.getInstance(),
    navController: NavHostController = rememberNavController()
) {
    val usuarioActual by repository.usuarioActual.collectAsState()
    val configs by repository.configs.collectAsState()

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
                    LoginScreen(
                        repository = repository,
                        onLoginSuccess = { user ->
                            val destination = if (user.rol == Rol.ADMIN) Routes.adminMain() else Routes.coachMain()
                            navController.navigate(destination) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToRegistro = {
                            navController.navigate(Routes.REGISTRO)
                        }
                    )
                }

                // 3. Registro
                composable(Routes.REGISTRO) {
                    RegistroScreen(
                        repository = repository,
                        onRegistroSuccess = {
                            navController.navigate(Routes.coachMain()) {
                                popUpTo(Routes.REGISTRO) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // 4. Coach Main Scaffold (Tabs: Inicio, Jugadores, Agenda, Planes, Ajustes)
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
                    var selectedTab by remember(tabName) { mutableStateOf(initialTab) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                CoachTab.entries.forEach { tab ->
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
                                    CoachHomeScreen(
                                        repository = repository,
                                        coachId = coachId,
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

                                CoachTab.JUGADORES -> {
                                    JugadoresListScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onNavigateToDetalleJugador = { jId ->
                                            navController.navigate(Routes.detalleJugador(jId))
                                        },
                                        onNavigateToCrearJugador = {
                                            navController.navigate(Routes.formJugador())
                                        }
                                    )
                                }

                                CoachTab.AGENDA -> {
                                    AgendaCoachScreen(
                                        repository = repository,
                                        coachId = coachId,
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

                                CoachTab.PLANES -> {
                                    PlanesListScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onNavigateToCrearPlan = {
                                            navController.navigate(Routes.formPlan(coachId = coachId))
                                        },
                                        onNavigateToEditarPlan = { pId ->
                                            navController.navigate(Routes.formPlan(coachId = coachId, planId = pId))
                                        }
                                    )
                                }

                                CoachTab.AJUSTES -> {
                                    ConfiguracionCoachScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onLogout = {
                                            navController.navigate(Routes.LOGIN) {
                                                popUpTo(0) { inclusive = true }
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
                    GestionCoachesPrincipalScreen(
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onNavigateToCrearHorarioCoach = { cId ->
                            navController.navigate(Routes.formHorario(coachId = cId))
                        },
                        onNavigateToEditarHorarioCoach = { cId, hId ->
                            navController.navigate(Routes.formHorario(coachId = cId, horarioId = hId))
                        },
                        onNavigateToDetalleJugador = { jId ->
                            navController.navigate(Routes.detalleJugador(jId))
                        }
                    )
                }

                // 6. Admin Main Scaffold (Tabs: Inicio, Coaches, Jugadores, Catalogos, Ajustes)
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
                                    AdminHomeScreen(
                                        repository = repository,
                                        onNavigateToCoaches = { selectedTab = AdminTab.COACHES },
                                        onNavigateToJugadores = { selectedTab = AdminTab.JUGADORES },
                                        onNavigateToCatalogos = { selectedTab = AdminTab.CATALOGOS }
                                    )
                                }

                                AdminTab.COACHES -> {
                                    AdminCoachesScreen(
                                        repository = repository,
                                        onNavigateToCrearCoach = {
                                            navController.navigate(Routes.adminFormCoach())
                                        },
                                        onNavigateToEditarCoach = { cId ->
                                            navController.navigate(Routes.adminFormCoach(cId))
                                        }
                                    )
                                }

                                AdminTab.JUGADORES -> {
                                    AdminJugadoresScreen(
                                        repository = repository,
                                        onNavigateToDetalleJugador = { jId ->
                                            navController.navigate(Routes.detalleJugador(jId))
                                        }
                                    )
                                }

                                AdminTab.CATALOGOS -> {
                                    AdminCatalogosScreen(
                                        repository = repository
                                    )
                                }

                                AdminTab.AJUSTES -> {
                                    AdminConfiguracionScreen(
                                        repository = repository,
                                        adminId = adminId,
                                        onLogout = {
                                            navController.navigate(Routes.LOGIN) {
                                                popUpTo(0) { inclusive = true }
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
                    DetalleJugadorScreen(
                        repository = repository,
                        jugadorId = jugadorId,
                        onBack = { navController.popBackStack() },
                        onNavigateToEditarJugador = { jId ->
                            navController.navigate(Routes.formJugador(jId))
                        },
                        onNavigateToRegistrarPago = { jId ->
                            navController.navigate(Routes.formPago(jId))
                        },
                        onNavigateToEditarPago = { jId, pId ->
                            navController.navigate(Routes.formPago(jId, pId))
                        }
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
                    FormJugadorScreen(
                        repository = repository,
                        coachId = coachId,
                        jugadorId = jugadorId,
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
                    FormPagoScreen(
                        repository = repository,
                        jugadorId = jugadorId,
                        pagoId = pagoId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 10. Formulario de Plan (Crear / Editar)
                composable(
                    route = Routes.FORM_PLAN,
                    arguments = listOf(
                        navArgument("coachId") { type = NavType.StringType },
                        navArgument("planId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val coachId = backStackEntry.arguments?.getString("coachId") ?: (usuarioActual?.id ?: "user_coach_1")
                    val planId = backStackEntry.arguments?.getString("planId")
                    FormPlanScreen(
                        repository = repository,
                        coachId = coachId,
                        planId = planId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 11. Formulario de Horario (Crear / Editar)
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
                    FormHorarioScreen(
                        repository = repository,
                        coachId = coachId,
                        horarioId = horarioId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 12. Visualización de Canchas para Coach
                composable(Routes.CANCHAS_COACH) {
                    CanchasCoachScreen(
                        repository = repository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 13. Formulario de Coach en Admin (Crear / Editar)
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
                    AdminFormCoachScreen(
                        repository = repository,
                        coachId = coachId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
