package com.example.agendatenis.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.ConfigUsuario
import com.example.agendatenis.model.TemaApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminConfiguracionUiState(
    val config: ConfigUsuario = ConfigUsuario("admin"),
    val tema: TemaApp = TemaApp.CLARO,
    val recordarSesion: Boolean = true,
    val mostrarConfirmarLogout: Boolean = false
)

class AdminConfiguracionViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _mostrarConfirmarLogout = MutableStateFlow(false)
    private var adminId: String = ""

    fun init(adminId: String) {
        this.adminId = adminId
    }

    val uiState: StateFlow<AdminConfiguracionUiState> = combine(
        repository.configs,
        _mostrarConfirmarLogout
    ) { configs, mostrarLogout ->
        val user = repository.usuarioActual.value
        val targetId = adminId.ifBlank { user?.id ?: "user_admin_1" }
        val config = configs[targetId] ?: ConfigUsuario(targetId)

        AdminConfiguracionUiState(
            config = config,
            tema = config.tema,
            recordarSesion = config.recordarSesion,
            mostrarConfirmarLogout = mostrarLogout
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminConfiguracionUiState()
    )

    fun cambiarTema(tema: TemaApp) {
        val current = uiState.value
        viewModelScope.launch {
            repository.actualizarConfig(tema, current.recordarSesion, current.config.diasAnticipacion)
        }
    }

    fun cambiarRecordarSesion(recordar: Boolean) {
        val current = uiState.value
        viewModelScope.launch {
            repository.actualizarConfig(current.tema, recordar, current.config.diasAnticipacion)
        }
    }

    fun setMostrarConfirmarLogout(visible: Boolean) {
        _mostrarConfirmarLogout.value = visible
    }

    fun cerrarSesion(onLogout: () -> Unit) {
        viewModelScope.launch {
            _mostrarConfirmarLogout.value = false
            repository.cerrarSesion()
            onLogout()
        }
    }
}
