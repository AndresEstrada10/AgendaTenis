package com.example.agendatenis.ui.screens.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendatenis.model.*
import com.example.agendatenis.repository.AgendaTenisRepository
import com.example.agendatenis.ui.components.ConfirmacionDialog
import com.example.agendatenis.ui.theme.TennisGreenLight
import com.example.agendatenis.ui.theme.TennisRedDestructive
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaCoachScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    onNavigateToCrearHorario: () -> Unit,
    onNavigateToEditarHorario: (String) -> Unit,
    onNavigateToVerCanchas: () -> Unit,
    onNavigateToGestionCoaches: () -> Unit = {}
) {
    val usuarioActual by repository.usuarioActual.collectAsState()
    val isCoachPrincipal = usuarioActual?.esCoachPrincipal == true

    val horarios by repository.horarios.collectAsState()
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()
    val asignaciones by repository.asignaciones.collectAsState()
    val jugadores by repository.jugadores.collectAsState()
    val usuarios by repository.usuarios.collectAsState()

    val coaches = usuarios.filter { it.rol == Rol.COACH }
    val coachesMap = coaches.associateBy { it.id }

    var coachFiltroId by remember { mutableStateOf(if (isCoachPrincipal) null else coachId) }
    var horarioAEliminar by remember { mutableStateOf<Horario?>(null) }

    val horariosFiltrados = remember(horarios, coachFiltroId, isCoachPrincipal, coachId) {
        horarios.filter {
            if (isCoachPrincipal) {
                if (coachFiltroId == null) true else it.coachId == coachFiltroId
            } else {
                it.coachId == coachId
            }
        }.sortedWith(compareBy({ it.diaSemana.diaIso }, { it.horaInicio }))
    }

    val canchasMap = canchas.associateBy { it.id }
    val ciudadesMap = ciudades.associateBy { it.id }
    val jugadoresMap = jugadores.associateBy { it.id }

    if (horarioAEliminar != null) {
        ConfirmacionDialog(
            titulo = "¿Eliminar Horario de Clase?",
            mensaje = "¿Deseas eliminar este bloque de ${horarioAEliminar?.diaSemana?.nombreCompleto} (${horarioAEliminar?.horaInicio} - ${horarioAEliminar?.horaFin})? Los jugadores asignados dejarán de tener esta clase programada.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val hId = horarioAEliminar?.id
                horarioAEliminar = null
                if (hId != null) {
                    repository.eliminarHorario(hId)
                }
            },
            onCancelar = { horarioAEliminar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agenda Semanal", fontWeight = FontWeight.Bold)
                        if (isCoachPrincipal) {
                            Text(
                                text = "Supervisión Coach Principal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (isCoachPrincipal) {
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
            if (isCoachPrincipal) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = coachFiltroId == null,
                        onClick = { coachFiltroId = null },
                        label = { Text("Todos") }
                    )
                    coaches.forEach { c ->
                        FilterChip(
                            selected = coachFiltroId == c.id,
                            onClick = { coachFiltroId = if (coachFiltroId == c.id) null else c.id },
                            label = { Text(if (c.id == coachId) "Mis Horarios" else c.nombre.split(" ").first()) }
                        )
                    }
                }
            }

            if (horariosFiltrados.isEmpty()) {
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
                    items(horariosFiltrados) { horario ->
                        val cancha = canchasMap[horario.canchaId]
                        val ciudad = cancha?.let { ciudadesMap[it.ciudadId] }
                        val asignados = asignaciones.filter { it.horarioId == horario.id }
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
                                        onClick = { horarioAEliminar = horario },
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

                                if (isCoachPrincipal && coachDueño != null) {
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
                                    text = if (asignados.isEmpty()) "Sin alumnos asignados" else "Jugadores (${asignados.size}):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (asignados.isNotEmpty()) {
                                    val nombresJugadores = asignados.mapNotNull { jugadoresMap[it.jugadorId]?.nombre }
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
    repository: AgendaTenisRepository,
    coachId: String,
    horarioId: String?,
    onBack: () -> Unit
) {
    val usuarioActual by repository.usuarioActual.collectAsState()
    val isCoachPrincipal = usuarioActual?.esCoachPrincipal == true

    val horarios by repository.horarios.collectAsState()
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()
    val usuarios by repository.usuarios.collectAsState()
    val coaches = usuarios.filter { it.rol == Rol.COACH }

    val horarioExistente = remember(horarioId) {
        horarios.firstOrNull { it.id == horarioId }
    }

    var coachDesignadoId by remember { mutableStateOf(horarioExistente?.coachId ?: coachId) }
    var diaSeleccionado by remember { mutableStateOf(horarioExistente?.diaSemana ?: DiaSemana.LUNES) }
    var canchaSeleccionadaId by remember { mutableStateOf(horarioExistente?.canchaId ?: (canchas.firstOrNull()?.id ?: "")) }
    var horaInicioStr by remember { mutableStateOf(horarioExistente?.horaInicio?.toString() ?: "08:00") }
    var horaFinStr by remember { mutableStateOf(horarioExistente?.horaFin?.toString() ?: "09:30") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val ciudadesMap = ciudades.associateBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (horarioId == null) "Nuevo Horario" else "Editar Horario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            if (mensajeError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensajeError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Designación de Coach por parte del Coach Principal (Jorge Sanmartin)
            if (isCoachPrincipal) {
                Text("Designar Horario a Coach *", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                coaches.forEach { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { coachDesignadoId = c.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = coachDesignadoId == c.id,
                            onClick = { coachDesignadoId = c.id }
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
                    val isSelected = diaSeleccionado == dia
                    FilledTonalButton(
                        onClick = { diaSeleccionado = dia },
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
            if (canchas.isEmpty()) {
                Text("No hay canchas registradas en el sistema por el Administrador.", color = MaterialTheme.colorScheme.error)
            } else {
                canchas.forEach { c ->
                    val ciudad = ciudadesMap[c.ciudadId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { canchaSeleccionadaId = c.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = canchaSeleccionadaId == c.id,
                            onClick = { canchaSeleccionadaId = c.id }
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
                onValueChange = { horaInicioStr = it },
                label = { Text("Hora de Inicio (HH:MM) *") },
                placeholder = { Text("08:00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = horaFinStr,
                onValueChange = { horaFinStr = it },
                label = { Text("Hora de Fin (HH:MM) *") },
                placeholder = { Text("09:30") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    try {
                        val inicio = LocalTime.parse(horaInicioStr.trim())
                        val fin = LocalTime.parse(horaFinStr.trim())

                        val res = repository.guardarHorario(
                            id = horarioId,
                            coachId = if (isCoachPrincipal) coachDesignadoId else coachId,
                            canchaId = canchaSeleccionadaId,
                            diaSemana = diaSeleccionado,
                            horaInicio = inicio,
                            horaFin = fin
                        )
                        res.onSuccess {
                            onBack()
                        }.onFailure { err ->
                            mensajeError = err.message
                        }
                    } catch (e: Exception) {
                        mensajeError = "Formato de hora inválido. Usa el formato HH:MM de 24 horas (ej: 08:30)"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Horario", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanchasCoachScreen(
    repository: AgendaTenisRepository,
    onBack: () -> Unit
) {
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()
    val ciudadesMap = ciudades.associateBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canchas Disponibles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            items(canchas) { cancha ->
                val ciudad = ciudadesMap[cancha.ciudadId]
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
                                text = "Ciudad: ${ciudad?.nombre ?: "Sin ciudad"}",
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
