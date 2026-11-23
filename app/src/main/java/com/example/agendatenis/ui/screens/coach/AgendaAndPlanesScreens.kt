package com.example.agendatenis.ui.screens.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendatenis.model.*
import com.example.agendatenis.ui.components.ConfirmacionDialog
import com.example.agendatenis.ui.theme.TennisGreenLight
import com.example.agendatenis.ui.theme.TennisRedDestructive
import com.example.agendatenis.ui.viewmodel.coach.AgendaCoachUiState
import com.example.agendatenis.ui.viewmodel.coach.CanchasCoachUiState
import com.example.agendatenis.ui.viewmodel.coach.FormHorarioUiState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaCoachScreen(
    uiState: AgendaCoachUiState,
    coachId: String,
    onCoachFiltroChange: (String) -> Unit,
    onSeleccionarHorarioParaEliminar: (Horario?) -> Unit,
    onConfirmarEliminarHorario: () -> Unit,
    onNavigateToCrearHorario: () -> Unit,
    onNavigateToEditarHorario: (String) -> Unit,
    onNavigateToVerCanchas: () -> Unit,
    onNavigateToGestionCoaches: () -> Unit = {}
) {
    if (uiState.horarioParaEliminar != null) {
        val h = uiState.horarioParaEliminar
        ConfirmacionDialog(
            titulo = "¿Eliminar Horario de Clase?",
            mensaje = "¿Deseas eliminar este bloque de ${h.diaSemana.nombreCompleto} (${h.horaInicio} - ${h.horaFin})? Los jugadores asignados dejarán de tener esta clase programada.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = onConfirmarEliminarHorario,
            onCancelar = { onSeleccionarHorarioParaEliminar(null) }
        )
    }

    val coachesMap = remember(uiState.coaches) { uiState.coaches.associateBy { it.id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agenda Semanal", fontWeight = FontWeight.Bold)
                        if (uiState.isCoachPrincipal) {
                            Text(
                                text = "Supervisión Coach Principal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isCoachPrincipal) {
                        IconButton(onClick = onNavigateToGestionCoaches) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = "Gestionar Coaches")
                        }
                    }
                    TextButton(onClick = onNavigateToVerCanchas) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Canchas")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrearHorario,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Horario")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filtro por coach en caso de Coach Principal (Jorge Sanmartin)
            if (uiState.isCoachPrincipal) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.coaches.forEach { c ->
                        FilterChip(
                            selected = uiState.coachSeleccionadoId == c.id,
                            onClick = { onCoachFiltroChange(c.id) },
                            label = { Text(if (c.id == coachId) "Mis Horarios" else c.nombre.split(" ").first()) }
                        )
                    }
                }
            }

            val todosHorarios = uiState.horariosAgrupados.values.flatten()
            if (todosHorarios.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay horarios registrados. Presiona + para agregar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    items(todosHorarios) { horario ->
                        val cancha = uiState.canchasMap[horario.canchaId]
                        val ciudad = cancha?.let { uiState.ciudadesMap[it.ciudadId] }
                        val jugadores = uiState.jugadoresPorHorario[horario.id] ?: emptyList()
                        val coachDueño = coachesMap[horario.coachId]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEditarHorario(horario.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = TennisGreenLight.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = horario.diaSemana.nombreCompleto,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TennisGreenLight
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${horario.horaInicio} - ${horario.horaFin}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onSeleccionarHorarioParaEliminar(horario) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar Horario",
                                            tint = TennisRedDestructive,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${cancha?.nombre ?: "Cancha"} (${ciudad?.nombre ?: ""})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (uiState.isCoachPrincipal && coachDueño != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Coach: ${coachDueño.nombre}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Jugadores inscritos en este horario
                                Text(
                                    text = if (jugadores.isEmpty()) "Sin alumnos asignados" else "Jugadores (${jugadores.size}):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (jugadores.isNotEmpty()) {
                                    val nombresJugadores = jugadores.map { it.nombre }
                                    Text(
                                        text = nombresJugadores.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormHorarioScreen(
    uiState: FormHorarioUiState,
    isCoachPrincipal: Boolean,
    coachId: String,
    onCoachIdChange: (String) -> Unit,
    onCanchaIdChange: (String) -> Unit,
    onDiaSemanaChange: (DiaSemana) -> Unit,
    onHoraInicioChange: (LocalTime) -> Unit,
    onHoraFinChange: (LocalTime) -> Unit,
    onGuardarClick: () -> Unit,
    onBack: () -> Unit
) {
    var horaInicioStr by remember(uiState.horaInicio) { mutableStateOf(uiState.horaInicio.toString()) }
    var horaFinStr by remember(uiState.horaFin) { mutableStateOf(uiState.horaFin.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (!uiState.esEdicion) "Nuevo Horario" else "Editar Horario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Designación de Coach por parte del Coach Principal (Jorge Sanmartin)
            if (isCoachPrincipal) {
                Text("Designar Horario a Coach *", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                uiState.coaches.forEach { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCoachIdChange(c.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.coachId == c.id,
                            onClick = { onCoachIdChange(c.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${c.nombre} ${if (c.id == coachId) "(Tú - Coach Principal)" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                HorizontalDivider()
            }

            Text("Día de la semana *", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DiaSemana.entries.forEach { dia ->
                    val isSelected = uiState.diaSemana == dia
                    FilledTonalButton(
                        onClick = { onDiaSemanaChange(dia) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected) TennisGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(dia.abrev, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Cancha Designada *", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            if (uiState.canchas.isEmpty()) {
                Text("No hay canchas registradas en el sistema por el Administrador.", color = MaterialTheme.colorScheme.error)
            } else {
                uiState.canchas.forEach { c ->
                    val ciudad = uiState.ciudadesMap[c.ciudadId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCanchaIdChange(c.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.canchaId == c.id,
                            onClick = { onCanchaIdChange(c.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${c.nombre} (${ciudad?.nombre ?: ""})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedTextField(
                value = horaInicioStr,
                onValueChange = {
                    horaInicioStr = it
                    try {
                        val parsed = LocalTime.parse(it.trim())
                        onHoraInicioChange(parsed)
                    } catch (_: Exception) {}
                },
                label = { Text("Hora de Inicio (HH:MM) *") },
                placeholder = { Text("08:00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = horaFinStr,
                onValueChange = {
                    horaFinStr = it
                    try {
                        val parsed = LocalTime.parse(it.trim())
                        onHoraFinChange(parsed)
                    } catch (_: Exception) {}
                },
                label = { Text("Hora de Fin (HH:MM) *") },
                placeholder = { Text("09:30") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    try {
                        val parsedInicio = LocalTime.parse(horaInicioStr.trim())
                        val parsedFin = LocalTime.parse(horaFinStr.trim())
                        onHoraInicioChange(parsedInicio)
                        onHoraFinChange(parsedFin)
                        onGuardarClick()
                    } catch (e: Exception) {
                        onGuardarClick()
                    }
                },
                enabled = !uiState.isGuardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.isGuardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Horario", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanchasCoachScreen(
    uiState: CanchasCoachUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canchas Disponibles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            items(uiState.ciudadesConCanchas) { (ciudad, canchas) ->
                canchas.forEach { cancha ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cancha.nombre,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Ciudad: ${ciudad.nombre}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!cancha.referencia.isNullOrBlank()) {
                                    Text(
                                        text = "Ref: ${cancha.referencia}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
