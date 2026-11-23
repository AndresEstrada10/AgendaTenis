package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.Jugador
import com.example.agendatenis.model.Pago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FormPagoUiState(
    val jugador: Jugador? = null,
    val pagoId: String? = null,
    val fecha: LocalDate = LocalDate.now(),
    val monto: String = "",
    val observacion: String = "",
    val errorMessage: String? = null,
    val isGuardando: Boolean = false,
    val esEdicion: Boolean = false
)

class FormPagoViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormPagoUiState())
    val uiState: StateFlow<FormPagoUiState> = _uiState.asStateFlow()
    private var initialized = false

    fun init(jugadorId: String, pagoId: String?) {
        if (!initialized) {
            val jugador = repository.jugadores.value.firstOrNull { it.id == jugadorId }
            var fecha = LocalDate.now()
            var monto = jugador?.valorMensual?.toString() ?: "50.0"
            var observacion = ""

            if (pagoId != null) {
                val pagoExistente = repository.pagos.value.firstOrNull { it.id == pagoId }
                if (pagoExistente != null) {
                    fecha = pagoExistente.fecha
                    monto = pagoExistente.monto.toString()
                    observacion = pagoExistente.observacion ?: ""
                }
            }

            _uiState.value = FormPagoUiState(
                jugador = jugador,
                pagoId = pagoId,
                fecha = fecha,
                monto = monto,
                observacion = observacion,
                esEdicion = pagoId != null
            )
            initialized = true
        }
    }

    fun onFechaChange(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(fecha = fecha)
    }

    fun onMontoChange(monto: String) {
        _uiState.value = _uiState.value.copy(monto = monto, errorMessage = null)
    }

    fun onObservacionChange(observacion: String) {
        _uiState.value = _uiState.value.copy(observacion = observacion)
    }

    fun guardar(onSuccess: () -> Unit) {
        val state = _uiState.value
        val montoDouble = state.monto.toDoubleOrNull()
        if (montoDouble == null || montoDouble <= 0) {
            _uiState.value = state.copy(errorMessage = "El monto debe ser un número válido mayor a 0")
            return
        }

        val jugadorId = state.jugador?.id ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isGuardando = true, errorMessage = null)
            val result: Result<Pago> = if (state.pagoId != null) {
                repository.actualizarPago(
                    pagoId = state.pagoId,
                    fecha = state.fecha,
                    monto = montoDouble,
                    observacion = state.observacion.ifBlank { null }
                )
            } else {
                repository.registrarPago(
                    jugadorId = jugadorId,
                    fecha = state.fecha,
                    monto = montoDouble,
                    observacion = state.observacion.ifBlank { null }
                )
            }

            _uiState.value = _uiState.value.copy(isGuardando = false)
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Error al guardar el pago"
                )
            }
        }
    }

    fun guardarPago(onSuccess: () -> Unit) {
        guardar(onSuccess)
    }
}
