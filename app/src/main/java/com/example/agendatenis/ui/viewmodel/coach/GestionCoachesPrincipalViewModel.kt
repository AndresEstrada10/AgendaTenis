package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GestionCoachesUiState(
    val coachesGestion: List<ResumenCoachGestion> = emptyList(),
    val canchasMap: Map<String, Cancha> = emptyMap(),
    val ciudadesMap: Map<String, Ciudad> = emptyMap(),
    val coaches: List<Usuario> = emptyList(),
    val jugadorParaReasignar: Jugador? = null,
    val horarioParaEliminar: Horario? = null,
    val usuarioActual: Usuario? = null,
    val isLoading: Boolean = false
)

class GestionCoachesPrincipalViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _jugadorParaReasignar = MutableStateFlow<Jugador?>(null)
    private val _horarioParaEliminar = MutableStateFlow<Horario?>(null)

    val uiState: StateFlow<GestionCoachesUiState> = combine(
        repository.usuarioActual,
        repository.usuarios,
        repository.canchas,
        repository.ciudades,
        combine(_jugadorParaReasignar, _horarioParaEliminar) { j, h -> Pair(j, h) }
    ) { user, usuarios, canchas, ciudades, dialogs ->
        val coachesList = usuarios.filter { it.rol == Rol.COACH }
        val canchasMap = canchas.associateBy { it.id }
        val ciudadesMap = ciudades.associateBy { it.id }
        val resumenes = repository.obtenerResumenCoachesGestion()

        GestionCoachesUiState(
            coachesGestion = resumenes,
            canchasMap = canchasMap,
            ciudadesMap = ciudadesMap,
            coaches = coachesList,
            jugadorParaReasignar = dialogs.first,
            horarioParaEliminar = dialogs.second,
            usuarioActual = user,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GestionCoachesUiState(isLoading = true)
    )

    fun setJugadorParaReasignar(jugador: Jugador?) {
        _jugadorParaReasignar.value = jugador
    }

    fun setHorarioParaEliminar(horario: Horario?) {
        _horarioParaEliminar.value = horario
    }

    fun reasignarCoach(jugadorId: String, nuevoCoachId: String) {
        viewModelScope.launch {
            repository.reasignarCoachAJugador(jugadorId, nuevoCoachId)
            _jugadorParaReasignar.value = null
        }
    }

    fun eliminarHorarioConfirmado() {
        val horario = _horarioParaEliminar.value ?: return
        viewModelScope.launch {
            repository.eliminarHorario(horario.id)
            _horarioParaEliminar.value = null
        }
    }
}
