package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.Usuario
import com.example.agendatenis.repository.AdminResumen
import kotlinx.coroutines.flow.*

data class AdminHomeUiState(
    val usuarioActual: Usuario? = null,
    val resumen: AdminResumen = AdminResumen(0, 0, 0, 0),
    val isLoading: Boolean = false
)

class AdminHomeViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    val uiState: StateFlow<AdminHomeUiState> = combine(
        repository.usuarioActual,
        repository.usuarios,
        repository.jugadores,
        repository.pagos
    ) { user, _, _, _ ->
        val resumen = repository.obtenerResumenAdmin()
        AdminHomeUiState(
            usuarioActual = user,
            resumen = resumen,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminHomeUiState(isLoading = true)
    )
}
