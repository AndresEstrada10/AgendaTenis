package com.example.agendatenis.data.datasource

import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de la fuente de datos (Data Source) para AgendaTenis.
 * Aplica el principio de Single Source of Truth (SSOT).
 */
interface AgendaTenisDataSource {
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

    suspend fun setUsuarios(usuarios: List<Usuario>)
    suspend fun addUsuario(usuario: Usuario)
    suspend fun updateUsuario(usuario: Usuario)

    suspend fun setCiudades(ciudades: List<Ciudad>)
    suspend fun addCiudad(ciudad: Ciudad)
    suspend fun updateCiudad(ciudad: Ciudad)
    suspend fun removeCiudad(ciudadId: String)

    suspend fun setCanchas(canchas: List<Cancha>)
    suspend fun addCancha(cancha: Cancha)
    suspend fun updateCancha(cancha: Cancha)
    suspend fun removeCancha(canchaId: String)

    suspend fun setJugadores(jugadores: List<Jugador>)
    suspend fun addJugador(jugador: Jugador)
    suspend fun updateJugador(jugador: Jugador)
    suspend fun removeJugador(jugadorId: String)

    suspend fun setHorarios(horarios: List<Horario>)
    suspend fun addHorario(horario: Horario)
    suspend fun updateHorario(horario: Horario)
    suspend fun removeHorario(horarioId: String)

    suspend fun setAsignaciones(asignaciones: List<Asignacion>)
    suspend fun replaceAsignacionesForJugador(jugadorId: String, nuevas: List<Asignacion>)
    suspend fun removeAsignacionesPorJugador(jugadorId: String)
    suspend fun removeAsignacionesPorHorario(horarioId: String)

    suspend fun setPagos(pagos: List<Pago>)
    suspend fun addPago(pago: Pago)
    suspend fun updatePago(pago: Pago)
    suspend fun removePago(pagoId: String)

    suspend fun setConfigs(configs: Map<String, ConfigUsuario>)
    suspend fun updateConfig(config: ConfigUsuario)

    suspend fun setUsuarioActual(usuario: Usuario?)
    suspend fun setBannerMensaje(mensaje: String?)
}
