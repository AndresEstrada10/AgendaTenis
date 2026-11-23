package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetalleJugadorUiState(
    val jugador: Jugador? = null,
    val pagoCalculado: PagoCalculado? = null,
    val pagos: List<Pago> = emptyList(),
    val horariosAsignados: List<Horario> = emptyList(),
    val canchasMap: Map<String, Cancha> = emptyMap(),
    val coachAsignado: Usuario? = null,
    val mostrarDialogEliminarJugador: Boolean = false,
    val pagoParaEliminar: Pago? = null,
    val mensajeResultado: String? = null,
    val isLoading: Boolean = false
)

class DetalleJugadorViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _jugadorId = MutableStateFlow("")
    private val _mostrarDialogEliminarJugador = MutableStateFlow(false)
    private val _pagoParaEliminar = MutableStateFlow<Pago?>(null)
    private val _mensajeResultado = MutableStateFlow<String?>(null)

    fun init(jugadorId: String) {
        _jugadorId.value = jugadorId
    }

    fun cargarJugador(jugadorId: String) {
        _jugadorId.value = jugadorId
    }

    private data class DialogsState(
        val dialogEliminar: Boolean,
        val pagoEliminar: Pago?,
        val mensaje: String?
    )

    private val dialogsState = combine(_mostrarDialogEliminarJugador, _pagoParaEliminar, _mensajeResultado) { d, p, m ->
        DialogsState(d, p, m)
    }

    val uiState: StateFlow<DetalleJugadorUiState> = combine(
        _jugadorId,
        repository.jugadores,
        repository.pagos,
        repository.asignaciones,
        dialogsState
    ) { jId, jugadores, pagos, asignaciones, dialogs ->
        val jugador = jugadores.firstOrNull { it.id == jId }
        val calc = if (jugador != null) repository.calcularEstadoPago(jugador.id) else null
        val jugadorPagos = pagos.filter { it.jugadorId == jId }.sortedByDescending { it.fecha }
        val asignadosHorariosIds = asignaciones.filter { it.jugadorId == jId }.map { it.horarioId }.toSet()
        val jugadorHorarios = repository.horarios.value.filter { it.id in asignadosHorariosIds }
        val canchasMap = repository.canchas.value.associateBy { it.id }
        val coach = jugador?.let { j -> repository.usuarios.value.firstOrNull { it.id == j.coachId } }

        DetalleJugadorUiState(
            jugador = jugador,
            pagoCalculado = calc,
            pagos = jugadorPagos,
            horariosAsignados = jugadorHorarios,
            canchasMap = canchasMap,
            coachAsignado = coach,
            mostrarDialogEliminarJugador = dialogs.dialogEliminar,
            pagoParaEliminar = dialogs.pagoEliminar,
            mensajeResultado = dialogs.mensaje,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetalleJugadorUiState(isLoading = true)
    )

    fun setMostrarDialogEliminarJugador(visible: Boolean) {
        _mostrarDialogEliminarJugador.value = visible
    }

    fun setMostrarConfirmarEliminar(visible: Boolean) {
        _mostrarDialogEliminarJugador.value = visible
    }

    fun setPagoParaEliminar(pago: Pago?) {
        _pagoParaEliminar.value = pago
    }

    fun setPagoAEliminar(pago: Pago?) {
        _pagoParaEliminar.value = pago
    }

    fun setMensajeResultado(mensaje: String?) {
        _mensajeResultado.value = mensaje
    }

    fun eliminarJugador(onSuccess: (String) -> Unit) {
        val jId = _jugadorId.value
        if (jId.isBlank()) return
        viewModelScope.launch {
            _mostrarDialogEliminarJugador.value = false
            val result = repository.eliminarJugador(jId)
            result.onSuccess { mensaje ->
                _mensajeResultado.value = mensaje
                onSuccess(mensaje)
            }.onFailure { error ->
                _mensajeResultado.value = error.message
            }
        }
    }

    fun eliminarJugadorConfirmado(onSuccess: () -> Unit) {
        eliminarJugador { onSuccess() }
    }

    fun eliminarPago(onSuccess: () -> Unit) {
        val pago = _pagoParaEliminar.value ?: return
        viewModelScope.launch {
            repository.eliminarPago(pago.id)
            _pagoParaEliminar.value = null
            onSuccess()
        }
    }

    fun eliminarPagoConfirmado() {
        eliminarPago {}
    }
}
