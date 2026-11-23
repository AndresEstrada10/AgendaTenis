package com.example.agendatenis.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.ConfigUsuario
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.Usuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val isPasswordVisible: Boolean = false,
    val recordarSesion: Boolean = true,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

sealed interface AuthEvent {
    data class LoginSuccess(val user: Usuario) : AuthEvent
    data class RegistroSuccess(val user: Usuario) : AuthEvent
}

class AuthViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    val usuarioActual: StateFlow<Usuario?> = repository.usuarioActual
    val configs: StateFlow<Map<String, ConfigUsuario>> = repository.configs

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registroUiState = MutableStateFlow(RegistroUiState())
    val registroUiState: StateFlow<RegistroUiState> = _registroUiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow: SharedFlow<AuthEvent> = _eventFlow.asSharedFlow()

    fun onLoginCorreoChange(correo: String) {
        _loginUiState.value = _loginUiState.value.copy(correo = correo, errorMessage = null)
    }

    fun onLoginContrasenaChange(contrasena: String) {
        _loginUiState.value = _loginUiState.value.copy(contrasena = contrasena, errorMessage = null)
    }

    fun onToggleLoginPasswordVisible() {
        _loginUiState.value = _loginUiState.value.copy(
            isPasswordVisible = !_loginUiState.value.isPasswordVisible
        )
    }

    fun onLoginRecordarSesionChange(recordar: Boolean) {
        _loginUiState.value = _loginUiState.value.copy(recordarSesion = recordar)
    }

    // Aliases for login
    fun onCorreoChange(correo: String) = onLoginCorreoChange(correo)
    fun onContrasenaChange(contrasena: String) = onLoginContrasenaChange(contrasena)
    fun togglePasswordVisibility() = onToggleLoginPasswordVisible()
    fun onRecordarSesionChange(recordar: Boolean) = onLoginRecordarSesionChange(recordar)

    fun iniciarSesion(onSuccess: (Usuario) -> Unit) {
        val state = _loginUiState.value
        if (state.correo.isBlank() || state.contrasena.isBlank()) {
            _loginUiState.value = state.copy(errorMessage = "Ingrese correo y contraseña")
            return
        }

        viewModelScope.launch {
            _loginUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.iniciarSesion(state.correo, state.contrasena)
            result.onSuccess { user ->
                _loginUiState.value = _loginUiState.value.copy(isLoading = false, errorMessage = null)
                _eventFlow.emit(AuthEvent.LoginSuccess(user))
                onSuccess(user)
            }.onFailure { error ->
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun quickLoginCoach(onSuccess: (Usuario) -> Unit) {
        viewModelScope.launch {
            val user = repository.usuarios.value.firstOrNull { it.id == "user_coach_1" }
                ?: repository.usuarios.value.firstOrNull { it.rol == Rol.COACH }
            if (user != null) {
                repository.setUsuarioActual(user)
                onSuccess(user)
            }
        }
    }

    fun quickLoginAdmin(onSuccess: (Usuario) -> Unit) {
        viewModelScope.launch {
            val user = repository.usuarios.value.firstOrNull { it.id == "user_admin_1" }
                ?: repository.usuarios.value.firstOrNull { it.rol == Rol.ADMIN }
            if (user != null) {
                repository.setUsuarioActual(user)
                onSuccess(user)
            }
        }
    }

    fun onRegistroNombreChange(nombre: String) {
        _registroUiState.value = _registroUiState.value.copy(nombre = nombre, errorMessage = null)
    }

    fun onRegistroCorreoChange(correo: String) {
        _registroUiState.value = _registroUiState.value.copy(correo = correo, errorMessage = null)
    }

    fun onRegistroContrasenaChange(contrasena: String) {
        _registroUiState.value = _registroUiState.value.copy(contrasena = contrasena, errorMessage = null)
    }

    fun onRegistroConfirmarContrasenaChange(confirmar: String) {
        _registroUiState.value = _registroUiState.value.copy(confirmarContrasena = confirmar, errorMessage = null)
    }

    fun onToggleRegistroPasswordVisible() {
        _registroUiState.value = _registroUiState.value.copy(
            isPasswordVisible = !_registroUiState.value.isPasswordVisible
        )
    }

    fun onToggleRegistroConfirmPasswordVisible() {
        _registroUiState.value = _registroUiState.value.copy(
            isConfirmPasswordVisible = !_registroUiState.value.isConfirmPasswordVisible
        )
    }

    // Aliases for registro
    fun onNombreRegistroChange(nombre: String) = onRegistroNombreChange(nombre)
    fun onCorreoRegistroChange(correo: String) = onRegistroCorreoChange(correo)
    fun onContrasenaRegistroChange(contrasena: String) = onRegistroContrasenaChange(contrasena)
    fun onConfirmarContrasenaRegistroChange(confirmar: String) = onRegistroConfirmarContrasenaChange(confirmar)
    fun toggleRegistroPasswordVisibility() = onToggleRegistroPasswordVisible()
    fun toggleRegistroConfirmPasswordVisibility() = onToggleRegistroConfirmPasswordVisible()
    fun registrar(onSuccess: (Usuario) -> Unit) = registrarCoach(onSuccess)

    fun registrarCoach(onSuccess: (Usuario) -> Unit) {
        val state = _registroUiState.value
        viewModelScope.launch {
            _registroUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.registrarCoach(
                nombre = state.nombre,
                correo = state.correo,
                contrasena = state.contrasena,
                confirmarContrasena = state.confirmarContrasena
            )
            result.onSuccess { coach ->
                _registroUiState.value = _registroUiState.value.copy(isLoading = false, errorMessage = null)
                _eventFlow.emit(AuthEvent.RegistroSuccess(coach))
                onSuccess(coach)
            }.onFailure { error ->
                _registroUiState.value = _registroUiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al registrar cuenta"
                )
            }
        }
    }

    fun resetForms() {
        _loginUiState.value = LoginUiState()
        _registroUiState.value = RegistroUiState()
    }

    fun limpiarErrores() {
        _loginUiState.value = _loginUiState.value.copy(errorMessage = null)
        _registroUiState.value = _registroUiState.value.copy(errorMessage = null)
    }
}
