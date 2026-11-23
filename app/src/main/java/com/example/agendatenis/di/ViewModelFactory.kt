package com.example.agendatenis.di

import com.example.agendatenis.data.repository.ThemeRepository
import com.example.agendatenis.ui.viewmodel.ThemeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.ui.viewmodel.admin.*
import com.example.agendatenis.ui.viewmodel.auth.AuthViewModel
import com.example.agendatenis.ui.viewmodel.coach.*

/**
 * Fábrica de ViewModels para la inyección manual de dependencias en AgendaTenis.
 * Garantiza que ningún ViewModel instancie directamente repositorios ni fuentes de datos.
 */
class ViewModelFactory(
    private val repository: IAgendaTenisRepository,
    private val themeRepository: ThemeRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ThemeViewModel::class.java) ->
                ThemeViewModel(requireNotNull(themeRepository)) as T

            // Auth
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository) as T

            // Coach
            modelClass.isAssignableFrom(CoachHomeViewModel::class.java) ->
                CoachHomeViewModel(repository) as T

            modelClass.isAssignableFrom(JugadoresViewModel::class.java) ->
                JugadoresViewModel(repository) as T

            modelClass.isAssignableFrom(AgendaCoachViewModel::class.java) ->
                AgendaCoachViewModel(repository) as T

            modelClass.isAssignableFrom(ConfiguracionCoachViewModel::class.java) ->
                ConfiguracionCoachViewModel(repository) as T

            modelClass.isAssignableFrom(GestionCoachesPrincipalViewModel::class.java) ->
                GestionCoachesPrincipalViewModel(repository) as T

            modelClass.isAssignableFrom(DetalleJugadorViewModel::class.java) ->
                DetalleJugadorViewModel(repository) as T

            modelClass.isAssignableFrom(FormJugadorViewModel::class.java) ->
                FormJugadorViewModel(repository) as T

            modelClass.isAssignableFrom(FormPagoViewModel::class.java) ->
                FormPagoViewModel(repository) as T

            modelClass.isAssignableFrom(FormHorarioViewModel::class.java) ->
                FormHorarioViewModel(repository) as T

            modelClass.isAssignableFrom(CanchasCoachViewModel::class.java) ->
                CanchasCoachViewModel(repository) as T

            // Admin
            modelClass.isAssignableFrom(AdminHomeViewModel::class.java) ->
                AdminHomeViewModel(repository) as T

            modelClass.isAssignableFrom(AdminCoachesViewModel::class.java) ->
                AdminCoachesViewModel(repository) as T

            modelClass.isAssignableFrom(AdminFormCoachViewModel::class.java) ->
                AdminFormCoachViewModel(repository) as T

            modelClass.isAssignableFrom(AdminJugadoresViewModel::class.java) ->
                AdminJugadoresViewModel(repository) as T

            modelClass.isAssignableFrom(AdminCatalogosViewModel::class.java) ->
                AdminCatalogosViewModel(repository) as T

            modelClass.isAssignableFrom(AdminConfiguracionViewModel::class.java) ->
                AdminConfiguracionViewModel(repository) as T

            else -> throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
