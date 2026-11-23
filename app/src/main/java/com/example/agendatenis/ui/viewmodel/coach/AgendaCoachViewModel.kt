package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AgendaCoachUiState(
    val coachSeleccionadoId: String = "",
    val diaFiltro: DiaSemana? = null,
    val horarioParaEliminar: Horario? = null,
    val coaches: List<Usuario> = emptyList(),
    val canchasMap: Map<String, Cancha> = emptyMap(),
    val ciudadesMap: Map<String, Ciudad> = emptyMap(),
    val horariosAgrupados: Map<DiaSemana, List<Horario>> = emptyMap(),
    val jugadoresPorHorario: Map<String, List<Jugador>> = emptyMap(),
    val isCoachPrincipal: Boolean = false,
    val usuarioActual: Usuario? = null
)

class AgendaCoachViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _coachSeleccionadoId = MutableStateFlow("")
    private val _diaFiltro = MutableStateFlow<DiaSemana?>(null)
    private val _horarioParaEliminar = MutableStateFlow<Horario?>(null)
    private var initialized = false

    fun init(coachId: String) {
        if (!initialized) {
            _coachSeleccionadoId.value = coachId
            initialized = true
        }
    }

    private data class FilterState(
        val coachId: String,
        val dia: DiaSemana?,
        val horarioEliminar: Horario?
    )

    private val filterState = combine(_coachSeleccionadoId, _diaFiltro, _horarioParaEliminar) { cId, dia, hEliminar ->
        FilterState(cId, dia, hEliminar)
    }

    val uiState: StateFlow<AgendaCoachUiState> = combine(
        repository.usuarioActual,
        repository.horarios,
        repository.canchas,
        repository.ciudades,
        filterState
    ) { user, horarios, canchas, ciudades, filters ->
        val isPrincipal = user?.esCoachPrincipal == true
        val targetCoachId = filters.coachId.ifBlank { user?.id ?: "user_coach_1" }
        val coachesList = repository.usuarios.value.filter { it.rol == Rol.COACH }
        val canchasMap = canchas.associateBy { it.id }
        val ciudadesMap = ciudades.associateBy { it.id }
        val jugadoresMap = repository.jugadores.value.associateBy { it.id }

        val jugadoresPorHorario = repository.asignaciones.value.groupBy(
            keySelector = { it.horarioId },
            valueTransform = { asig -> jugadoresMap[asig.jugadorId] }
        ).mapValues { entry -> entry.value.filterNotNull().filter { it.estado == EstadoJugador.ACTIVO } }

        val horariosFiltrados = horarios
            .filter { it.coachId == targetCoachId }
            .let { list ->
                val dia = filters.dia
                if (dia != null) list.filter { it.diaSemana == dia } else list
            }
            .sortedWith(compareBy({ it.diaSemana.diaIso }, { it.horaInicio }))

        val agrupados = horariosFiltrados.groupBy { it.diaSemana }

        AgendaCoachUiState(
            coachSeleccionadoId = targetCoachId,
            diaFiltro = filters.dia,
            horarioParaEliminar = filters.horarioEliminar,
            coaches = coachesList,
            canchasMap = canchasMap,
            ciudadesMap = ciudadesMap,
            horariosAgrupados = agrupados,
            jugadoresPorHorario = jugadoresPorHorario,
            isCoachPrincipal = isPrincipal,
            usuarioActual = user
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AgendaCoachUiState()
    )

    fun onCoachFiltroChange(coachId: String) {
        _coachSeleccionadoId.value = coachId
    }

    fun onDiaFiltroChange(dia: DiaSemana?) {
        _diaFiltro.value = dia
    }

    fun onSeleccionarHorarioParaEliminar(horario: Horario?) {
        _horarioParaEliminar.value = horario
    }

    fun setHorarioParaEliminar(horario: Horario?) {
        _horarioParaEliminar.value = horario
    }

    fun eliminarHorarioConfirmado() {
        val horario = _horarioParaEliminar.value ?: return
        viewModelScope.launch {
            repository.eliminarHorario(horario.id)
            _horarioParaEliminar.value = null
        }
    }
}
