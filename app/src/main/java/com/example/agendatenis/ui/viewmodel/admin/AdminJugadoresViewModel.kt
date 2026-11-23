package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.Jugador
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.Usuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminJugadoresUiState(
    val searchQuery: String = "",
    val coachFiltroId: String? = null,
    val jugadorParaReasignar: Jugador? = null,
    val coaches: List<Usuario> = emptyList(),
    val coachesMap: Map<String, Usuario> = emptyMap(),
    val jugadoresFiltrados: List<Jugador> = emptyList(),
    val isLoading: Boolean = false
)

class AdminJugadoresViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _coachFiltroId = MutableStateFlow<String?>(null)
    private val _jugadorParaReasignar = MutableStateFlow<Jugador?>(null)

    val uiState: StateFlow<AdminJugadoresUiState> = combine(
        repository.jugadores,
        repository.usuarios,
        _searchQuery,
        _coachFiltroId,
        _jugadorParaReasignar
    ) { jugadores, usuarios, search, coachFiltro, reasignarJugador ->
        val coachesList = usuarios.filter { it.rol == Rol.COACH }
        val coachesMap = coachesList.associateBy { it.id }

        val filtrados = jugadores
            .filter {
                if (coachFiltro == null) true else it.coachId == coachFiltro
            }
            .filter {
                if (search.isBlank()) true
                else it.nombre.contains(search, ignoreCase = true) ||
                        it.telefono.contains(search) ||
                        it.correo.contains(search, ignoreCase = true)
            }
            .sortedBy { it.nombre }

        AdminJugadoresUiState(
            searchQuery = search,
            coachFiltroId = coachFiltro,
            jugadorParaReasignar = reasignarJugador,
            coaches = coachesList,
            coachesMap = coachesMap,
            jugadoresFiltrados = filtrados,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminJugadoresUiState(isLoading = true)
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

    fun seleccionarJugadorParaReasignar(jugador: Jugador?) {
        _jugadorParaReasignar.value = jugador
    }

    fun reasignarCoach(jugadorId: String, nuevoCoachId: String) {
        viewModelScope.launch {
            repository.reasignarCoachAJugador(jugadorId, nuevoCoachId)
            _jugadorParaReasignar.value = null
        }
    }
}
