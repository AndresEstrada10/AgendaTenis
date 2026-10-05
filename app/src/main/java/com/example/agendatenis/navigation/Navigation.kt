package com.example.agendatenis.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Screen {
    data object Splash : Screen
    data object Login : Screen
    data object Registro : Screen

    // Coach container & sub-destinations
    data class CoachMain(val tab: CoachTab = CoachTab.INICIO) : Screen
    data class DetalleJugador(val jugadorId: String, val backScreen: Screen) : Screen
    data class FormJugador(val jugadorId: String? = null, val backScreen: Screen) : Screen
    data class FormPago(val jugadorId: String, val pagoId: String? = null, val backScreen: Screen) : Screen
    data class FormPlan(val planId: String? = null, val coachId: String, val backScreen: Screen) : Screen
    data class FormHorario(val horarioId: String? = null, val backScreen: Screen) : Screen
    data class CanchasCoachView(val backScreen: Screen) : Screen

    // Admin container & sub-destinations
    data class AdminMain(val tab: AdminTab = AdminTab.INICIO) : Screen
    data class AdminFormCoach(val coachId: String? = null, val backScreen: Screen) : Screen
    data class AdminFormCiudad(val ciudadId: String? = null, val backScreen: Screen) : Screen
    data class AdminFormCancha(val canchaId: String? = null, val ciudadId: String? = null, val backScreen: Screen) : Screen
}

enum class CoachTab(val label: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Default.Home),
    JUGADORES("Jugadores", Icons.Default.Person),
    AGENDA("Agenda", Icons.Default.DateRange),
    PLANES("Planes", Icons.Default.List),
    AJUSTES("Ajustes", Icons.Default.Settings)
}

enum class AdminTab(val label: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Default.Home),
    COACHES("Coaches", Icons.Default.AccountBox),
    JUGADORES("Jugadores", Icons.Default.Person),
    CATALOGOS("Catálogos", Icons.Default.Place),
    AJUSTES("Ajustes", Icons.Default.Settings)
}
