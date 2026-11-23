package com.example.agendatenis.di

import com.example.agendatenis.data.datasource.AgendaTenisDataSource
import com.example.agendatenis.data.datasource.InMemoryAgendaTenisDataSource
import com.example.agendatenis.data.repository.AgendaTenisRepositoryImpl
import com.example.agendatenis.data.repository.IAgendaTenisRepository

/**
 * Contenedor de dependencias para la aplicación AgendaTenis (Manual DI).
 * Gestiona el ciclo de vida de los componentes DataSource y Repository (SSOT).
 */
interface AppContainer {
    val dataSource: AgendaTenisDataSource
    val repository: IAgendaTenisRepository
}

class DefaultAppContainer : AppContainer {

    override val dataSource: AgendaTenisDataSource by lazy {
        InMemoryAgendaTenisDataSource()
    }

    override val repository: IAgendaTenisRepository by lazy {
        AgendaTenisRepositoryImpl(dataSource)
    }

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun getInstance(): AppContainer {
            return instance ?: synchronized(this) {
                instance ?: DefaultAppContainer().also { instance = it }
            }
        }
    }
}
