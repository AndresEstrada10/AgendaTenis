package com.example.agendatenis

import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.di.DefaultAppContainer
import com.example.agendatenis.di.ViewModelFactory
import com.example.agendatenis.model.Rol
import com.example.agendatenis.model.Usuario
import com.example.agendatenis.ui.viewmodel.admin.AdminCoachesViewModel
import com.example.agendatenis.ui.viewmodel.admin.AdminHomeViewModel
import com.example.agendatenis.ui.viewmodel.auth.AuthViewModel
import com.example.agendatenis.ui.viewmodel.coach.CoachHomeViewModel
import com.example.agendatenis.ui.viewmodel.coach.FormJugadorViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelArchitectureTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: IAgendaTenisRepository
    private lateinit var factory: ViewModelFactory

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = DefaultAppContainer.getInstance().repository
        factory = ViewModelFactory(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelFactoryCreatesAllViewModels() {
        val authVm = factory.create(AuthViewModel::class.java)
        assertNotNull(authVm)

        val coachHomeVm = factory.create(CoachHomeViewModel::class.java)
        assertNotNull(coachHomeVm)

        val adminHomeVm = factory.create(AdminHomeViewModel::class.java)
        assertNotNull(adminHomeVm)

        val adminCoachesVm = factory.create(AdminCoachesViewModel::class.java)
        assertNotNull(adminCoachesVm)
    }

    @Test
    fun testAuthViewModelLoginFlow(): Unit = runBlocking {
        val authVm = factory.create(AuthViewModel::class.java)
        authVm.onCorreoChange("jorge@agendatenis.com")
        authVm.onContrasenaChange("coach123")

        assertEquals("jorge@agendatenis.com", authVm.loginUiState.value.correo)
        assertEquals("coach123", authVm.loginUiState.value.contrasena)

        val completer = CompletableDeferred<Usuario>()
        authVm.iniciarSesion(
            onSuccess = { user ->
                completer.complete(user)
            }
        )
        val user = withTimeout(3000) { completer.await() }
        assertEquals("Jorge Sanmartin", user.nombre)
        assertEquals(Rol.COACH, user.rol)
    }

    @Test
    fun testAdminCoachesViewModelQuery() = runTest {
        val vm = factory.create(AdminCoachesViewModel::class.java)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect()
        }
        vm.onSearchQueryChange("Jorge")
        testScheduler.advanceUntilIdle()
        assertEquals("Jorge", vm.uiState.value.searchQuery)
    }

    @Test
    fun testFormJugadorViewModelValidation(): Unit = runBlocking {
        val formVm = factory.create(FormJugadorViewModel::class.java)
        formVm.init("user_coach_1", null)

        formVm.onNombreChange("")
        formVm.guardar {}
        assertNotNull(formVm.uiState.value.errorMessage)
        assertTrue(formVm.uiState.value.errorMessage!!.contains("nombre"))

        formVm.onNombreChange("Nuevo Tenista")
        formVm.onTelefonoChange("0999999999")
        formVm.onCorreoChange("tenista@ejemplo.com")
        formVm.onValorMensualChange("80.0")

        val completer = CompletableDeferred<Unit>()
        formVm.guardar {
            completer.complete(Unit)
        }
        withTimeout(3000) { completer.await() }
    }
}
