package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JugadorItemUi(
    val jugador: Jugador,
    val coachNombre: String?,
    val estadoPago: EstadoPago?,
    val diasActivos: Set<DiaSemana>,
    val horasTexto: String
)

data class JugadoresUiState(
    val searchQuery: String = "",
    val coachFiltroId: String? = null,
    val jugadorParaReasignar: Jugador? = null,
    val coaches: List<Usuario> = emptyList(),
    val jugadoresItems: List<JugadorItemUi> = emptyList(),
    val isCoachPrincipal: Boolean = false,
    val coachId: String = ""
)

class JugadoresViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _coachFiltroId = MutableStateFlow<String?>(null)
    private val _jugadorParaReasignar = MutableStateFlow<Jugador?>(null)
    private var initialized = false
    private var defaultCoachId = "user_coach_1"

    fun init(coachId: String) {
        if (!initialized) {
            defaultCoachId = coachId
            val user = repository.usuarioActual.value
            val isPrincipal = user?.esCoachPrincipal == true
            _coachFiltroId.value = if (isPrincipal) null else coachId
            initialized = true
        }
    }

    private data class FilterState(
        val search: String,
        val coachFiltro: String?,
        val reasignar: Jugador?
    )

    private val filterState = combine(_searchQuery, _coachFiltroId, _jugadorParaReasignar) { search, coachFiltro, reasignar ->
        FilterState(search, coachFiltro, reasignar)
    }

    val uiState: StateFlow<JugadoresUiState> = combine(
        repository.usuarioActual,
        repository.jugadores,
        repository.usuarios,
        repository.pagos,
        filterState
    ) { user, jugadores, usuarios, _, filters ->
        val isPrincipal = user?.esCoachPrincipal == true
        val currentCoachId = user?.id ?: defaultCoachId
        val coachesList = usuarios.filter { it.rol == Rol.COACH }
        val coachesMap = coachesList.associateBy { it.id }
        val horarios = repository.horarios.value
        val asignaciones = repository.asignaciones.value

        val filtrados = jugadores
            .filter {
                if (isPrincipal) {
                    if (filters.coachFiltro == null) true else it.coachId == filters.coachFiltro
                } else {
                    it.coachId == currentCoachId
                }
            }
            .filter {
                if (filters.search.isBlank()) true
                else it.nombre.contains(filters.search, ignoreCase = true) ||
                        it.telefono.contains(filters.search) ||
                        it.correo.contains(filters.search, ignoreCase = true)
            }
            .sortedBy { it.nombre }

        val items = filtrados.map { jugador ->
            val coach = coachesMap[jugador.coachId]
            val jugadorAsignaciones = asignaciones.filter { it.jugadorId == jugador.id }
            val jugadorHorarios = horarios.filter { h -> jugadorAsignaciones.any { it.horarioId == h.id } }
            val diasActivos = jugadorHorarios.map { it.diaSemana }.toSet()
            val horasTexto = jugadorHorarios.joinToString(", ") { "${it.horaInicio}-${it.horaFin}" }
            val calc = repository.calcularEstadoPago(jugador.id)

            JugadorItemUi(
                jugador = jugador,
                coachNombre = coach?.nombre,
                estadoPago = calc?.estado,
                diasActivos = diasActivos,
                horasTexto = horasTexto
            )
        }

        JugadoresUiState(
            searchQuery = filters.search,
            coachFiltroId = filters.coachFiltro,
            jugadorParaReasignar = filters.reasignar,
            coaches = coachesList,
            jugadoresItems = items,
            isCoachPrincipal = isPrincipal,
            coachId = currentCoachId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JugadoresUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCoachFiltroChange(coachId: String?) {
        _coachFiltroId.value = coachId
    }

    fun onSeleccionarJugadorParaReasignar(jugador: Jugador?) {
        _jugadorParaReasignar.value = jugador
    }

    fun reasignarCoach(jugadorId: String, nuevoCoachId: String) {
        viewModelScope.launch {
            repository.reasignarCoachAJugador(jugadorId, nuevoCoachId)
            _jugadorParaReasignar.value = null
        }
    }
}
