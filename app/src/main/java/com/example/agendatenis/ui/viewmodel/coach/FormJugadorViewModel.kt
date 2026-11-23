package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FormJugadorUiState(
    val jugadorId: String? = null,
    val nombre: String = "",
    val telefono: String = "",
    val correo: String = "",
    val fechaInicio: LocalDate = LocalDate.now(),
    val valorMensual: String = "50.0",
    val coachId: String = "",
    val horariosSeleccionados: Set<String> = emptySet(),
    val coaches: List<Usuario> = emptyList(),
    val horariosDisponibles: List<Horario> = emptyList(),
    val canchasMap: Map<String, Cancha> = emptyMap(),
    val ciudadesMap: Map<String, Ciudad> = emptyMap(),
    val errorMessage: String? = null,
    val isGuardando: Boolean = false,
    val esEdicion: Boolean = false
)

class FormJugadorViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormJugadorUiState())
    val uiState: StateFlow<FormJugadorUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun init(coachId: String, jugadorId: String?) {
        if (!initialized) {
            val coachesList = repository.usuarios.value.filter { it.rol == Rol.COACH }
            val canchasMap = repository.canchas.value.associateBy { it.id }
            val ciudadesMap = repository.ciudades.value.associateBy { it.id }

            var nombre = ""
            var telefono = ""
            var correo = ""
            var fechaInicio = LocalDate.now()
            var valorMensual = "50.0"
            var targetCoachId = coachId
            var horariosSel = emptySet<String>()

            if (jugadorId != null) {
                val j = repository.jugadores.value.firstOrNull { it.id == jugadorId }
                if (j != null) {
                    nombre = j.nombre
                    telefono = j.telefono
                    correo = j.correo
                    fechaInicio = j.fechaInicio
                    valorMensual = j.valorMensual.toString()
                    targetCoachId = j.coachId
                    horariosSel = repository.asignaciones.value
                        .filter { it.jugadorId == jugadorId }
                        .map { it.horarioId }
                        .toSet()
                }
            }

            val horariosDisponibles = repository.horarios.value.filter { it.coachId == targetCoachId }

            _uiState.value = FormJugadorUiState(
                jugadorId = jugadorId,
                nombre = nombre,
                telefono = telefono,
                correo = correo,
                fechaInicio = fechaInicio,
                valorMensual = valorMensual,
                coachId = targetCoachId,
                horariosSeleccionados = horariosSel,
                coaches = coachesList,
                horariosDisponibles = horariosDisponibles,
                canchasMap = canchasMap,
                ciudadesMap = ciudadesMap,
                esEdicion = jugadorId != null
            )
            initialized = true
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, errorMessage = null) }
    }

    fun onTelefonoChange(telefono: String) {
        _uiState.update { it.copy(telefono = telefono, errorMessage = null) }
    }

    fun onCorreoChange(correo: String) {
        _uiState.update { it.copy(correo = correo, errorMessage = null) }
    }

    fun onFechaInicioChange(fecha: LocalDate) {
        _uiState.update { it.copy(fechaInicio = fecha) }
    }

    fun onValorMensualChange(valor: String) {
        _uiState.update { it.copy(valorMensual = valor, errorMessage = null) }
    }

    fun onCoachIdChange(coachId: String) {
        val nuevosHorariosDisponibles = repository.horarios.value.filter { it.coachId == coachId }
        _uiState.update {
            it.copy(
                coachId = coachId,
                horariosDisponibles = nuevosHorariosDisponibles,
                horariosSeleccionados = emptySet()
            )
        }
    }

    fun toggleHorario(horarioId: String) {
        _uiState.update { state ->
            val set = if (state.horariosSeleccionados.contains(horarioId)) {
                state.horariosSeleccionados - horarioId
            } else {
                state.horariosSeleccionados + horarioId
            }
            state.copy(horariosSeleccionados = set)
        }
    }

    fun guardar(onSuccess: () -> Unit) {
        val state = _uiState.value
        val nombre = state.nombre.trim()
        val telefono = state.telefono.trim()
        val correo = state.correo.trim()
        val valorDouble = state.valorMensual.toDoubleOrNull()

        if (nombre.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre es obligatorio") }
            return
        }
        if (telefono.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El teléfono es obligatorio") }
            return
        }
        if (correo.isBlank() || !correo.contains("@")) {
            _uiState.update { it.copy(errorMessage = "El correo electrónico es obligatorio y debe tener un formato válido") }
            return
        }
        if (valorDouble == null || valorDouble <= 0) {
            _uiState.update { it.copy(errorMessage = "El valor mensual debe ser un número válido mayor a 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true, errorMessage = null) }
            val result = repository.guardarJugador(
                id = state.jugadorId,
                nombre = nombre,
                telefono = telefono,
                correo = correo,
                fechaInicio = state.fechaInicio,
                valorMensual = valorDouble,
                coachId = state.coachId,
                horariosIds = state.horariosSeleccionados.toList()
            )
            _uiState.update { it.copy(isGuardando = false) }
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Error al guardar jugador") }
            }
        }
    }

    fun guardarJugador(onSuccess: () -> Unit) {
        guardar(onSuccess)
    }
}
