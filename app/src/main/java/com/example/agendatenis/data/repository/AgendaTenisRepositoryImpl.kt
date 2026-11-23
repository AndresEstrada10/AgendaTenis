package com.example.agendatenis.data.repository

import com.example.agendatenis.data.datasource.AgendaTenisDataSource
import com.example.agendatenis.model.*
import com.example.agendatenis.repository.AdminResumen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Implementación del repositorio de AgendaTenis.
 * Aplica lógica de negocio y coordina el acceso al DataSource mediante Dispatchers.IO.
 */
class AgendaTenisRepositoryImpl(
    private val dataSource: AgendaTenisDataSource
) : IAgendaTenisRepository {

    override val usuarios: StateFlow<List<Usuario>> = dataSource.usuarios
    override val ciudades: StateFlow<List<Ciudad>> = dataSource.ciudades
    override val canchas: StateFlow<List<Cancha>> = dataSource.canchas
    override val jugadores: StateFlow<List<Jugador>> = dataSource.jugadores
    override val horarios: StateFlow<List<Horario>> = dataSource.horarios
    override val asignaciones: StateFlow<List<Asignacion>> = dataSource.asignaciones
    override val pagos: StateFlow<List<Pago>> = dataSource.pagos
    override val configs: StateFlow<Map<String, ConfigUsuario>> = dataSource.configs
    override val usuarioActual: StateFlow<Usuario?> = dataSource.usuarioActual
    override val bannerMensaje: StateFlow<String?> = dataSource.bannerMensaje

    // --- AUTENTICACIÓN (RF-01, RF-02, RN-21, RN-22, RN-23) ---
    override suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            val user = usuarios.value.firstOrNull {
                it.correo.trim().equals(correo.trim(), ignoreCase = true) ||
                        (it.rol == Rol.ADMIN && correo.trim().equals("admin@agendatenis.com", ignoreCase = true)) ||
                        (it.id == "user_coach_1" && (correo.trim().equals("coach@agendatenis.com", ignoreCase = true) || correo.trim().equals("carlos@agendatenis.com", ignoreCase = true)))
            } ?: return@withContext Result.failure(Exception("El correo no se encuentra registrado"))

            if (user.contrasena != contrasena) {
                return@withContext Result.failure(Exception("Contraseña incorrecta"))
            }

            if (user.estado == EstadoUsuario.INACTIVO) {
                return@withContext Result.failure(Exception("Esta cuenta de usuario ha sido desactivada. Comuníquese con el administrador."))
            }

            dataSource.setUsuarioActual(user)
            Result.success(user)
        }

    override suspend fun registrarCoach(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            return@withContext Result.failure(Exception("Todos los campos son obligatorios"))
        }

        if (usuarios.value.any { it.correo.trim().equals(correo.trim(), ignoreCase = true) }) {
            return@withContext Result.failure(Exception("El correo ya está registrado en el sistema"))
        }

        if (contrasena.length < 6) {
            return@withContext Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        if (contrasena != confirmarContrasena) {
            return@withContext Result.failure(Exception("Las contraseñas no coinciden"))
        }

        val nuevoCoach = Usuario(
            id = "user_coach_${System.currentTimeMillis()}",
            nombre = nombre.trim(),
            correo = correo.trim().lowercase(),
            contrasena = contrasena,
            rol = Rol.COACH,
            estado = EstadoUsuario.ACTIVO
        )

        dataSource.addUsuario(nuevoCoach)
        dataSource.updateConfig(ConfigUsuario(usuarioId = nuevoCoach.id))
        dataSource.setUsuarioActual(nuevoCoach)
        Result.success(nuevoCoach)
    }

    override suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        dataSource.setUsuarioActual(null)
    }

    override suspend fun setUsuarioActual(usuario: Usuario) = withContext(Dispatchers.IO) {
        dataSource.setUsuarioActual(usuario)
    }

    // --- CONFIGURACIÓN (RF-03, RN-08) ---
    override suspend fun obtenerConfigActual(): ConfigUsuario = withContext(Dispatchers.IO) {
        val user = usuarioActual.value ?: return@withContext ConfigUsuario("default")
        configs.value[user.id] ?: ConfigUsuario(user.id)
    }

    override suspend fun actualizarConfig(
        tema: TemaApp,
        recordarSesion: Boolean,
        diasAnticipacion: Int
    ) = withContext(Dispatchers.IO) {
        val user = usuarioActual.value ?: return@withContext
        val clampedDias = diasAnticipacion.coerceIn(1, 7)
        val nuevaConfig = ConfigUsuario(
            usuarioId = user.id,
            tema = tema,
            recordarSesion = recordarSesion,
            diasAnticipacion = clampedDias
        )
        dataSource.updateConfig(nuevaConfig)
    }

    override suspend fun setBannerMensaje(mensaje: String?) = withContext(Dispatchers.IO) {
        dataSource.setBannerMensaje(mensaje)
    }

    // --- CÁLCULO DE PAGOS Y RECORDATORIOS (RN-02..RN-12) ---
    override suspend fun calcularEstadoPago(jugadorId: String): PagoCalculado? = withContext(Dispatchers.IO) {
        val jugador = jugadores.value.firstOrNull { it.id == jugadorId } ?: return@withContext null
        val pagosJugador = pagos.value
            .filter { it.jugadorId == jugadorId }
            .sortedBy { it.fecha }

        val fechaAncla = if (pagosJugador.isNotEmpty()) {
            pagosJugador.first().fecha
        } else {
            jugador.fechaInicio
        }

        val nroPagos = pagosJugador.size
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

        PagoCalculado(
            jugador = jugador,
            fechaAncla = fechaAncla,
            proximaFecha = proximaFecha,
            diasRestantes = diasRestantes,
            estado = estado,
            valorMensual = jugador.valorMensual
        )
    }

    override suspend fun obtenerRecordatoriosParaCoach(coachId: String): List<RecordatorioItem> =
        withContext(Dispatchers.IO) {
            val jugadoresCoach = jugadores.value.filter { it.coachId == coachId && it.estado == EstadoJugador.ACTIVO }
            val config = configs.value[coachId] ?: ConfigUsuario(coachId)
            val diasAnticipacion = config.diasAnticipacion

            val items = mutableListOf<RecordatorioItem>()

            for (jugador in jugadoresCoach) {
                val calc = calcularEstadoPago(jugador.id) ?: continue
                val dias = calc.diasRestantes
                val valorFormateado = String.format("%.2f", jugador.valorMensual)

                if (dias < 0) {
                    items.add(
                        RecordatorioItem(
                            id = "rec_venc_${jugador.id}",
                            jugadorId = jugador.id,
                            jugadorNombre = jugador.nombre,
                            fechaVencimiento = calc.proximaFecha,
                            mensaje = "Pago vencido hace ${-dias} día(s) (venció el ${calc.proximaFecha}) • Cuota: $$valorFormateado",
                            esMismoDia = false,
                            esVencido = true,
                            valorMensual = jugador.valorMensual
                        )
                    )
                } else if (dias == 0L) {
                    items.add(
                        RecordatorioItem(
                            id = "rec_hoy_${jugador.id}",
                            jugadorId = jugador.id,
                            jugadorNombre = jugador.nombre,
                            fechaVencimiento = calc.proximaFecha,
                            mensaje = "El pago vence hoy (${calc.proximaFecha}) • Cuota: $$valorFormateado",
                            esMismoDia = true,
                            esVencido = false,
                            valorMensual = jugador.valorMensual
                        )
                    )
                } else if (dias in 1..diasAnticipacion.toLong()) {
                    items.add(
                        RecordatorioItem(
                            id = "rec_prox_${jugador.id}",
                            jugadorId = jugador.id,
                            jugadorNombre = jugador.nombre,
                            fechaVencimiento = calc.proximaFecha,
                            mensaje = "El pago vence en $dias día(s) (${calc.proximaFecha}) • Cuota: $$valorFormateado",
                            esMismoDia = false,
                            esVencido = false,
                            valorMensual = jugador.valorMensual
                        )
                    )
                }
            }

            items.sortedWith(compareBy({ !it.esVencido }, { it.fechaVencimiento }))
        }

    override suspend fun obtenerClasesDeHoyParaCoach(coachId: String): List<ClaseHoyItem> =
        withContext(Dispatchers.IO) {
            val hoy = LocalDate.now()
            val diaHoy = DiaSemana.fromIso(hoy.dayOfWeek.value)

            val horariosHoy = horarios.value.filter { it.coachId == coachId && it.diaSemana == diaHoy }
            val canchasMap = canchas.value.associateBy { it.id }
            val ciudadesMap = ciudades.value.associateBy { it.id }
            val jugadoresMap = jugadores.value.associateBy { it.id }

            val clases = mutableListOf<ClaseHoyItem>()
            for (horario in horariosHoy) {
                val asignados = asignaciones.value.filter { it.horarioId == horario.id }
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

            clases.sortedBy { it.horaInicio }
        }

    // --- GESTIÓN DE PAGOS (RF-11, RF-12, RF-13, RN-01, RN-10, RN-13) ---
    override suspend fun registrarPago(
        jugadorId: String,
        fecha: LocalDate,
        monto: Double,
        observacion: String?
    ): Result<Pago> = withContext(Dispatchers.IO) {
        if (monto <= 0) {
            return@withContext Result.failure(Exception("El monto debe ser mayor a 0"))
        }

        val jugador = jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return@withContext Result.failure(Exception("Jugador no encontrado"))

        val nuevoPago = Pago(
            id = "pago_${System.currentTimeMillis()}",
            jugadorId = jugadorId,
            fecha = fecha,
            monto = monto,
            observacion = observacion?.takeIf { it.isNotBlank() }
        )

        dataSource.addPago(nuevoPago)
        dataSource.setBannerMensaje("Pago registrado exitosamente para ${jugador.nombre}")
        Result.success(nuevoPago)
    }

    override suspend fun actualizarPago(
        pagoId: String,
        fecha: LocalDate,
        monto: Double,
        observacion: String?
    ): Result<Pago> = withContext(Dispatchers.IO) {
        if (monto <= 0) {
            return@withContext Result.failure(Exception("El monto debe ser mayor a 0"))
        }
        val pagoActual = pagos.value.firstOrNull { it.id == pagoId }
            ?: return@withContext Result.failure(Exception("Pago no encontrado"))

        val pagoActualizado = pagoActual.copy(
            fecha = fecha,
            monto = monto,
            observacion = observacion?.takeIf { it.isNotBlank() }
        )
        dataSource.updatePago(pagoActualizado)
        Result.success(pagoActualizado)
    }

    override suspend fun eliminarPago(pagoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existe = pagos.value.any { it.id == pagoId }
        if (!existe) {
            return@withContext Result.failure(Exception("Pago no encontrado"))
        }
        dataSource.removePago(pagoId)
        Result.success(Unit)
    }

    override suspend fun obtenerPagosPorJugador(jugadorId: String): List<Pago> =
        withContext(Dispatchers.IO) {
            pagos.value.filter { it.jugadorId == jugadorId }.sortedByDescending { it.fecha }
        }

    // --- GESTIÓN DE JUGADORES CON VALOR INDIVIDUAL Y CORREO (RF-06, RF-08, RF-09, RN-14, RN-20) ---
    override suspend fun guardarJugador(
        id: String?,
        nombre: String,
        telefono: String,
        correo: String,
        fechaInicio: LocalDate,
        valorMensual: Double,
        coachId: String,
        horariosIds: List<String>
    ): Result<Jugador> = withContext(Dispatchers.IO) {
        if (nombre.isBlank()) return@withContext Result.failure(Exception("El nombre es obligatorio"))
        if (telefono.isBlank()) return@withContext Result.failure(Exception("El teléfono es obligatorio"))
        if (correo.isBlank() || !correo.contains("@")) {
            return@withContext Result.failure(Exception("El correo electrónico es obligatorio y debe tener un formato válido"))
        }
        if (valorMensual <= 0) {
            return@withContext Result.failure(Exception("El valor mensual individual debe ser mayor a 0"))
        }

        val jugadorId = id ?: "jug_${System.currentTimeMillis()}"
        val jugador = if (id != null) {
            val existente = jugadores.value.firstOrNull { it.id == id }
                ?: return@withContext Result.failure(Exception("Jugador no encontrado"))
            existente.copy(
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                correo = correo.trim().lowercase(),
                fechaInicio = fechaInicio,
                valorMensual = valorMensual,
                coachId = coachId
            )
        } else {
            Jugador(
                id = jugadorId,
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                correo = correo.trim().lowercase(),
                fechaInicio = fechaInicio,
                valorMensual = valorMensual,
                coachId = coachId,
                estado = EstadoJugador.ACTIVO
            )
        }

        if (id != null) {
            dataSource.updateJugador(jugador)
        } else {
            dataSource.addJugador(jugador)
        }

        val nuevasAsignaciones = horariosIds.map { horarioId ->
            Asignacion(
                id = "asig_${System.currentTimeMillis()}_${horarioId}",
                jugadorId = jugadorId,
                horarioId = horarioId
            )
        }
        dataSource.replaceAsignacionesForJugador(jugadorId, nuevasAsignaciones)

        Result.success(jugador)
    }

    override suspend fun eliminarJugador(jugadorId: String): Result<String> = withContext(Dispatchers.IO) {
        val jugador = jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return@withContext Result.failure(Exception("Jugador no encontrado"))

        val tienePagos = pagos.value.any { it.jugadorId == jugadorId }
        if (tienePagos) {
            dataSource.updateJugador(jugador.copy(estado = EstadoJugador.INACTIVO))
            dataSource.removeAsignacionesPorJugador(jugadorId)
            Result.success("El jugador tiene pagos registrados, por lo que fue marcado como INACTIVO para preservar su historial.")
        } else {
            dataSource.removeJugador(jugadorId)
            dataSource.removeAsignacionesPorJugador(jugadorId)
            Result.success("Jugador eliminado definitivamente.")
        }
    }

    // --- GESTIÓN DE HORARIOS (RF-07, RF-18, RN-16, RN-17) ---
    override suspend fun guardarHorario(
        id: String?,
        coachId: String,
        canchaId: String,
        diaSemana: DiaSemana,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Result<Horario> = withContext(Dispatchers.IO) {
        if (!horaFin.isAfter(horaInicio)) {
            return@withContext Result.failure(Exception("La hora de fin debe ser posterior a la hora de inicio"))
        }

        val horariosMismoDia = horarios.value.filter {
            it.coachId == coachId && it.diaSemana == diaSemana && it.id != id
        }
        val haySolapamiento = horariosMismoDia.any { h ->
            !(horaFin <= h.horaInicio || horaInicio >= h.horaFin)
        }
        if (haySolapamiento) {
            return@withContext Result.failure(Exception("El coach ya tiene una clase programada en ese rango de horas este día"))
        }

        val horario = if (id != null) {
            val existente = horarios.value.firstOrNull { it.id == id }
                ?: return@withContext Result.failure(Exception("Horario no encontrado"))
            existente.copy(coachId = coachId, canchaId = canchaId, diaSemana = diaSemana, horaInicio = horaInicio, horaFin = horaFin)
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

        if (id != null) {
            dataSource.updateHorario(horario)
        } else {
            dataSource.addHorario(horario)
        }

        Result.success(horario)
    }

    override suspend fun eliminarHorario(horarioId: String): Result<Unit> = withContext(Dispatchers.IO) {
        dataSource.removeHorario(horarioId)
        dataSource.removeAsignacionesPorHorario(horarioId)
        Result.success(Unit)
    }

    // --- ADMIN: CIUDADES Y CANCHAS (A-03, A-04, RN-19) ---
    override suspend fun guardarCiudad(id: String?, nombre: String): Result<Ciudad> = withContext(Dispatchers.IO) {
        if (nombre.isBlank()) return@withContext Result.failure(Exception("El nombre de la ciudad es obligatorio"))
        val ciudad = if (id != null) {
            val existente = ciudades.value.firstOrNull { it.id == id }
                ?: return@withContext Result.failure(Exception("Ciudad no encontrada"))
            existente.copy(nombre = nombre.trim())
        } else {
            Ciudad(id = "ciu_${System.currentTimeMillis()}", nombre = nombre.trim())
        }

        if (id != null) {
            dataSource.updateCiudad(ciudad)
        } else {
            dataSource.addCiudad(ciudad)
        }
        Result.success(ciudad)
    }

    override suspend fun eliminarCiudad(ciudadId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tieneCanchas = canchas.value.any { it.ciudadId == ciudadId }
        if (tieneCanchas) {
            return@withContext Result.failure(Exception("No se puede eliminar la ciudad porque tiene canchas asociadas"))
        }
        dataSource.removeCiudad(ciudadId)
        Result.success(Unit)
    }

    override suspend fun guardarCancha(
        id: String?,
        nombre: String,
        referencia: String?,
        ciudadId: String
    ): Result<Cancha> = withContext(Dispatchers.IO) {
        if (nombre.isBlank()) return@withContext Result.failure(Exception("El nombre de la cancha es obligatorio"))
        if (ciudadId.isBlank()) return@withContext Result.failure(Exception("Debe seleccionar una ciudad"))

        val cancha = if (id != null) {
            val existente = canchas.value.firstOrNull { it.id == id }
                ?: return@withContext Result.failure(Exception("Cancha no encontrada"))
            existente.copy(nombre = nombre.trim(), referencia = referencia?.trim(), ciudadId = ciudadId)
        } else {
            Cancha(
                id = "can_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                referencia = referencia?.trim(),
                ciudadId = ciudadId
            )
        }

        if (id != null) {
            dataSource.updateCancha(cancha)
        } else {
            dataSource.addCancha(cancha)
        }
        Result.success(cancha)
    }

    override suspend fun eliminarCancha(canchaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tieneHorarios = horarios.value.any { it.canchaId == canchaId }
        if (tieneHorarios) {
            return@withContext Result.failure(Exception("No se puede eliminar la cancha porque tiene horarios programados"))
        }
        dataSource.removeCancha(canchaId)
        Result.success(Unit)
    }

    // --- ADMIN: GESTIÓN DE COACHES (A-02, RN-21) ---
    override suspend fun guardarCoachAdmin(
        id: String?,
        nombre: String,
        correo: String,
        contrasena: String,
        estado: EstadoUsuario
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        if (nombre.isBlank() || correo.isBlank()) {
            return@withContext Result.failure(Exception("El nombre y correo son obligatorios"))
        }

        if (id == null) {
            if (usuarios.value.any { it.correo.trim().equals(correo.trim(), ignoreCase = true) }) {
                return@withContext Result.failure(Exception("El correo ya está registrado"))
            }
            if (contrasena.length < 6) {
                return@withContext Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
            }
            val nuevo = Usuario(
                id = "user_coach_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                contrasena = contrasena,
                rol = Rol.COACH,
                estado = estado
            )
            dataSource.addUsuario(nuevo)
            dataSource.updateConfig(ConfigUsuario(nuevo.id))
            Result.success(nuevo)
        } else {
            val existente = usuarios.value.firstOrNull { it.id == id }
                ?: return@withContext Result.failure(Exception("Usuario no encontrado"))
            val actualizado = existente.copy(
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                contrasena = if (contrasena.isNotBlank()) contrasena else existente.contrasena,
                estado = estado
            )
            dataSource.updateUsuario(actualizado)
            Result.success(actualizado)
        }
    }

    override suspend fun reasignarCoachAJugador(
        jugadorId: String,
        nuevoCoachId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val jugador = jugadores.value.firstOrNull { it.id == jugadorId }
            ?: return@withContext Result.failure(Exception("Jugador no encontrado"))
        val coach = usuarios.value.firstOrNull { it.id == nuevoCoachId && it.rol == Rol.COACH }
            ?: return@withContext Result.failure(Exception("Coach no encontrado"))

        dataSource.updateJugador(jugador.copy(coachId = nuevoCoachId))
        Result.success(Unit)
    }

    override suspend fun obtenerResumenAdmin(): AdminResumen = withContext(Dispatchers.IO) {
        val totalCoaches = usuarios.value.count { it.rol == Rol.COACH }
        val coachesActivos = usuarios.value.count { it.rol == Rol.COACH && it.estado == EstadoUsuario.ACTIVO }
        val jugadoresActivos = jugadores.value.count { it.estado == EstadoJugador.ACTIVO }

        val pagosProximos = jugadores.value
            .filter { it.estado == EstadoJugador.ACTIVO }
            .mapNotNull { calcularEstadoPago(it.id) }
            .count { it.estado == EstadoPago.PROXIMO || it.estado == EstadoPago.VENCIDO }

        AdminResumen(
            totalCoaches = totalCoaches,
            coachesActivos = coachesActivos,
            jugadoresActivos = jugadoresActivos,
            pagosProximosGlobales = pagosProximos
        )
    }

    override suspend fun obtenerResumenCoachesGestion(): List<ResumenCoachGestion> =
        withContext(Dispatchers.IO) {
            val coaches = usuarios.value.filter { it.rol == Rol.COACH }
            val canchasMap = canchas.value.associateBy { it.id }
            coaches.map { coach ->
                val jug = jugadores.value.filter { it.coachId == coach.id && it.estado == EstadoJugador.ACTIVO }
                val hor = horarios.value.filter { it.coachId == coach.id }
                val canchasList = hor.mapNotNull { canchasMap[it.canchaId] }.distinctBy { it.id }
                ResumenCoachGestion(
                    coach = coach,
                    jugadoresDesignados = jug,
                    horariosDesignados = hor,
                    canchasDesignadas = canchasList
                )
            }
        }
}
