package com.example.agendatenis.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    repository: AgendaTenisRepository = AgendaTenisRepository.getInstance()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val usuarioActual by repository.usuarioActual.collectAsState()
    val configs by repository.configs.collectAsState()

    // Manejo de tema según configuración del usuario (o sistema por defecto)
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
            when (val screen = currentScreen) {
                is Screen.Splash -> {
                    SplashScreen(
                        onTimeout = {
                            val user = repository.usuarioActual.value
                            currentScreen = if (user != null) {
                                if (user.rol == Rol.ADMIN) Screen.AdminMain() else Screen.CoachMain()
                            } else {
                                Screen.Login
                            }
                        }
                    )
                }

                is Screen.Login -> {
                    LoginScreen(
                        repository = repository,
                        onLoginSuccess = { user ->
                            currentScreen = if (user.rol == Rol.ADMIN) Screen.AdminMain() else Screen.CoachMain()
                        },
                        onNavigateToRegistro = {
                            currentScreen = Screen.Registro
                        }
                    )
                }

                is Screen.Registro -> {
                    RegistroScreen(
                        repository = repository,
                        onRegistroSuccess = { coach ->
                            currentScreen = Screen.CoachMain()
                        },
                        onNavigateToLogin = {
                            currentScreen = Screen.Login
                        }
                    )
                }

                // Coach Main Scaffold (Bottom Nav: Inicio, Jugadores, Agenda, Planes, Ajustes)
                is Screen.CoachMain -> {
                    val coachId = usuarioActual?.id ?: "user_coach_1"
                    var selectedTab by remember { mutableStateOf(screen.tab) }

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
                                            currentScreen = Screen.DetalleJugador(jId, backScreen = Screen.CoachMain(CoachTab.INICIO))
                                        },
                                        onNavigateToRegistrarPago = { jId ->
                                            currentScreen = Screen.FormPago(jId, null, backScreen = Screen.CoachMain(CoachTab.INICIO))
                                        }
                                    )
                                }

                                CoachTab.JUGADORES -> {
                                    JugadoresListScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onNavigateToDetalleJugador = { jId ->
                                            currentScreen = Screen.DetalleJugador(jId, backScreen = Screen.CoachMain(CoachTab.JUGADORES))
                                        },
                                        onNavigateToCrearJugador = {
                                            currentScreen = Screen.FormJugador(null, backScreen = Screen.CoachMain(CoachTab.JUGADORES))
                                        }
                                    )
                                }

                                CoachTab.AGENDA -> {
                                    AgendaCoachScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onNavigateToCrearHorario = {
                                            currentScreen = Screen.FormHorario(null, backScreen = Screen.CoachMain(CoachTab.AGENDA))
                                        },
                                        onNavigateToEditarHorario = { hId ->
                                            currentScreen = Screen.FormHorario(hId, backScreen = Screen.CoachMain(CoachTab.AGENDA))
                                        },
                                        onNavigateToVerCanchas = {
                                            currentScreen = Screen.CanchasCoachView(backScreen = Screen.CoachMain(CoachTab.AGENDA))
                                        }
                                    )
                                }

                                CoachTab.PLANES -> {
                                    PlanesListScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onNavigateToCrearPlan = {
                                            currentScreen = Screen.FormPlan(null, coachId, backScreen = Screen.CoachMain(CoachTab.PLANES))
                                        },
                                        onNavigateToEditarPlan = { pId ->
                                            currentScreen = Screen.FormPlan(pId, coachId, backScreen = Screen.CoachMain(CoachTab.PLANES))
                                        }
                                    )
                                }

                                CoachTab.AJUSTES -> {
                                    ConfiguracionCoachScreen(
                                        repository = repository,
                                        coachId = coachId,
                                        onLogout = {
                                            currentScreen = Screen.Login
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Admin Main Scaffold (Bottom Nav: Inicio, Coaches, Jugadores, Catalogos, Ajustes)
                is Screen.AdminMain -> {
                    val adminId = usuarioActual?.id ?: "user_admin_1"
                    var selectedTab by remember { mutableStateOf(screen.tab) }

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
                                            currentScreen = Screen.AdminFormCoach(null, backScreen = Screen.AdminMain(AdminTab.COACHES))
                                        },
                                        onNavigateToEditarCoach = { cId ->
                                            currentScreen = Screen.AdminFormCoach(cId, backScreen = Screen.AdminMain(AdminTab.COACHES))
                                        }
                                    )
                                }

                                AdminTab.JUGADORES -> {
                                    AdminJugadoresScreen(
                                        repository = repository,
                                        onNavigateToDetalleJugador = { jId ->
                                            currentScreen = Screen.DetalleJugador(jId, backScreen = Screen.AdminMain(AdminTab.JUGADORES))
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
                                            currentScreen = Screen.Login
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-pantallas Coach / Compartidas
                is Screen.DetalleJugador -> {
                    DetalleJugadorScreen(
                        repository = repository,
                        jugadorId = screen.jugadorId,
                        onBack = { currentScreen = screen.backScreen },
                        onNavigateToEditarJugador = { jId ->
                            currentScreen = Screen.FormJugador(jId, backScreen = screen)
                        },
                        onNavigateToRegistrarPago = { jId ->
                            currentScreen = Screen.FormPago(jId, null, backScreen = screen)
                        },
                        onNavigateToEditarPago = { jId, pId ->
                            currentScreen = Screen.FormPago(jId, pId, backScreen = screen)
                        }
                    )
                }

                is Screen.FormJugador -> {
                    val coachId = usuarioActual?.id ?: "user_coach_1"
                    FormJugadorScreen(
                        repository = repository,
                        coachId = coachId,
                        jugadorId = screen.jugadorId,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.FormPago -> {
                    FormPagoScreen(
                        repository = repository,
                        jugadorId = screen.jugadorId,
                        pagoId = screen.pagoId,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.FormPlan -> {
                    FormPlanScreen(
                        repository = repository,
                        coachId = screen.coachId,
                        planId = screen.planId,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.FormHorario -> {
                    val coachId = usuarioActual?.id ?: "user_coach_1"
                    FormHorarioScreen(
                        repository = repository,
                        coachId = coachId,
                        horarioId = screen.horarioId,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.CanchasCoachView -> {
                    CanchasCoachScreen(
                        repository = repository,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.AdminFormCoach -> {
                    AdminFormCoachScreen(
                        repository = repository,
                        coachId = screen.coachId,
                        onBack = { currentScreen = screen.backScreen }
                    )
                }

                is Screen.AdminFormCiudad -> {
                    // Handled within AdminCatalogosScreen dialogs
                    currentScreen = screen.backScreen
                }

                is Screen.AdminFormCancha -> {
                    // Handled within AdminCatalogosScreen dialogs
                    currentScreen = screen.backScreen
                }
            }
        }
    }
}
