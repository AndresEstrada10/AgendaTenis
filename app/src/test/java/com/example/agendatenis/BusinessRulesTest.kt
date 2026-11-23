package com.example.agendatenis

import com.example.agendatenis.model.*
import com.example.agendatenis.repository.AgendaTenisRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BusinessRulesTest {

    private lateinit var repository: AgendaTenisRepository

    @Before
    fun setUp() {
        repository = AgendaTenisRepository.getInstance()
    }

    @Test
    fun testNombresPrincipalesConfigurados() {
        val admin = repository.usuarios.value.first { it.rol == Rol.ADMIN }
        assertEquals("Andres Estrada", admin.nombre)

        val coach1 = repository.usuarios.value.first { it.id == "user_coach_1" }
        assertEquals("Jorge Sanmartin", coach1.nombre)

        val jug1 = repository.jugadores.value.first { it.id == "jug_1" }
        assertEquals("Angel Veliz", jug1.nombre)
        assertEquals("angel.veliz@ejemplo.com", jug1.correo)
        assertEquals(75.0, jug1.valorMensual, 0.001)
    }

    @Test
    fun testCoachPrincipalPermisosYGestion() {
        val coachJorge = repository.usuarios.value.first { it.id == "user_coach_1" }
        assertTrue(coachJorge.esCoachPrincipal)

        val coachMariana = repository.usuarios.value.first { it.id == "user_coach_2" }
        assertFalse(coachMariana.esCoachPrincipal)

        // Resumen de gestión para Coach Principal (coaches, jugadores, horarios y canchas designadas)
        val resumen = repository.obtenerResumenCoachesGestion()
        assertTrue(resumen.isNotEmpty())
        val resumenJorge = resumen.first { it.coach.id == "user_coach_1" }
        assertTrue(resumenJorge.jugadoresDesignados.isNotEmpty())
        assertTrue(resumenJorge.horariosDesignados.isNotEmpty())
        assertTrue(resumenJorge.canchasDesignadas.isNotEmpty())

        // Reasignación de jugador a Mariana por parte del Coach Principal
        val reasigRes = repository.reasignarCoachAJugador("jug_1", "user_coach_2")
        assertTrue(reasigRes.isSuccess)
        val j1Actualizado = repository.jugadores.value.first { it.id == "jug_1" }
        assertEquals("user_coach_2", j1Actualizado.coachId)

        // Regresar a Jorge
        repository.reasignarCoachAJugador("jug_1", "user_coach_1")
    }

    @Test
    fun testRN03_RN04_calculoFechaAnclaYProximaFecha() {
        // Para jugador 1 (Angel Veliz, tiene 2 pagos), la proximaFecha debe ser fechaAncla + 2 meses
        val calcJ1 = repository.calcularEstadoPago("jug_1")
        assertNotNull(calcJ1)
        assertEquals(calcJ1!!.fechaAncla.plusMonths(2), calcJ1.proximaFecha)
        assertEquals(75.0, calcJ1.valorMensual, 0.001)

        // Para jugador 4 (nuevo, sin pagos), fechaAncla = fechaInicio y proximaFecha = fechaInicio + 0 meses
        val calcJ4 = repository.calcularEstadoPago("jug_4")
        assertNotNull(calcJ4)
        val j4 = repository.jugadores.value.first { it.id == "jug_4" }
        assertEquals(j4.fechaInicio, calcJ4!!.fechaAncla)
        assertEquals(j4.fechaInicio, calcJ4.proximaFecha)
        assertEquals(50.0, calcJ4.valorMensual, 0.001)
    }

    @Test
    fun testRN13_montoMayorACero() {
        val resultInvalido = repository.registrarPago(
            jugadorId = "jug_1",
            fecha = LocalDate.now(),
            monto = 0.0,
            observacion = null
        )
        assertTrue(resultInvalido.isFailure)

        val resultValido = repository.registrarPago(
            jugadorId = "jug_1",
            fecha = LocalDate.now(),
            monto = 75.0,
            observacion = "Pago de prueba"
        )
        assertTrue(resultValido.isSuccess)
    }

    @Test
    fun testRN16_horariosSolapadosMismoDiaMismoCoach() {
        val coachId = "user_coach_1"
        val canchaId = "can_1"
        val dia = DiaSemana.DOMINGO

        // Guardar primer horario
        val h1 = repository.guardarHorario(
            id = null,
            coachId = coachId,
            canchaId = canchaId,
            diaSemana = dia,
            horaInicio = LocalTime.of(10, 0),
            horaFin = LocalTime.of(11, 30)
        )
        assertTrue(h1.isSuccess)

        // Intentar guardar horario que se solapa (11:00 a 12:00)
        val hSolapado = repository.guardarHorario(
            id = null,
            coachId = coachId,
            canchaId = canchaId,
            diaSemana = dia,
            horaInicio = LocalTime.of(11, 0),
            horaFin = LocalTime.of(12, 0)
        )
        assertTrue(hSolapado.isFailure)
        assertTrue(hSolapado.exceptionOrNull()?.message?.contains("solapada") == true ||
                hSolapado.exceptionOrNull()?.message?.contains("clase programada") == true)
    }

    @Test
    fun testRN19_noEliminarCiudadConCanchas() {
        // ciu_1 tiene cancha 1 y 2
        val res = repository.eliminarCiudad("ciu_1")
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("canchas") == true)
    }

    @Test
    fun testRN20_eliminarJugadorConPagosPasaAInactivo() {
        // jug_1 tiene pagos registrados
        val res = repository.eliminarJugador("jug_1")
        assertTrue(res.isSuccess)
        val j1 = repository.jugadores.value.first { it.id == "jug_1" }
        assertEquals(EstadoJugador.INACTIVO, j1.estado)

        // Crear jugador sin pagos con correo y cuota individual
        val jNuevo = repository.guardarJugador(
            id = null,
            nombre = "Jugador Sin Pagos",
            telefono = "0900000000",
            correo = "sinpagos@ejemplo.com",
            fechaInicio = LocalDate.now(),
            valorMensual = 60.0,
            coachId = "user_coach_1",
            horariosIds = emptyList()
        ).getOrThrow()

        val resBorrado = repository.eliminarJugador(jNuevo.id)
        assertTrue(resBorrado.isSuccess)
        assertNull(repository.jugadores.value.firstOrNull { it.id == jNuevo.id })
    }

    @Test
    fun testRN21_coachDesactivadoNoIniciaSesion() {
        // Desactivar a coach2
        repository.guardarCoachAdmin(
            id = "user_coach_2",
            nombre = "Mariana Silva",
            correo = "mariana@agendatenis.com",
            contrasena = "coach123",
            estado = EstadoUsuario.INACTIVO
        )

        val loginRes = repository.iniciarSesion("mariana@agendatenis.com", "coach123")
        assertTrue(loginRes.isFailure)
        assertTrue(loginRes.exceptionOrNull()?.message?.contains("desactivada") == true)
    }

    @Test
    fun testRN22_correoUnicoYContrasena() {
        val resDuplicado = repository.registrarCoach(
            nombre = "Coach Duplicado",
            correo = "jorge@agendatenis.com",
            contrasena = "123456",
            confirmarContrasena = "123456"
        )
        assertTrue(resDuplicado.isFailure)

        val resPasswordCorta = repository.registrarCoach(
            nombre = "Coach Pass Corta",
            correo = "nuevo@agendatenis.com",
            contrasena = "123",
            confirmarContrasena = "123"
        )
        assertTrue(resPasswordCorta.isFailure)

        val resMismatch = repository.registrarCoach(
            nombre = "Coach Mismatch",
            correo = "nuevo@agendatenis.com",
            contrasena = "123456",
            confirmarContrasena = "654321"
        )
        assertTrue(resMismatch.isFailure)
    }

    @Test
    fun testValoresIndividualesPorJugadorYModificacionPorCoachYAdmin() {
        // Cada jugador tiene su propio valor mensual independiente
        val j1 = repository.jugadores.value.first { it.id == "jug_1" }
        val j2 = repository.jugadores.value.first { it.id == "jug_2" }
        val j3 = repository.jugadores.value.first { it.id == "jug_3" }
        assertEquals(75.0, j1.valorMensual, 0.001)
        assertEquals(50.0, j2.valorMensual, 0.001)
        assertEquals(120.0, j3.valorMensual, 0.001)

        // Coach modifica el valor individual de jug_2
        val updateCoach = repository.guardarJugador(
            id = j2.id,
            nombre = j2.nombre,
            telefono = j2.telefono,
            correo = j2.correo,
            fechaInicio = j2.fechaInicio,
            valorMensual = 65.0,
            coachId = j2.coachId,
            horariosIds = emptyList()
        )
        assertTrue(updateCoach.isSuccess)
        val j2ModCoach = repository.jugadores.value.first { it.id == j2.id }
        assertEquals(65.0, j2ModCoach.valorMensual, 0.001)

        // Admin modifica el valor individual de jug_2 y lo reasigna
        val updateAdmin = repository.guardarJugador(
            id = j2.id,
            nombre = j2.nombre,
            telefono = j2.telefono,
            correo = "valeria.nueva@ejemplo.com",
            fechaInicio = j2.fechaInicio,
            valorMensual = 95.0,
            coachId = "user_coach_2",
            horariosIds = emptyList()
        )
        assertTrue(updateAdmin.isSuccess)
        val j2ModAdmin = repository.jugadores.value.first { it.id == j2.id }
        assertEquals(95.0, j2ModAdmin.valorMensual, 0.001)
        assertEquals("valeria.nueva@ejemplo.com", j2ModAdmin.correo)
        assertEquals("user_coach_2", j2ModAdmin.coachId)
    }

    @Test
    fun testValidacionCorreoYValorMensualJugador() {
        // Correo sin formato válido
        val resEmailInvalido = repository.guardarJugador(
            id = null,
            nombre = "Carlos Perez",
            telefono = "0988776655",
            correo = "carlos_sin_arroba",
            fechaInicio = LocalDate.now(),
            valorMensual = 50.0,
            coachId = "user_coach_1",
            horariosIds = emptyList()
        )
        assertTrue(resEmailInvalido.isFailure)
        assertTrue(resEmailInvalido.exceptionOrNull()?.message?.contains("correo") == true)

        // Valor mensual menor o igual a cero
        val resValorInvalido = repository.guardarJugador(
            id = null,
            nombre = "Carlos Perez",
            telefono = "0988776655",
            correo = "carlos@ejemplo.com",
            fechaInicio = LocalDate.now(),
            valorMensual = 0.0,
            coachId = "user_coach_1",
            horariosIds = emptyList()
        )
        assertTrue(resValorInvalido.isFailure)
        assertTrue(resValorInvalido.exceptionOrNull()?.message?.contains("mayor a 0") == true)
    }

    @Test
    fun testRecordatoriosUtilizanValorIndividual() {
        val recs = repository.obtenerRecordatoriosParaCoach("user_coach_1")
        assertFalse(recs.isEmpty())
        for (rec in recs) {
            val jugador = repository.jugadores.value.first { it.id == rec.jugadorId }
            assertEquals(jugador.valorMensual, rec.valorMensual, 0.001)
            assertTrue(rec.mensaje.contains("Cuota: $") || rec.mensaje.contains(String.format("%.2f", jugador.valorMensual)))
        }
    }
}
