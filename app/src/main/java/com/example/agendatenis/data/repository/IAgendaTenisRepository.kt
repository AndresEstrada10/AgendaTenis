package com.example.agendatenis.data.repository

import com.example.agendatenis.model.*
import com.example.agendatenis.repository.AdminResumen
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Contrato del repositorio para AgendaTenis.
 * Desacopla la capa de presentación (UI/ViewModels) de las fuentes de datos.
 */
interface IAgendaTenisRepository {

    // Flujos reactivos (SSOT)
    val usuarios: StateFlow<List<Usuario>>
    val ciudades: StateFlow<List<Ciudad>>
    val canchas: StateFlow<List<Cancha>>
    val jugadores: StateFlow<List<Jugador>>
    val horarios: StateFlow<List<Horario>>
    val asignaciones: StateFlow<List<Asignacion>>
    val pagos: StateFlow<List<Pago>>
    val configs: StateFlow<Map<String, ConfigUsuario>>
    val usuarioActual: StateFlow<Usuario?>
    val bannerMensaje: StateFlow<String?>

    // Autenticación y Sesión
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario>
    suspend fun registrarCoach(nombre: String, correo: String, contrasena: String, confirmarContrasena: String): Result<Usuario>
    suspend fun cerrarSesion()
    suspend fun setUsuarioActual(usuario: Usuario)

    // Configuración
    suspend fun obtenerConfigActual(): ConfigUsuario
    suspend fun actualizarConfig(tema: TemaApp, recordarSesion: Boolean, diasAnticipacion: Int)
    suspend fun setBannerMensaje(mensaje: String?)

    // Cálculos de Negocio
    suspend fun calcularEstadoPago(jugadorId: String): PagoCalculado?
    suspend fun obtenerRecordatoriosParaCoach(coachId: String): List<RecordatorioItem>
    suspend fun obtenerClasesDeHoyParaCoach(coachId: String): List<ClaseHoyItem>

    // Pagos
    suspend fun registrarPago(jugadorId: String, fecha: LocalDate, monto: Double, observacion: String?): Result<Pago>
    suspend fun actualizarPago(pagoId: String, fecha: LocalDate, monto: Double, observacion: String?): Result<Pago>
    suspend fun eliminarPago(pagoId: String): Result<Unit>
    suspend fun obtenerPagosPorJugador(jugadorId: String): List<Pago>

    // Jugadores
    suspend fun guardarJugador(
        id: String?,
        nombre: String,
        telefono: String,
        correo: String,
        fechaInicio: LocalDate,
        valorMensual: Double,
        coachId: String,
        horariosIds: List<String>
    ): Result<Jugador>
    suspend fun eliminarJugador(jugadorId: String): Result<String>

    // Horarios
    suspend fun guardarHorario(
        id: String?,
        coachId: String,
        canchaId: String,
        diaSemana: DiaSemana,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Result<Horario>
    suspend fun eliminarHorario(horarioId: String): Result<Unit>

    // Catálogos (Ciudades y Canchas)
    suspend fun guardarCiudad(id: String?, nombre: String): Result<Ciudad>
    suspend fun eliminarCiudad(ciudadId: String): Result<Unit>
    suspend fun guardarCancha(id: String?, nombre: String, referencia: String?, ciudadId: String): Result<Cancha>
    suspend fun eliminarCancha(canchaId: String): Result<Unit>

    // Administración
    suspend fun guardarCoachAdmin(id: String?, nombre: String, correo: String, contrasena: String, estado: EstadoUsuario): Result<Usuario>
    suspend fun reasignarCoachAJugador(jugadorId: String, nuevoCoachId: String): Result<Unit>
    suspend fun obtenerResumenAdmin(): AdminResumen
    suspend fun obtenerResumenCoachesGestion(): List<ResumenCoachGestion>
}
