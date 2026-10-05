package com.example.agendatenis.model

import java.time.LocalDate
import java.time.LocalTime

enum class Rol {
    ADMIN,
    COACH
}

enum class EstadoUsuario {
    ACTIVO,
    INACTIVO
}

enum class EstadoJugador {
    ACTIVO,
    INACTIVO
}

enum class TemaApp {
    CLARO,
    OSCURO
}

enum class DiaSemana(val abrev: String, val nombreCompleto: String, val diaIso: Int) {
    LUNES("L", "Lunes", 1),
    MARTES("M", "Martes", 2),
    MIERCOLES("X", "Miércoles", 3),
    JUEVES("J", "Jueves", 4),
    VIERNES("V", "Viernes", 5),
    SABADO("S", "Sábado", 6),
    DOMINGO("D", "Domingo", 7);

    companion object {
        fun fromIso(dayOfWeek: Int): DiaSemana {
            return entries.firstOrNull { it.diaIso == dayOfWeek } ?: LUNES
        }
    }
}

enum class EstadoPago {
    AL_DIA,
    PROXIMO,
    VENCIDO
}

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: Rol,
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO
)

data class Ciudad(
    val id: String,
    val nombre: String
)

data class Cancha(
    val id: String,
    val nombre: String,
    val referencia: String? = null,
    val ciudadId: String
)

data class Plan(
    val id: String,
    val nombre: String,
    val clasesPorSemana: Int,
    val valor: Double,
    val coachId: String
)

data class Jugador(
    val id: String,
    val nombre: String,
    val telefono: String,
    val fechaInicio: LocalDate,
    val planId: String,
    val coachId: String,
    val estado: EstadoJugador = EstadoJugador.ACTIVO
)

data class Horario(
    val id: String,
    val coachId: String,
    val canchaId: String,
    val diaSemana: DiaSemana,
    val horaInicio: LocalTime,
    val horaFin: LocalTime
)

data class Asignacion(
    val id: String,
    val jugadorId: String,
    val horarioId: String
)

data class Pago(
    val id: String,
    val jugadorId: String,
    val fecha: LocalDate,
    val monto: Double,
    val observacion: String? = null
)

data class ConfigUsuario(
    val usuarioId: String,
    val tema: TemaApp = TemaApp.CLARO,
    val recordarSesion: Boolean = true,
    val diasAnticipacion: Int = 3
)

// Computed representations for UI
data class PagoCalculado(
    val jugador: Jugador,
    val plan: Plan?,
    val fechaAncla: LocalDate,
    val proximaFecha: LocalDate,
    val diasRestantes: Long,
    val estado: EstadoPago
)

data class ClaseHoyItem(
    val horarioId: String,
    val jugadorId: String,
    val jugadorNombre: String,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val canchaNombre: String,
    val ciudadNombre: String
)

data class RecordatorioItem(
    val id: String,
    val jugadorId: String,
    val jugadorNombre: String,
    val fechaVencimiento: LocalDate,
    val mensaje: String,
    val esMismoDia: Boolean,
    val esVencido: Boolean
)
