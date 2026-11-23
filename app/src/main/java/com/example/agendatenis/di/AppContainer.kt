package com.example.agendatenis.di

import android.content.Context
import com.example.agendatenis.data.repository.DataStoreThemeRepository
import com.example.agendatenis.data.repository.ThemeRepository
import com.example.agendatenis.data.repository.themeDataStore
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
    fun getThemeRepository(context: Context): ThemeRepository
}

class DefaultAppContainer : AppContainer {
    private var themeRepository: ThemeRepository? = null

    @Synchronized
    override fun getThemeRepository(context: Context): ThemeRepository =
        themeRepository ?: DataStoreThemeRepository(context.applicationContext.themeDataStore)
            .also { themeRepository = it }


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
