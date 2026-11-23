package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.EstadoUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminFormCoachUiState(
    val coachId: String? = null,
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isGuardando: Boolean = false,
    val esEdicion: Boolean = false
)

class AdminFormCoachViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFormCoachUiState())
    val uiState: StateFlow<AdminFormCoachUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun init(coachId: String?) {
        if (!initialized) {
            if (coachId != null) {
                val coach = repository.usuarios.value.firstOrNull { it.id == coachId }
                if (coach != null) {
                    _uiState.value = AdminFormCoachUiState(
                        coachId = coachId,
                        nombre = coach.nombre,
                        correo = coach.correo,
                        contrasena = "",
                        estado = coach.estado,
                        esEdicion = true
                    )
                }
            } else {
                _uiState.value = AdminFormCoachUiState()
            }
            initialized = true
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre, errorMessage = null)
    }

    fun onCorreoChange(correo: String) {
        _uiState.value = _uiState.value.copy(correo = correo, errorMessage = null)
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.value = _uiState.value.copy(contrasena = contrasena, errorMessage = null)
    }

    fun onEstadoChange(estado: EstadoUsuario) {
        _uiState.value = _uiState.value.copy(estado = estado)
    }

    fun onTogglePasswordVisible() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun guardar(onSuccess: () -> Unit) {
        val state = _uiState.value
        val nombre = state.nombre.trim()
        val correo = state.correo.trim()
        val contrasena = state.contrasena

        if (nombre.isBlank() || correo.isBlank()) {
            _uiState.value = state.copy(errorMessage = "El nombre y correo son obligatorios")
            return
        }

        if (state.coachId == null && contrasena.length < 6) {
            _uiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isGuardando = true, errorMessage = null)
            val result = repository.guardarCoachAdmin(
                id = state.coachId,
                nombre = nombre,
                correo = correo,
                contrasena = contrasena,
                estado = state.estado
            )
            _uiState.value = _uiState.value.copy(isGuardando = false)
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Error al guardar coach"
                )
            }
        }
    }

    fun guardarCoach(onSuccess: () -> Unit) {
        guardar(onSuccess)
    }
}
