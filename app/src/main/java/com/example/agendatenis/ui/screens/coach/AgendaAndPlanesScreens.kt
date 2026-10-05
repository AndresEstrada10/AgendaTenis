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
                Icon(Icons.Default.Add, contentDescription = "Programar nuevo horario")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filtro por Coach si es Coach Principal
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
                    FilterChip(
                        selected = coachFiltroId == coachId,
                        onClick = { coachFiltroId = coachId },
                        label = { Text("Mis Horarios") }
                    )
                    coaches.filter { it.id != coachId }.forEach { c ->
                        FilterChip(
                            selected = coachFiltroId == c.id,
                            onClick = { coachFiltroId = if (coachFiltroId == c.id) null else c.id },
                            label = { Text(c.nombre.split(" ").first()) }
                        )
                    }
                }
            }

            if (horariosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay horarios configurados en esta vista.\nPulsa el botón (+) para programar una clase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    // Agrupado por día de la semana (RF-19)
                    DiaSemana.entries.forEach { dia ->
                        val horariosDelDia = horariosFiltrados.filter { it.diaSemana == dia }
                        if (horariosDelDia.isNotEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dia.nombreCompleto.uppercase(),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(${horariosDelDia.size} bloque${if (horariosDelDia.size > 1) "s" else ""})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            items(horariosDelDia) { h ->
                                val cancha = canchasMap[h.canchaId]
                                val ciudad = cancha?.let { ciudadesMap[it.ciudadId] }
                                val coachResponsable = coachesMap[h.coachId]
                                val asignados = asignaciones.filter { it.horarioId == h.id }
                                val nombresJugadores = asignados.mapNotNull { jugadoresMap[it.jugadorId]?.nombre }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                Icon(
                                                    imageVector = Icons.Default.DateRange,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${h.horaInicio} - ${h.horaFin}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { onNavigateToEditarHorario(h.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { horarioAEliminar = h },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TennisRedDestructive, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (isCoachPrincipal && coachResponsable != null) {
                                            Text(
                                                text = "Coach: ${coachResponsable.nombre}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Cancha designada: ${cancha?.nombre ?: "Cancha desconocida"} • ${ciudad?.nombre ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (nombresJugadores.isEmpty()) "Sin jugadores asignados aún"
                                            else "Jugadores: ${nombresJugadores.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (nombresJugadores.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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
    val usuarios by repository.usuarios.collectAsState()
    val coaches = usuarios.filter { it.rol == Rol.COACH }

    val horarios by repository.horarios.collectAsState()
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
fun PlanesListScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    onNavigateToCrearPlan: () -> Unit,
    onNavigateToEditarPlan: (String) -> Unit
) {
    val planes by repository.planes.collectAsState()
    val jugadores by repository.jugadores.collectAsState()

    val coachPlanes = planes.filter { it.coachId == coachId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Planes", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrearPlan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nuevo plan")
            }
        }
    ) { paddingValues ->
        if (coachPlanes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No has creado planes de entrenamiento aún.\nPulsa el botón (+) para crear uno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(coachPlanes) { plan ->
                    val jugadoresConEstePlan = jugadores.count { it.planId == plan.id && it.estado == EstadoJugador.ACTIVO }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEditarPlan(plan.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.nombre,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${plan.clasesPorSemana} clases por semana",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$jugadoresConEstePlan jugador(es) activos inscritos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%.2f", plan.valor)}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TennisGreenLight
                                    )
                                )
                                Text(
                                    text = "Mensual",
                                    style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPlanScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    planId: String?,
    onBack: () -> Unit
) {
    val planes by repository.planes.collectAsState()
    val planExistente = remember(planId) {
        planes.firstOrNull { it.id == planId }
    }

    var nombre by remember { mutableStateOf(planExistente?.nombre ?: "") }
    var clasesPorSemanaStr by remember { mutableStateOf(planExistente?.clasesPorSemana?.toString() ?: "3") }
    var valorStr by remember { mutableStateOf(planExistente?.valor?.toString() ?: "50.0") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    if (mostrarConfirmarEliminar && planId != null) {
        ConfirmacionDialog(
            titulo = "¿Eliminar Plan?",
            mensaje = "¿Estás seguro de eliminar el plan '$nombre'? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                mostrarConfirmarEliminar = false
                val res = repository.eliminarPlan(planId)
                res.onSuccess {
                    onBack()
                }.onFailure { err ->
                    mensajeError = err.message
                }
            },
            onCancelar = { mostrarConfirmarEliminar = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (planId == null) "Nuevo Plan" else "Editar Plan", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Plan *") },
                placeholder = { Text("Ej. Plan Intermedio 3 Días") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = clasesPorSemanaStr,
                onValueChange = { clasesPorSemanaStr = it },
                label = { Text("Clases por Semana *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = valorStr,
                onValueChange = { valorStr = it },
                label = { Text("Valor Mensual ($) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val clases = clasesPorSemanaStr.toIntOrNull()
                    val valor = valorStr.toDoubleOrNull()
                    if (clases == null || clases <= 0) {
                        mensajeError = "Las clases por semana deben ser un número mayor a 0"
                        return@Button
                    }
                    if (valor == null || valor <= 0) {
                        mensajeError = "El valor debe ser un monto mayor a 0"
                        return@Button
                    }

                    val res = repository.guardarPlan(
                        id = planId,
                        nombre = nombre,
                        clasesPorSemana = clases,
                        valor = valor,
                        coachId = coachId
                    )
                    res.onSuccess {
                        onBack()
                    }.onFailure { err ->
                        mensajeError = err.message
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // RN-18: Guardar/Eliminar en FormPlan
            if (planId != null) {
                OutlinedButton(
                    onClick = { mostrarConfirmarEliminar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TennisRedDestructive),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Plan", fontWeight = FontWeight.Bold)
                }
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
    val ciudades by repository.ciudades.collectAsState()
    val canchas by repository.canchas.collectAsState()

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catálogo compartido (Solo Lectura). Las canchas y ciudades son administradas centralmente.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            ciudades.forEach { ciudad ->
                val canchasDeCiudad = canchas.filter { it.ciudadId == ciudad.id }

                item {
                    Text(
                        text = "Ciudad: ${ciudad.nombre}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (canchasDeCiudad.isEmpty()) {
                    item {
                        Text(
                            text = "No hay canchas registradas en esta ciudad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(canchasDeCiudad) { cancha ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = cancha.nombre,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (!cancha.referencia.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionCoachScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    onLogout: () -> Unit
) {
    val config = remember(coachId, repository.configs.collectAsState().value) {
        repository.obtenerConfigActual()
    }

    var tema by remember(config) { mutableStateOf(config.tema) }
    var recordarSesion by remember(config) { mutableStateOf(config.recordarSesion) }
    var diasAnticipacion by remember(config) { mutableStateOf(config.diasAnticipacion) }
    var mostrarConfirmarLogout by remember { mutableStateOf(false) }

    if (mostrarConfirmarLogout) {
        ConfirmacionDialog(
            titulo = "¿Cerrar Sesión?",
            mensaje = "¿Deseas cerrar tu sesión actual de AgendaTenis?",
            textoConfirmar = "Cerrar Sesión",
            esDestructivo = true,
            onConfirmar = {
                mostrarConfirmarLogout = false
                repository.cerrarSesion()
                onLogout()
            },
            onCancelar = { mostrarConfirmarLogout = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes y Configuración", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección Tema
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tema de la Aplicación",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                tema = TemaApp.CLARO
                                repository.actualizarConfig(TemaApp.CLARO, recordarSesion, diasAnticipacion)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (tema == TemaApp.CLARO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (tema == TemaApp.CLARO) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Claro")
                        }
                        FilledTonalButton(
                            onClick = {
                                tema = TemaApp.OSCURO
                                repository.actualizarConfig(TemaApp.OSCURO, recordarSesion, diasAnticipacion)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (tema == TemaApp.OSCURO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (tema == TemaApp.OSCURO) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Oscuro")
                        }
                    }
                }
            }

            // Sección Recordar Sesión
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recordar Sesión",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Mantener sesión iniciada al abrir la aplicación",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = recordarSesion,
                        onCheckedChange = {
                            recordarSesion = it
                            repository.actualizarConfig(tema, it, diasAnticipacion)
                        }
                    )
                }
            }

            // Sección Días de Anticipación (RF-15, RN-08: rango 1-7, def 3)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Anticipación de Recordatorios",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Avisar $diasAnticipacion días antes del vencimiento (Rango: 1 a 7 días)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = diasAnticipacion.toFloat(),
                        onValueChange = {
                            diasAnticipacion = it.toInt()
                            repository.actualizarConfig(tema, recordarSesion, it.toInt())
                        },
                        valueRange = 1f..7f,
                        steps = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón rojo: Cerrar Sesión (especificación de color: rojo solo para cerrar sesión / eliminar)
            Button(
                onClick = { mostrarConfirmarLogout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TennisRedDestructive,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
