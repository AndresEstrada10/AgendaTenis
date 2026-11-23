package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.Cancha
import com.example.agendatenis.model.Ciudad
import kotlinx.coroutines.flow.*

data class CanchasCoachUiState(
    val ciudadesConCanchas: List<Pair<Ciudad, List<Cancha>>> = emptyList(),
    val isLoading: Boolean = false
)

class CanchasCoachViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    val uiState: StateFlow<CanchasCoachUiState> = combine(
        repository.ciudades,
        repository.canchas
    ) { ciudades, canchas ->
        val canchasPorCiudad = canchas.groupBy { it.ciudadId }
        val lista = ciudades.map { ciudad ->
            ciudad to (canchasPorCiudad[ciudad.id] ?: emptyList())
        }
        CanchasCoachUiState(
            ciudadesConCanchas = lista,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CanchasCoachUiState(isLoading = true)
    )
}
