package com.example.agendatenis.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rutas y definiciones de navegación para NavController (Jetpack Compose Navigation)
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    // Coach Container y Tabs
    const val COACH_MAIN = "coach_main?tab={tab}"
    fun coachMain(tab: CoachTab = CoachTab.INICIO) = "coach_main?tab=${tab.name}"

    // Coach sub-pantallas
    const val DETALLE_JUGADOR = "detalle_jugador/{jugadorId}"
    fun detalleJugador(jugadorId: String) = "detalle_jugador/$jugadorId"

    const val FORM_JUGADOR = "form_jugador?jugadorId={jugadorId}"
    fun formJugador(jugadorId: String? = null) =
        if (jugadorId != null) "form_jugador?jugadorId=$jugadorId" else "form_jugador"

    const val FORM_PAGO = "form_pago/{jugadorId}?pagoId={pagoId}"
    fun formPago(jugadorId: String, pagoId: String? = null) =
        if (pagoId != null) "form_pago/$jugadorId?pagoId=$pagoId" else "form_pago/$jugadorId"

    const val FORM_PLAN = "form_plan/{coachId}?planId={planId}"
    fun formPlan(coachId: String, planId: String? = null) =
        if (planId != null) "form_plan/$coachId?planId=$planId" else "form_plan/$coachId"

    const val FORM_HORARIO = "form_horario?coachId={coachId}&horarioId={horarioId}"
    fun formHorario(coachId: String? = null, horarioId: String? = null): String {
        val params = mutableListOf<String>()
        if (!coachId.isNullOrBlank()) params.add("coachId=$coachId")
        if (!horarioId.isNullOrBlank()) params.add("horarioId=$horarioId")
        return if (params.isEmpty()) "form_horario" else "form_horario?" + params.joinToString("&")
    }

    const val CANCHAS_COACH = "canchas_coach"
    const val GESTION_COACHES_PRINCIPAL = "gestion_coaches_principal"

    // Admin Container y Tabs
    const val ADMIN_MAIN = "admin_main?tab={tab}"
    fun adminMain(tab: AdminTab = AdminTab.INICIO) = "admin_main?tab=${tab.name}"

    const val ADMIN_FORM_COACH = "admin_form_coach?coachId={coachId}"
    fun adminFormCoach(coachId: String? = null) =
        if (coachId != null) "admin_form_coach?coachId=$coachId" else "admin_form_coach"
}

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
    data class FormHorario(val horarioId: String? = null, val coachId: String? = null, val backScreen: Screen) : Screen
    data class CanchasCoachView(val backScreen: Screen) : Screen

    // Coach Principal (Jorge): Visualización y administración de coaches, horarios y jugadores designados
    data class GestionCoachesPrincipal(val backScreen: Screen) : Screen

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
