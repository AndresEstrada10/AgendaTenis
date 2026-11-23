package com.example.agendatenis.repository

import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.di.DefaultAppContainer

data class AdminResumen(
    val totalCoaches: Int,
    val coachesActivos: Int,
    val jugadoresActivos: Int,
    val pagosProximosGlobales: Int
)

/**
 * Fachada Singleton compatible con versiones previas y pruebas unitarias de AgendaTenis.
 * Delega en IAgendaTenisRepository del contenedor de inyección de dependencias (AppContainer),
 * asegurando un único punto de verdad (Single Source of Truth - SSOT).
 */
class AgendaTenisRepository private constructor(
    private val delegate: IAgendaTenisRepository
) : IAgendaTenisRepository by delegate {

    companion object {
        @Volatile
        private var instance: AgendaTenisRepository? = null

        fun getInstance(): AgendaTenisRepository {
            return instance ?: synchronized(this) {
                instance ?: AgendaTenisRepository(DefaultAppContainer.getInstance().repository).also { instance = it }
            }
        }
    }
}
