package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.Cancha
import com.example.agendatenis.model.Ciudad
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminCatalogosUiState(
    val ciudades: List<Ciudad> = emptyList(),
    val canchas: List<Cancha> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class AdminCatalogosViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminCatalogosUiState> = combine(
        repository.ciudades,
        repository.canchas,
        _errorMessage
    ) { ciudades, canchas, error ->
        AdminCatalogosUiState(
            ciudades = ciudades,
            canchas = canchas,
            errorMessage = error,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminCatalogosUiState(isLoading = true)
    )

    fun guardarCiudad(id: String?, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.guardarCiudad(id, nombre)
            result.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onResult(false, error.message)
            }
        }
    }

    fun eliminarCiudad(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.eliminarCiudad(id)
            result.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onResult(false, error.message)
            }
        }
    }

    fun guardarCancha(id: String?, nombre: String, referencia: String?, ciudadId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.guardarCancha(id, nombre, referencia, ciudadId)
            result.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onResult(false, error.message)
            }
        }
    }

    fun eliminarCancha(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.eliminarCancha(id)
            result.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onResult(false, error.message)
            }
        }
    }

    fun limpiarError() {
        _errorMessage.value = null
    }
}
