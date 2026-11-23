package com.example.agendatenis.data.datasource

import com.example.agendatenis.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * Implementación en memoria del Data Source de AgendaTenis.
 * Actúa como Single Source of Truth (SSOT), ejecutando operaciones en Dispatchers.IO
 * e implementando retardos asíncronos simulados (delay) para representar llamadas de I/O.
 */
class InMemoryAgendaTenisDataSource : AgendaTenisDataSource {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    override val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _ciudades = MutableStateFlow<List<Ciudad>>(emptyList())
    override val ciudades: StateFlow<List<Ciudad>> = _ciudades.asStateFlow()

    private val _canchas = MutableStateFlow<List<Cancha>>(emptyList())
    override val canchas: StateFlow<List<Cancha>> = _canchas.asStateFlow()

    private val _jugadores = MutableStateFlow<List<Jugador>>(emptyList())
    override val jugadores: StateFlow<List<Jugador>> = _jugadores.asStateFlow()

    private val _horarios = MutableStateFlow<List<Horario>>(emptyList())
    override val horarios: StateFlow<List<Horario>> = _horarios.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<Asignacion>>(emptyList())
    override val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    override val pagos: StateFlow<List<Pago>> = _pagos.asStateFlow()

    private val _configs = MutableStateFlow<Map<String, ConfigUsuario>>(emptyMap())
    override val configs: StateFlow<Map<String, ConfigUsuario>> = _configs.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    override val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _bannerMensaje = MutableStateFlow<String?>(null)
    override val bannerMensaje: StateFlow<String?> = _bannerMensaje.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val today = LocalDate.now()

        val admin = Usuario(
            id = "user_admin_1",
            nombre = "Andres Estrada",
            correo = "andres@agendatenis.com",
            contrasena = "admin123",
            rol = Rol.ADMIN,
            estado = EstadoUsuario.ACTIVO
        )
        val coach1 = Usuario(
            id = "user_coach_1",
            nombre = "Jorge Sanmartin",
            correo = "jorge@agendatenis.com",
            contrasena = "coach123",
            rol = Rol.COACH,
            estado = EstadoUsuario.ACTIVO
        )
        val coach2 = Usuario(
            id = "user_coach_2",
            nombre = "Mariana Silva",
            correo = "mariana@agendatenis.com",
            contrasena = "coach123",
            rol = Rol.COACH,
            estado = EstadoUsuario.ACTIVO
        )
        _usuarios.value = listOf(admin, coach1, coach2)

        _configs.value = mapOf(
            admin.id to ConfigUsuario(usuarioId = admin.id, tema = TemaApp.CLARO, recordarSesion = true, diasAnticipacion = 3),
            coach1.id to ConfigUsuario(usuarioId = coach1.id, tema = TemaApp.CLARO, recordarSesion = true, diasAnticipacion = 3),
            coach2.id to ConfigUsuario(usuarioId = coach2.id, tema = TemaApp.CLARO, recordarSesion = true, diasAnticipacion = 3)
        )

        val ciudadManta = Ciudad(id = "ciu_1", nombre = "Manta")
        val ciudadPortoviejo = Ciudad(id = "ciu_2", nombre = "Portoviejo")
        _ciudades.value = listOf(ciudadManta, ciudadPortoviejo)

        val cancha1 = Cancha(id = "can_1", nombre = "Cancha 1 (Arcilla)", referencia = "Club ULEAM Tenis", ciudadId = ciudadManta.id)
        val cancha2 = Cancha(id = "can_2", nombre = "Cancha 2 (Rápida)", referencia = "Parque La Poza", ciudadId = ciudadManta.id)
        val cancha3 = Cancha(id = "can_3", nombre = "Cancha Central", referencia = "Portoviejo Tenis Club", ciudadId = ciudadPortoviejo.id)
        _canchas.value = listOf(cancha1, cancha2, cancha3)

