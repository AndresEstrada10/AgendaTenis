package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.EstadoUsuario
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.Usuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminCoachesUiState(
    val coaches: List<Usuario> = emptyList(),
    val searchQuery: String = "",
    val coachesFiltrados: List<Usuario> = emptyList(),
    val isLoading: Boolean = false
)

class AdminCoachesViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<AdminCoachesUiState> = combine(
        repository.usuarios,
        _searchQuery
    ) { usuarios, query ->
        val coaches = usuarios.filter { it.rol == Rol.COACH }
        val filtrados = if (query.isBlank()) {
            coaches
        } else {
            coaches.filter {
                it.nombre.contains(query, ignoreCase = true) || it.correo.contains(query, ignoreCase = true)
            }
        }.sortedBy { it.nombre }

        AdminCoachesUiState(
            coaches = coaches,
            searchQuery = query,
            coachesFiltrados = filtrados,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminCoachesUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleEstadoCoach(coach: Usuario) {
        val nuevoEstado = if (coach.estado == EstadoUsuario.ACTIVO) EstadoUsuario.INACTIVO else EstadoUsuario.ACTIVO
        viewModelScope.launch {
            repository.guardarCoachAdmin(
                id = coach.id,
                nombre = coach.nombre,
                correo = coach.correo,
                contrasena = "",
                estado = nuevoEstado
            )
        }
    }
}
