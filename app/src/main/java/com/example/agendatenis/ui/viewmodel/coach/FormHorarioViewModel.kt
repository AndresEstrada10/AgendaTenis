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
import java.time.LocalTime

data class FormHorarioUiState(
    val horarioId: String? = null,
    val coachId: String = "",
    val canchaId: String = "",
    val diaSemana: DiaSemana = DiaSemana.LUNES,
    val horaInicio: LocalTime = LocalTime.of(8, 0),
    val horaFin: LocalTime = LocalTime.of(9, 30),
    val canchas: List<Cancha> = emptyList(),
    val ciudadesMap: Map<String, Ciudad> = emptyMap(),
    val coaches: List<Usuario> = emptyList(),
    val errorMessage: String? = null,
    val isGuardando: Boolean = false,
    val esEdicion: Boolean = false
)

class FormHorarioViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormHorarioUiState())
    val uiState: StateFlow<FormHorarioUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun init(coachId: String, horarioId: String?) {
        if (!initialized) {
            val canchasList = repository.canchas.value
            val ciudadesMap = repository.ciudades.value.associateBy { it.id }
            val coachesList = repository.usuarios.value.filter { it.rol == Rol.COACH }

            var selectedCoachId = coachId
            var selectedCanchaId = canchasList.firstOrNull()?.id ?: ""
            var selectedDia = DiaSemana.LUNES
            var selectedInicio = LocalTime.of(8, 0)
            var selectedFin = LocalTime.of(9, 30)

            if (horarioId != null) {
                val h = repository.horarios.value.firstOrNull { it.id == horarioId }
                if (h != null) {
                    selectedCoachId = h.coachId
                    selectedCanchaId = h.canchaId
                    selectedDia = h.diaSemana
                    selectedInicio = h.horaInicio
                    selectedFin = h.horaFin
                }
            }

            _uiState.value = FormHorarioUiState(
                horarioId = horarioId,
                coachId = selectedCoachId,
                canchaId = selectedCanchaId,
                diaSemana = selectedDia,
                horaInicio = selectedInicio,
                horaFin = selectedFin,
                canchas = canchasList,
                ciudadesMap = ciudadesMap,
                coaches = coachesList,
                esEdicion = horarioId != null
            )
            initialized = true
        }
    }

    fun onCoachIdChange(coachId: String) {
        _uiState.update { it.copy(coachId = coachId) }
    }

    fun onCanchaIdChange(canchaId: String) {
        _uiState.update { it.copy(canchaId = canchaId, errorMessage = null) }
    }

    fun onDiaSemanaChange(dia: DiaSemana) {
        _uiState.update { it.copy(diaSemana = dia) }
    }

    fun onHoraInicioChange(hora: LocalTime) {
        _uiState.update { it.copy(horaInicio = hora, errorMessage = null) }
    }

    fun onHoraFinChange(hora: LocalTime) {
        _uiState.update { it.copy(horaFin = hora, errorMessage = null) }
    }

    fun guardar(onSuccess: () -> Unit) {
        val state = _uiState.value
        val coachId = state.coachId
        val canchaId = state.canchaId
        val dia = state.diaSemana
        val inicio = state.horaInicio
        val fin = state.horaFin

        if (canchaId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Debe seleccionar una cancha") }
            return
        }

        if (!fin.isAfter(inicio)) {
            _uiState.update { it.copy(errorMessage = "La hora de fin debe ser posterior a la hora de inicio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true, errorMessage = null) }
            val result = repository.guardarHorario(
                id = state.horarioId,
                coachId = coachId,
                canchaId = canchaId,
                diaSemana = dia,
                horaInicio = inicio,
                horaFin = fin
            )
            _uiState.update { it.copy(isGuardando = false) }
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Error al guardar el horario") }
            }
        }
    }

    fun guardarHorario(onSuccess: () -> Unit) {
        guardar(onSuccess)
    }
}
