package com.example.agendatenis

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.agendatenis.data.repository.DataStoreThemeRepository
import com.example.agendatenis.data.repository.ThemeRepository
import com.example.agendatenis.model.TemaApp
import com.example.agendatenis.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun sistemaPorDefectoYPreferenciaConservadaAlRecrearElAlmacenamiento() = runTest {
        val file = folder.newFolder().resolve("apariencia.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var store = PreferenceDataStoreFactory.create(scope = scope) { file }
        var repository = DataStoreThemeRepository(store)
        assertNull(repository.tema.first())
        repository.guardarTema(TemaApp.OSCURO)
        assertEquals(TemaApp.OSCURO, repository.tema.first())
        scope.coroutineContext[Job]!!.cancelAndJoin()

        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = PreferenceDataStoreFactory.create(scope = scope) { file }
        repository = DataStoreThemeRepository(store)
        assertEquals(TemaApp.OSCURO, repository.tema.first())
        repository.guardarTema(TemaApp.CLARO)
        assertEquals(TemaApp.CLARO, repository.tema.first())
        repository.guardarTema(null)
        assertNull(repository.tema.first())
        scope.coroutineContext[Job]!!.cancelAndJoin()

        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = PreferenceDataStoreFactory.create(scope = scope) { file }
        repository = DataStoreThemeRepository(store)
        assertNull(repository.tema.first())
        scope.coroutineContext[Job]!!.cancelAndJoin()
    }

    @Test
    fun viewModelPublicaLaMismaPreferenciaYNoLaAsociaAUnaSesion() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = object : ThemeRepository {
                override val tema = MutableStateFlow<TemaApp?>(null)
                override suspend fun guardarTema(tema: TemaApp?) { this.tema.value = tema }
            }
            val viewModel = ThemeViewModel(repository)
            assertTrue(viewModel.uiState.value.cargado)
            assertNull(viewModel.uiState.value.tema)
            viewModel.cambiarTema(TemaApp.OSCURO)
            assertEquals(TemaApp.OSCURO, viewModel.uiState.value.tema)
            val siguienteViewModel = ThemeViewModel(repository)
            assertEquals(TemaApp.OSCURO, siguienteViewModel.uiState.value.tema)
            siguienteViewModel.cambiarTema(TemaApp.CLARO)
            assertEquals(TemaApp.CLARO, viewModel.uiState.value.tema)
            viewModel.cambiarTema(null)
            assertNull(siguienteViewModel.uiState.value.tema)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
