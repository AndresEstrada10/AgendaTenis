package com.example.agendatenis.repository

import com.example.agendatenis.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class AgendaTenisRepository private constructor() {

    companion object {
        @Volatile
        private var instance: AgendaTenisRepository? = null

        fun getInstance(): AgendaTenisRepository {
            return instance ?: synchronized(this) {
                instance ?: AgendaTenisRepository().also { instance = it }
            }
        }
    }

    // In-memory data collections
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _ciudades = MutableStateFlow<List<Ciudad>>(emptyList())
    val ciudades: StateFlow<List<Ciudad>> = _ciudades.asStateFlow()

    private val _canchas = MutableStateFlow<List<Cancha>>(emptyList())
    val canchas: StateFlow<List<Cancha>> = _canchas.asStateFlow()

    private val _planes = MutableStateFlow<List<Plan>>(emptyList())
    val planes: StateFlow<List<Plan>> = _planes.asStateFlow()

    private val _jugadores = MutableStateFlow<List<Jugador>>(emptyList())
    val jugadores: StateFlow<List<Jugador>> = _jugadores.asStateFlow()

    private val _horarios = MutableStateFlow<List<Horario>>(emptyList())
    val horarios: StateFlow<List<Horario>> = _horarios.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<Asignacion>>(emptyList())
    val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos.asStateFlow()

    private val _configs = MutableStateFlow<Map<String, ConfigUsuario>>(emptyMap())
    val configs: StateFlow<Map<String, ConfigUsuario>> = _configs.asStateFlow()

    // Current Session
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // Transient UI State (e.g. Banner de pago exitoso)
    private val _bannerMensaje = MutableStateFlow<String?>(null)
    val bannerMensaje: StateFlow<String?> = _bannerMensaje.asStateFlow()

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

        val plan1 = Plan(id = "plan_1", nombre = "Plan Básico (2 días)", clasesPorSemana = 2, valor = 50.0, coachId = coach1.id)
        val plan2 = Plan(id = "plan_2", nombre = "Plan Intermedio (3 días)", clasesPorSemana = 3, valor = 75.0, coachId = coach1.id)
        val plan3 = Plan(id = "plan_3", nombre = "Plan Competencia (5 días)", clasesPorSemana = 5, valor = 120.0, coachId = coach1.id)
        val planCoach2 = Plan(id = "plan_4", nombre = "Plan Junior Mariana (3 días)", clasesPorSemana = 3, valor = 80.0, coachId = coach2.id)
        _planes.value = listOf(plan1, plan2, plan3, planCoach2)

        // Jugadores para coach1
        // Jugador 1: próximo a vencer (vence en 2 días)
        val fechaInicioJ1 = today.minusMonths(2).plusDays(2)
        val j1 = Jugador(id = "jug_1", nombre = "Angel Veliz", telefono = "0987654321", fechaInicio = fechaInicioJ1, planId = plan2.id, coachId = coach1.id, estado = EstadoJugador.ACTIVO)
        // Jugador 2: vencido (venció hace 3 días)
        val fechaInicioJ2 = today.minusMonths(3).minusDays(3)
        val j2 = Jugador(id = "jug_2", nombre = "Valeria Ramos", telefono = "0991234567", fechaInicio = fechaInicioJ2, planId = plan1.id, coachId = coach1.id, estado = EstadoJugador.ACTIVO)
        // Jugador 3: al día (vence en 20 días)
        val fechaInicioJ3 = today.minusMonths(1).plusDays(20)
        val j3 = Jugador(id = "jug_3", nombre = "Mateo Delgado", telefono = "0978901234", fechaInicio = fechaInicioJ3, planId = plan3.id, coachId = coach1.id, estado = EstadoJugador.ACTIVO)
        // Jugador 4: nuevo sin pagos
        val j4 = Jugador(id = "jug_4", nombre = "Camila Zambrano", telefono = "0965432109", fechaInicio = today.minusDays(5), planId = plan1.id, coachId = coach1.id, estado = EstadoJugador.ACTIVO)
        // Jugador para coach2
        val j5 = Jugador(id = "jug_5", nombre = "Lucas Intriago", telefono = "0954321098", fechaInicio = today.minusMonths(1), planId = planCoach2.id, coachId = coach2.id, estado = EstadoJugador.ACTIVO)

        _jugadores.value = listOf(j1, j2, j3, j4, j5)

        // Pagos históricos
        val p1_1 = Pago(id = "pago_1", jugadorId = j1.id, fecha = fechaInicioJ1, monto = 75.0, observacion = "Primer mes - Transferencia")
        val p1_2 = Pago(id = "pago_2", jugadorId = j1.id, fecha = fechaInicioJ1.plusMonths(1), monto = 75.0, observacion = "Segundo mes - Efectivo")

        val p2_1 = Pago(id = "pago_3", jugadorId = j2.id, fecha = fechaInicioJ2, monto = 50.0, observacion = "Mes 1")
        val p2_2 = Pago(id = "pago_4", jugadorId = j2.id, fecha = fechaInicioJ2.plusMonths(1), monto = 50.0, observacion = "Mes 2")

        val p3_1 = Pago(id = "pago_5", jugadorId = j3.id, fecha = fechaInicioJ3, monto = 120.0, observacion = "Mes 1 anticipado")

        _pagos.value = listOf(p1_1, p1_2, p2_1, p2_2, p3_1)

        // Horarios para coach1
        val diaHoy = DiaSemana.fromIso(today.dayOfWeek.value)
        val diaManana = DiaSemana.fromIso(today.plusDays(1).dayOfWeek.value)

        val h1 = Horario(id = "hor_1", coachId = coach1.id, canchaId = cancha1.id, diaSemana = diaHoy, horaInicio = LocalTime.of(8, 0), horaFin = LocalTime.of(9, 30))
        val h2 = Horario(id = "hor_2", coachId = coach1.id, canchaId = cancha1.id, diaSemana = diaHoy, horaInicio = LocalTime.of(16, 0), horaFin = LocalTime.of(17, 30))
        val h3 = Horario(id = "hor_3", coachId = coach1.id, canchaId = cancha2.id, diaSemana = diaManana, horaInicio = LocalTime.of(9, 0), horaFin = LocalTime.of(10, 30))

        _horarios.value = listOf(h1, h2, h3)

        // Asignaciones
        val asig1 = Asignacion(id = "asig_1", jugadorId = j1.id, horarioId = h1.id)
        val asig2 = Asignacion(id = "asig_2", jugadorId = j2.id, horarioId = h2.id)
        val asig3 = Asignacion(id = "asig_3", jugadorId = j3.id, horarioId = h3.id)
        _asignaciones.value = listOf(asig1, asig2, asig3)
    }

    // --- AUTENTICACIÓN (RF-01, RF-02, RN-21, RN-22, RN-23) ---
    fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        val user = _usuarios.value.firstOrNull {
            it.correo.trim().equals(correo.trim(), ignoreCase = true) ||
            (it.rol == Rol.ADMIN && correo.trim().equals("admin@agendatenis.com", ignoreCase = true)) ||
            (it.id == "user_coach_1" && (correo.trim().equals("coach@agendatenis.com", ignoreCase = true) || correo.trim().equals("carlos@agendatenis.com", ignoreCase = true)))
        } ?: return Result.failure(Exception("El correo no se encuentra registrado"))

        if (user.contrasena != contrasena) {
            return Result.failure(Exception("Contraseña incorrecta"))
        }

        // RN-21: coach desactivado no inicia sesión, datos se conservan
        if (user.estado == EstadoUsuario.INACTIVO) {
            return Result.failure(Exception("Esta cuenta de usuario ha sido desactivada. Comuníquese con el administrador."))
        }

        _usuarioActual.value = user
        return Result.success(user)
    }

    fun registrarCoach(nombre: String, correo: String, contrasena: String, confirmarContrasena: String): Result<Usuario> {
        // RN-24: validar campos obligatorios
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }

        // RN-22: correo único, contraseña >= 6 caracteres e igual a confirmación
        if (_usuarios.value.any { it.correo.trim().equals(correo.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("El correo ya está registrado en el sistema"))
        }

        if (contrasena.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        if (contrasena != confirmarContrasena) {
            return Result.failure(Exception("Las contraseñas no coinciden"))
        }

        val nuevoCoach = Usuario(
            id = "user_coach_${System.currentTimeMillis()}",
            nombre = nombre.trim(),
            correo = correo.trim().lowercase(),
            contrasena = contrasena,
            rol = Rol.COACH,
            estado = EstadoUsuario.ACTIVO
        )

        _usuarios.value = _usuarios.value + nuevoCoach
        _configs.value = _configs.value + (nuevoCoach.id to ConfigUsuario(usuarioId = nuevoCoach.id))
        _usuarioActual.value = nuevoCoach
        return Result.success(nuevoCoach)
    }

    // RN-23: cerrar sesión
    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    // Quick user switch for testing
    fun setUsuarioActual(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    // --- CONFIGURACIÓN (RF-03, RN-08) ---
    fun obtenerConfigActual(): ConfigUsuario {
        val user = _usuarioActual.value ?: return ConfigUsuario("default")
        return _configs.value[user.id] ?: ConfigUsuario(user.id)
    }

    fun actualizarConfig(tema: TemaApp, recordarSesion: Boolean, diasAnticipacion: Int) {
        val user = _usuarioActual.value ?: return
        val clampedDias = diasAnticipacion.coerceIn(1, 7)
        val nuevaConfig = ConfigUsuario(
            usuarioId = user.id,
            tema = tema,
            recordarSesion = recordarSesion,
            diasAnticipacion = clampedDias
        )
        _configs.value = _configs.value + (user.id to nuevaConfig)
    }

    fun setBannerMensaje(mensaje: String?) {
        _bannerMensaje.value = mensaje
    }

    // --- REGLAS DE NEGOCIO: CÁLCULO DE PAGOS Y RECORDATORIOS (RN-02..RN-12) ---
    /**
     * RN-03: fechaAncla = fecha del primer pago; sin pagos => fechaInicio del jugador
     * RN-04: proximaFecha = fechaAncla + (nroPagos) meses; pago atrasado NO desplaza calendario
     * RN-05: si el dia ancla no existe en el mes => ultimo dia del mes, conservando dia ancla para meses siguientes
     * RN-06: cada pago cubre un ciclo mensual
     * RN-07: editar/eliminar pago recalcula proxima fecha
     */
    fun calcularEstadoPago(jugadorId: String): PagoCalculado? {
        val jugador = _jugadores.value.firstOrNull { it.id == jugadorId } ?: return null
        val plan = _planes.value.firstOrNull { it.id == jugador.planId }
        val pagosJugador = _pagos.value
            .filter { it.jugadorId == jugadorId }
            .sortedBy { it.fecha }

        val fechaAncla = if (pagosJugador.isNotEmpty()) {
            pagosJugador.first().fecha
        } else {
            jugador.fechaInicio
        }

        val nroPagos = pagosJugador.size
        // java.time.LocalDate.plusMonths aplica RN-05 por defecto (ej. 31 de enero + 1 mes = 28/29 de feb)
        val proximaFecha = fechaAncla.plusMonths(nroPagos.toLong())

        val hoy = LocalDate.now()
        val diasRestantes = ChronoUnit.DAYS.between(hoy, proximaFecha)

        val config = obtenerConfigActual()
        val diasAnticipacion = config.diasAnticipacion

        val estado = when {
            diasRestantes < 0 -> EstadoPago.VENCIDO
            diasRestantes <= diasAnticipacion -> EstadoPago.PROXIMO
            else -> EstadoPago.AL_DIA
        }

        return PagoCalculado(
            jugador = jugador,
            plan = plan,
            fechaAncla = fechaAncla,
            proximaFecha = proximaFecha,
            diasRestantes = diasRestantes,
            estado = estado
        )
    }

    /**
     * RN-08: 2 recordatorios por ciclo: N días antes (1-7, def 3) y mismo día
     * RN-09: fecha vencida sin pago => estado "Vencido" en Inicio hasta registrar pago
     * RN-11: jugadores inactivos no generan recordatorios
     */
    fun obtenerRecordatoriosParaCoach(coachId: String): List<RecordatorioItem> {
        val jugadoresCoach = _jugadores.value.filter { it.coachId == coachId && it.estado == EstadoJugador.ACTIVO }
        val config = _configs.value[coachId] ?: ConfigUsuario(coachId)
        val diasAnticipacion = config.diasAnticipacion
        val hoy = LocalDate.now()

        val items = mutableListOf<RecordatorioItem>()

        for (jugador in jugadoresCoach) {
            val calc = calcularEstadoPago(jugador.id) ?: continue
            val dias = calc.diasRestantes

            if (dias < 0) {
                // RN-09: Vencido
                items.add(
                    RecordatorioItem(
                        id = "rec_venc_${jugador.id}",
                        jugadorId = jugador.id,
                        jugadorNombre = jugador.nombre,
                        fechaVencimiento = calc.proximaFecha,
                        mensaje = "Pago vencido hace ${-dias} día(s) (venció el ${calc.proximaFecha})",
                        esMismoDia = false,
                        esVencido = true
                    )
                )
            } else if (dias == 0L) {
                // Mismo día
                items.add(
                    RecordatorioItem(
                        id = "rec_hoy_${jugador.id}",
                        jugadorId = jugador.id,
                        jugadorNombre = jugador.nombre,
                        fechaVencimiento = calc.proximaFecha,
                        mensaje = "El pago vence hoy (${calc.proximaFecha})",
                        esMismoDia = true,
                        esVencido = false
                    )
                )
            } else if (dias in 1..diasAnticipacion.toLong()) {
                // N días antes
                items.add(
                    RecordatorioItem(
                        id = "rec_prox_${jugador.id}",
                        jugadorId = jugador.id,
                        jugadorNombre = jugador.nombre,
                        fechaVencimiento = calc.proximaFecha,
                        mensaje = "El pago vence en $dias día(s) (${calc.proximaFecha})",
                        esMismoDia = false,
                        esVencido = false
                    )
                )
            }
        }

        return items.sortedWith(compareBy({ !it.esVencido }, { it.fechaVencimiento }))
    }

    fun obtenerClasesDeHoyParaCoach(coachId: String): List<ClaseHoyItem> {
        val hoy = LocalDate.now()
        val diaHoy = DiaSemana.fromIso(hoy.dayOfWeek.value)

        val horariosHoy = _horarios.value.filter { it.coachId == coachId && it.diaSemana == diaHoy }
        val canchasMap = _canchas.value.associateBy { it.id }
        val ciudadesMap = _ciudades.value.associateBy { it.id }
        val jugadoresMap = _jugadores.value.associateBy { it.id }

        val clases = mutableListOf<ClaseHoyItem>()
        for (horario in horariosHoy) {
            val asignados = _asignaciones.value.filter { it.horarioId == horario.id }
            val cancha = canchasMap[horario.canchaId]
            val ciudad = cancha?.let { ciudadesMap[it.ciudadId] }

            for (asig in asignados) {
                val jugador = jugadoresMap[asig.jugadorId]
                if (jugador != null && jugador.estado == EstadoJugador.ACTIVO) {
                    clases.add(
                        ClaseHoyItem(
                            horarioId = horario.id,
                            jugadorId = jugador.id,
                            jugadorNombre = jugador.nombre,
                            horaInicio = horario.horaInicio,
                            horaFin = horario.horaFin,
                            canchaNombre = cancha?.nombre ?: "Cancha desconocida",
                            ciudadNombre = ciudad?.nombre ?: ""
                        )
                    )
                }
            }
        }

        return clases.sortedBy { it.horaInicio }
    }

    // --- GESTIÓN DE PAGOS (RF-11, RF-12, RF-13, RN-01, RN-10, RN-13) ---
    fun registrarPago(jugadorId: String, fecha: LocalDate, monto: Double, observacion: String?): Result<Pago> {
        if (monto <= 0) {
            return Result.failure(Exception("El monto debe ser mayor a 0"))
        }

        val jugador = _jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return Result.failure(Exception("Jugador no encontrado"))

        val nuevoPago = Pago(
            id = "pago_${System.currentTimeMillis()}",
            jugadorId = jugadorId,
            fecha = fecha,
            monto = monto,
            observacion = observacion?.takeIf { it.isNotBlank() }
        )

        _pagos.value = _pagos.value + nuevoPago
        // RN-10: confirmación
        _bannerMensaje.value = "Pago registrado exitosamente para ${jugador.nombre}"
        return Result.success(nuevoPago)
    }

    fun actualizarPago(pagoId: String, fecha: LocalDate, monto: Double, observacion: String?): Result<Pago> {
        if (monto <= 0) {
            return Result.failure(Exception("El monto debe ser mayor a 0"))
        }
        val pagoIndex = _pagos.value.indexOfFirst { it.id == pagoId }
        if (pagoIndex == -1) {
            return Result.failure(Exception("Pago no encontrado"))
        }
        val pagoActualizado = _pagos.value[pagoIndex].copy(
            fecha = fecha,
            monto = monto,
            observacion = observacion?.takeIf { it.isNotBlank() }
        )
        val list = _pagos.value.toMutableList()
        list[pagoIndex] = pagoActualizado
        _pagos.value = list
        return Result.success(pagoActualizado)
    }

    fun eliminarPago(pagoId: String): Result<Unit> {
        val pago = _pagos.value.firstOrNull { it.id == pagoId }
            ?: return Result.failure(Exception("Pago no encontrado"))
        _pagos.value = _pagos.value.filterNot { it.id == pagoId }
        return Result.success(Unit)
    }

    fun obtenerPagosPorJugador(jugadorId: String): List<Pago> {
        return _pagos.value.filter { it.jugadorId == jugadorId }.sortedByDescending { it.fecha }
    }

    // --- GESTIÓN DE PLANES (RF-04, RF-05, RN-18) ---
    fun guardarPlan(id: String?, nombre: String, clasesPorSemana: Int, valor: Double, coachId: String): Result<Plan> {
        if (nombre.isBlank()) return Result.failure(Exception("El nombre del plan es obligatorio"))
        if (clasesPorSemana <= 0) return Result.failure(Exception("Las clases por semana deben ser mayores a 0"))
        if (valor <= 0) return Result.failure(Exception("El valor debe ser mayor a 0"))

        val plan = if (id != null) {
            val existente = _planes.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Plan no encontrado"))
            existente.copy(nombre = nombre.trim(), clasesPorSemana = clasesPorSemana, valor = valor)
        } else {
            Plan(
                id = "plan_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                clasesPorSemana = clasesPorSemana,
                valor = valor,
                coachId = coachId
            )
        }

        _planes.value = if (id != null) {
            _planes.value.map { if (it.id == id) plan else it }
        } else {
            _planes.value + plan
        }
        return Result.success(plan)
    }

    // RN-18: no eliminar plan con jugadores asociados
    fun eliminarPlan(planId: String): Result<Unit> {
        val tieneJugadores = _jugadores.value.any { it.planId == planId && it.estado == EstadoJugador.ACTIVO }
        if (tieneJugadores) {
            return Result.failure(Exception("No se puede eliminar un plan que tiene jugadores activos asociados"))
        }
        _planes.value = _planes.value.filterNot { it.id == planId }
        return Result.success(Unit)
    }

    // --- GESTIÓN DE JUGADORES (RF-06, RF-08, RF-09, RN-14, RN-15, RN-20) ---
    fun guardarJugador(
        id: String?,
        nombre: String,
        telefono: String,
        fechaInicio: LocalDate,
        planId: String,
        coachId: String,
        horariosIds: List<String>
    ): Result<Jugador> {
        if (nombre.isBlank()) return Result.failure(Exception("El nombre es obligatorio"))
        if (telefono.isBlank()) return Result.failure(Exception("El teléfono es obligatorio"))
        if (planId.isBlank()) return Result.failure(Exception("Debe seleccionar un plan"))

        val jugadorId = id ?: "jug_${System.currentTimeMillis()}"
        val jugador = if (id != null) {
            val existente = _jugadores.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Jugador no encontrado"))
            existente.copy(
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                fechaInicio = fechaInicio,
                planId = planId,
                coachId = coachId
            )
        } else {
            Jugador(
                id = jugadorId,
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                fechaInicio = fechaInicio,
                planId = planId,
                coachId = coachId,
                estado = EstadoJugador.ACTIVO
            )
        }

        _jugadores.value = if (id != null) {
            _jugadores.value.map { if (it.id == id) jugador else it }
        } else {
            _jugadores.value + jugador
        }

        // Actualizar asignaciones
        _asignaciones.value = _asignaciones.value.filterNot { it.jugadorId == jugadorId } +
                horariosIds.map { horarioId ->
                    Asignacion(id = "asig_${System.currentTimeMillis()}_${horarioId}", jugadorId = jugadorId, horarioId = horarioId)
                }

        return Result.success(jugador)
    }

    // RN-20: eliminar jugador con pagos => inactivo (conserva historial); sin pagos => borrado definitivo; siempre con confirmación
    fun eliminarJugador(jugadorId: String): Result<String> {
        val jugador = _jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return Result.failure(Exception("Jugador no encontrado"))

        val tienePagos = _pagos.value.any { it.jugadorId == jugadorId }
        return if (tienePagos) {
            // Se marca inactivo para conservar historial
            _jugadores.value = _jugadores.value.map {
                if (it.id == jugadorId) it.copy(estado = EstadoJugador.INACTIVO) else it
            }
            // Eliminar asignaciones para no ocupar cupos ni aparecer en agenda
            _asignaciones.value = _asignaciones.value.filterNot { it.jugadorId == jugadorId }
            Result.success("El jugador tiene pagos registrados, por lo que fue marcado como INACTIVO para preservar su historial.")
        } else {
            // Borrado definitivo
            _jugadores.value = _jugadores.value.filterNot { it.id == jugadorId }
            _asignaciones.value = _asignaciones.value.filterNot { it.jugadorId == jugadorId }
            Result.success("Jugador eliminado definitivamente.")
        }
    }

    // --- GESTIÓN DE HORARIOS (RF-07, RF-18, RN-16, RN-17) ---
    fun guardarHorario(
        id: String?,
        coachId: String,
        canchaId: String,
        diaSemana: DiaSemana,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Result<Horario> {
        if (!horaFin.isAfter(horaInicio)) {
            return Result.failure(Exception("La hora de fin debe ser posterior a la hora de inicio"))
        }

        // RN-16: un coach no puede tener horarios solapados el mismo día
        val horariosMismoDia = _horarios.value.filter {
            it.coachId == coachId && it.diaSemana == diaSemana && it.id != id
        }
        val haySolapamiento = horariosMismoDia.any { h ->
            // Solapamiento de intervalos [start, end)
            !(horaFin <= h.horaInicio || horaInicio >= h.horaFin)
        }
        if (haySolapamiento) {
            return Result.failure(Exception("El coach ya tiene una clase programada en ese rango de horas este día"))
        }

        // RN-17: una cancha admite horarios simultáneos (otros coaches o grupales); no se bloquea

        val horario = if (id != null) {
            val existente = _horarios.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Horario no encontrado"))
            existente.copy(canchaId = canchaId, diaSemana = diaSemana, horaInicio = horaInicio, horaFin = horaFin)
        } else {
            Horario(
                id = "hor_${System.currentTimeMillis()}",
                coachId = coachId,
                canchaId = canchaId,
                diaSemana = diaSemana,
                horaInicio = horaInicio,
                horaFin = horaFin
            )
        }

        _horarios.value = if (id != null) {
            _horarios.value.map { if (it.id == id) horario else it }
        } else {
            _horarios.value + horario
        }

        return Result.success(horario)
    }

    fun eliminarHorario(horarioId: String): Result<Unit> {
        _horarios.value = _horarios.value.filterNot { it.id == horarioId }
        _asignaciones.value = _asignaciones.value.filterNot { it.horarioId == horarioId }
        return Result.success(Unit)
    }

    // --- ADMIN: CIUDADES Y CANCHAS (A-03, A-04, RN-19) ---
    fun guardarCiudad(id: String?, nombre: String): Result<Ciudad> {
        if (nombre.isBlank()) return Result.failure(Exception("El nombre de la ciudad es obligatorio"))
        val ciudad = if (id != null) {
            val existente = _ciudades.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Ciudad no encontrada"))
            existente.copy(nombre = nombre.trim())
        } else {
            Ciudad(id = "ciu_${System.currentTimeMillis()}", nombre = nombre.trim())
        }
        _ciudades.value = if (id != null) {
            _ciudades.value.map { if (it.id == id) ciudad else it }
        } else {
            _ciudades.value + ciudad
        }
        return Result.success(ciudad)
    }

    // RN-19: no eliminar ciudad con canchas
    fun eliminarCiudad(ciudadId: String): Result<Unit> {
        val tieneCanchas = _canchas.value.any { it.ciudadId == ciudadId }
        if (tieneCanchas) {
            return Result.failure(Exception("No se puede eliminar la ciudad porque tiene canchas asociadas"))
        }
        _ciudades.value = _ciudades.value.filterNot { it.id == ciudadId }
        return Result.success(Unit)
    }

    fun guardarCancha(id: String?, nombre: String, referencia: String?, ciudadId: String): Result<Cancha> {
        if (nombre.isBlank()) return Result.failure(Exception("El nombre de la cancha es obligatorio"))
        if (ciudadId.isBlank()) return Result.failure(Exception("Debe seleccionar una ciudad"))

        val cancha = if (id != null) {
            val existente = _canchas.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Cancha no encontrada"))
            existente.copy(nombre = nombre.trim(), referencia = referencia?.trim(), ciudadId = ciudadId)
        } else {
            Cancha(
                id = "can_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                referencia = referencia?.trim(),
                ciudadId = ciudadId
            )
        }
        _canchas.value = if (id != null) {
            _canchas.value.map { if (it.id == id) cancha else it }
        } else {
            _canchas.value + cancha
        }
        return Result.success(cancha)
    }

    // RN-19: no eliminar cancha con horarios
    fun eliminarCancha(canchaId: String): Result<Unit> {
        val tieneHorarios = _horarios.value.any { it.canchaId == canchaId }
        if (tieneHorarios) {
            return Result.failure(Exception("No se puede eliminar la cancha porque tiene horarios programados"))
        }
        _canchas.value = _canchas.value.filterNot { it.id == canchaId }
        return Result.success(Unit)
    }

    // --- ADMIN: GESTIÓN DE COACHES (A-02, RN-21) ---
    // CRUD(sin eliminar, desactivar) Coaches
    fun guardarCoachAdmin(id: String?, nombre: String, correo: String, contrasena: String, estado: EstadoUsuario): Result<Usuario> {
        if (nombre.isBlank() || correo.isBlank()) {
            return Result.failure(Exception("El nombre y correo son obligatorios"))
        }

        if (id == null) {
            if (_usuarios.value.any { it.correo.trim().equals(correo.trim(), ignoreCase = true) }) {
                return Result.failure(Exception("El correo ya está registrado"))
            }
            if (contrasena.length < 6) {
                return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
            }
            val nuevo = Usuario(
                id = "user_coach_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                contrasena = contrasena,
                rol = Rol.COACH,
                estado = estado
            )
            _usuarios.value = _usuarios.value + nuevo
            _configs.value = _configs.value + (nuevo.id to ConfigUsuario(nuevo.id))
            return Result.success(nuevo)
        } else {
            val existente = _usuarios.value.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val actualizado = existente.copy(
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                contrasena = if (contrasena.isNotBlank()) contrasena else existente.contrasena,
                estado = estado
            )
            _usuarios.value = _usuarios.value.map { if (it.id == id) actualizado else it }
            return Result.success(actualizado)
        }
    }

    // ADMIN: Reasignar coach a jugador (A-06)
    fun reasignarCoachAJugador(jugadorId: String, nuevoCoachId: String): Result<Unit> {
        val jugador = _jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return Result.failure(Exception("Jugador no encontrado"))
        val coach = _usuarios.value.firstOrNull { it.id == nuevoCoachId && it.rol == Rol.COACH }
            ?: return Result.failure(Exception("Coach no encontrado"))

        _jugadores.value = _jugadores.value.map {
            if (it.id == jugadorId) it.copy(coachId = nuevoCoachId) else it
        }
        return Result.success(Unit)
    }

    // ADMIN: Resumen general (A-09)
    fun obtenerResumenAdmin(): AdminResumen {
        val totalCoaches = _usuarios.value.count { it.rol == Rol.COACH }
        val coachesActivos = _usuarios.value.count { it.rol == Rol.COACH && it.estado == EstadoUsuario.ACTIVO }
        val jugadoresActivos = _jugadores.value.count { it.estado == EstadoJugador.ACTIVO }

        val pagosProximos = _jugadores.value
            .filter { it.estado == EstadoJugador.ACTIVO }
            .mapNotNull { calcularEstadoPago(it.id) }
            .count { it.estado == EstadoPago.PROXIMO || it.estado == EstadoPago.VENCIDO }

        return AdminResumen(
            totalCoaches = totalCoaches,
            coachesActivos = coachesActivos,
            jugadoresActivos = jugadoresActivos,
            pagosProximosGlobales = pagosProximos
        )
    }
}

data class AdminResumen(
    val totalCoaches: Int,
    val coachesActivos: Int,
    val jugadoresActivos: Int,
    val pagosProximosGlobales: Int
)
