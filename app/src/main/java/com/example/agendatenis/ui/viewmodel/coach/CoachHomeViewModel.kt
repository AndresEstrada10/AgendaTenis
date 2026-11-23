package com.example.agendatenis.ui.viewmodel.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendatenis.data.repository.IAgendaTenisRepository
import com.example.agendatenis.model.ClaseHoyItem
import com.example.agendatenis.model.RecordatorioItem
import com.example.agendatenis.model.Usuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CoachHomeUiState(
    val usuarioActual: Usuario? = null,
    val bannerMensaje: String? = null,
    val recordatorios: List<RecordatorioItem> = emptyList(),
    val clasesHoy: List<ClaseHoyItem> = emptyList(),
    val mostrarDialogRecordatorios: Boolean = false,
    val isLoading: Boolean = false
)

class CoachHomeViewModel(
    private val repository: IAgendaTenisRepository
) : ViewModel() {

    private val _dialogRecordatoriosVisible = MutableStateFlow(false)

    val uiState: StateFlow<CoachHomeUiState> = combine(
        repository.usuarioActual,
        repository.bannerMensaje,
        repository.pagos,
        repository.jugadores,
        _dialogRecordatoriosVisible
    ) { user, banner, _, _, dialogVisible ->
        val coachId = user?.id ?: "user_coach_1"
        val recordatorios = repository.obtenerRecordatoriosParaCoach(coachId)
        val clasesHoy = repository.obtenerClasesDeHoyParaCoach(coachId)

        CoachHomeUiState(
            usuarioActual = user,
            bannerMensaje = banner,
            recordatorios = recordatorios,
            clasesHoy = clasesHoy,
            mostrarDialogRecordatorios = dialogVisible,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CoachHomeUiState(isLoading = true)
    )

    fun setMostrarDialogRecordatorios(visible: Boolean) {
        _dialogRecordatoriosVisible.value = visible
    }

    fun dismissBanner() {
        viewModelScope.launch {
            repository.setBannerMensaje(null)
        }
    }
}
