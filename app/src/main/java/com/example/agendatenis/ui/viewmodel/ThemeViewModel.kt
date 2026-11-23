package com.example.agendatenis.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.ThemeRepository
import com.example.agendatenis.model.TemaApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeUiState(val cargado: Boolean = false, val tema: TemaApp? = null)

class ThemeViewModel(private val repository: ThemeRepository) : ViewModel() {
    val uiState = repository.tema.map { ThemeUiState(cargado = true, tema = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState())

    fun cambiarTema(tema: TemaApp?) {
        viewModelScope.launch { repository.guardarTema(tema) }
    }
}