        val fechaInicioJ1 = today.minusMonths(2).plusDays(2)
        val j1 = Jugador(
            id = "jug_1",
            nombre = "Angel Veliz",
            telefono = "0987654321",
            correo = "angel.veliz@ejemplo.com",
            fechaInicio = fechaInicioJ1,
            valorMensual = 75.0,
            coachId = coach1.id,
            estado = EstadoJugador.ACTIVO
        )
        val fechaInicioJ2 = today.minusMonths(3).minusDays(3)
        val j2 = Jugador(
            id = "jug_2",
            nombre = "Valeria Ramos",
            telefono = "0991234567",
            correo = "valeria.ramos@ejemplo.com",
            fechaInicio = fechaInicioJ2,
            valorMensual = 50.0,
            coachId = coach1.id,
            estado = EstadoJugador.ACTIVO
        )
        val fechaInicioJ3 = today.minusMonths(1).plusDays(20)
        val j3 = Jugador(
            id = "jug_3",
            nombre = "Mateo Delgado",
            telefono = "0978901234",
            correo = "mateo.delgado@ejemplo.com",
            fechaInicio = fechaInicioJ3,
            valorMensual = 120.0,
            coachId = coach1.id,
            estado = EstadoJugador.ACTIVO
        )
        val j4 = Jugador(
            id = "jug_4",
            nombre = "Camila Zambrano",
            telefono = "0965432109",
            correo = "camila.zambrano@ejemplo.com",
            fechaInicio = today.minusDays(5),
            valorMensual = 50.0,
            coachId = coach1.id,
            estado = EstadoJugador.ACTIVO
        )
        val j5 = Jugador(
            id = "jug_5",
            nombre = "Lucas Intriago",
            telefono = "0954321098",
            correo = "lucas.intriago@ejemplo.com",
            fechaInicio = today.minusMonths(1),
            valorMensual = 80.0,
            coachId = coach2.id,
            estado = EstadoJugador.ACTIVO
        )
        _jugadores.value = listOf(j1, j2, j3, j4, j5)

        val p1_1 = Pago(id = "pago_1", jugadorId = j1.id, fecha = fechaInicioJ1, monto = 75.0, observacion = "Primer mes - Transferencia")
        val p1_2 = Pago(id = "pago_2", jugadorId = j1.id, fecha = fechaInicioJ1.plusMonths(1), monto = 75.0, observacion = "Segundo mes - Efectivo")
        val p2_1 = Pago(id = "pago_3", jugadorId = j2.id, fecha = fechaInicioJ2, monto = 50.0, observacion = "Mes 1")
        val p2_2 = Pago(id = "pago_4", jugadorId = j2.id, fecha = fechaInicioJ2.plusMonths(1), monto = 50.0, observacion = "Mes 2")
        val p3_1 = Pago(id = "pago_5", jugadorId = j3.id, fecha = fechaInicioJ3, monto = 120.0, observacion = "Mes 1 anticipado")
        _pagos.value = listOf(p1_1, p1_2, p2_1, p2_2, p3_1)

        val diaHoy = DiaSemana.fromIso(today.dayOfWeek.value)
        val diaManana = DiaSemana.fromIso(today.plusDays(1).dayOfWeek.value)

        val h1 = Horario(id = "hor_1", coachId = coach1.id, canchaId = cancha1.id, diaSemana = diaHoy, horaInicio = LocalTime.of(8, 0), horaFin = LocalTime.of(9, 30))
        val h2 = Horario(id = "hor_2", coachId = coach1.id, canchaId = cancha1.id, diaSemana = diaHoy, horaInicio = LocalTime.of(16, 0), horaFin = LocalTime.of(17, 30))
        val h3 = Horario(id = "hor_3", coachId = coach1.id, canchaId = cancha2.id, diaSemana = diaManana, horaInicio = LocalTime.of(9, 0), horaFin = LocalTime.of(10, 30))
        _horarios.value = listOf(h1, h2, h3)

