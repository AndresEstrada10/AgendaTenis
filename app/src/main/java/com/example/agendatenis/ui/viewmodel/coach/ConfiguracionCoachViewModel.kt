package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.ConfigUsuario
import com.example.agendatenis.model.TemaApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ConfiguracionCoachUiState(
    val config: ConfigUsuario = ConfigUsuario("default"),
    val tema: TemaApp = TemaApp.CLARO,
    val recordarSesion: Boolean = true,
    val diasAnticipacion: Int = 3,
    val mostrarConfirmarLogout: Boolean = false
)

class ConfiguracionCoachViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _mostrarConfirmarLogout = MutableStateFlow(false)
    private var coachId: String = ""

    fun init(coachId: String) {
        this.coachId = coachId
    }

    val uiState: StateFlow<ConfiguracionCoachUiState> = combine(
        repository.configs,
        _mostrarConfirmarLogout
    ) { configs, mostrarLogout ->
        val user = repository.usuarioActual.value
        val targetId = coachId.ifBlank { user?.id ?: "user_coach_1" }
        val config = configs[targetId] ?: ConfigUsuario(targetId)

        ConfiguracionCoachUiState(
            config = config,
            tema = config.tema,
            recordarSesion = config.recordarSesion,
            diasAnticipacion = config.diasAnticipacion,
            mostrarConfirmarLogout = mostrarLogout
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConfiguracionCoachUiState()
    )

    fun cambiarTema(tema: TemaApp) {
        val current = uiState.value
        viewModelScope.launch {
            repository.actualizarConfig(tema, current.recordarSesion, current.diasAnticipacion)
        }
    }

    fun cambiarRecordarSesion(recordar: Boolean) {
        val current = uiState.value
        viewModelScope.launch {
            repository.actualizarConfig(current.tema, recordar, current.diasAnticipacion)
        }
    }

    fun cambiarDiasAnticipacion(dias: Int) {
        val current = uiState.value
        viewModelScope.launch {
            repository.actualizarConfig(current.tema, current.recordarSesion, dias)
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