        val asig1 = Asignacion(id = "asig_1", jugadorId = j1.id, horarioId = h1.id)
        val asig2 = Asignacion(id = "asig_2", jugadorId = j2.id, horarioId = h2.id)
        val asig3 = Asignacion(id = "asig_3", jugadorId = j3.id, horarioId = h3.id)
        _asignaciones.value = listOf(asig1, asig2, asig3)
    }

    override suspend fun setUsuarios(usuarios: List<Usuario>) = withContext(Dispatchers.IO) {
        delay(40)
        _usuarios.value = usuarios
    }

    override suspend fun addUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        delay(40)
        _usuarios.value = _usuarios.value + usuario
    }

    override suspend fun updateUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        delay(40)
        _usuarios.value = _usuarios.value.map { if (it.id == usuario.id) usuario else it }
    }

    override suspend fun setCiudades(ciudades: List<Ciudad>) = withContext(Dispatchers.IO) {
        delay(40)
        _ciudades.value = ciudades
    }

    override suspend fun addCiudad(ciudad: Ciudad) = withContext(Dispatchers.IO) {
        delay(40)
        _ciudades.value = _ciudades.value + ciudad
    }

    override suspend fun updateCiudad(ciudad: Ciudad) = withContext(Dispatchers.IO) {
        delay(40)
        _ciudades.value = _ciudades.value.map { if (it.id == ciudad.id) ciudad else it }
    }

    override suspend fun removeCiudad(ciudadId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _ciudades.value = _ciudades.value.filterNot { it.id == ciudadId }
    }

    override suspend fun setCanchas(canchas: List<Cancha>) = withContext(Dispatchers.IO) {
        delay(40)
        _canchas.value = canchas
    }

    override suspend fun addCancha(cancha: Cancha) = withContext(Dispatchers.IO) {
        delay(40)
        _canchas.value = _canchas.value + cancha
    }

    override suspend fun updateCancha(cancha: Cancha) = withContext(Dispatchers.IO) {
        delay(40)
        _canchas.value = _canchas.value.map { if (it.id == cancha.id) cancha else it }
    }

    override suspend fun removeCancha(canchaId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _canchas.value = _canchas.value.filterNot { it.id == canchaId }
    }

    override suspend fun setJugadores(jugadores: List<Jugador>) = withContext(Dispatchers.IO) {
        delay(40)
        _jugadores.value = jugadores
    }

    override suspend fun addJugador(jugador: Jugador) = withContext(Dispatchers.IO) {
        delay(40)
        _jugadores.value = _jugadores.value + jugador
    }

    override suspend fun updateJugador(jugador: Jugador) = withContext(Dispatchers.IO) {
        delay(40)
        _jugadores.value = _jugadores.value.map { if (it.id == jugador.id) jugador else it }
    }

    override suspend fun removeJugador(jugadorId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _jugadores.value = _jugadores.value.filterNot { it.id == jugadorId }
    }

    override suspend fun setHorarios(horarios: List<Horario>) = withContext(Dispatchers.IO) {
        delay(40)
        _horarios.value = horarios
    }

    override suspend fun addHorario(horario: Horario) = withContext(Dispatchers.IO) {
        delay(40)
        _horarios.value = _horarios.value + horario
    }

    override suspend fun updateHorario(horario: Horario) = withContext(Dispatchers.IO) {
        delay(40)
        _horarios.value = _horarios.value.map { if (it.id == horario.id) horario else it }
    }

    override suspend fun removeHorario(horarioId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _horarios.value = _horarios.value.filterNot { it.id == horarioId }
    }

    override suspend fun setAsignaciones(asignaciones: List<Asignacion>) = withContext(Dispatchers.IO) {
        delay(40)
        _asignaciones.value = asignaciones
    }

    override suspend fun replaceAsignacionesForJugador(jugadorId: String, nuevas: List<Asignacion>) = withContext(Dispatchers.IO) {
        delay(40)
        _asignaciones.value = _asignaciones.value.filterNot { it.jugadorId == jugadorId } + nuevas
    }

    override suspend fun removeAsignacionesPorJugador(jugadorId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _asignaciones.value = _asignaciones.value.filterNot { it.jugadorId == jugadorId }
    }

    override suspend fun removeAsignacionesPorHorario(horarioId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _asignaciones.value = _asignaciones.value.filterNot { it.horarioId == horarioId }
    }

    override suspend fun setPagos(pagos: List<Pago>) = withContext(Dispatchers.IO) {
        delay(40)
        _pagos.value = pagos
    }

    override suspend fun addPago(pago: Pago) = withContext(Dispatchers.IO) {
        delay(40)
        _pagos.value = _pagos.value + pago
    }

    override suspend fun updatePago(pago: Pago) = withContext(Dispatchers.IO) {
        delay(40)
        _pagos.value = _pagos.value.map { if (it.id == pago.id) pago else it }
    }

    override suspend fun removePago(pagoId: String) = withContext(Dispatchers.IO) {
        delay(40)
        _pagos.value = _pagos.value.filterNot { it.id == pagoId }
    }

    override suspend fun setConfigs(configs: Map<String, ConfigUsuario>) = withContext(Dispatchers.IO) {
        delay(40)
        _configs.value = configs
    }

    override suspend fun updateConfig(config: ConfigUsuario) = withContext(Dispatchers.IO) {
        delay(40)
        _configs.value = _configs.value + (config.usuarioId to config)
    }

    override suspend fun setUsuarioActual(usuario: Usuario?) = withContext(Dispatchers.IO) {
        delay(40)
        _usuarioActual.value = usuario
    }

    override suspend fun setBannerMensaje(mensaje: String?) = withContext(Dispatchers.IO) {
        delay(40)
        _bannerMensaje.value = mensaje
    }
}
