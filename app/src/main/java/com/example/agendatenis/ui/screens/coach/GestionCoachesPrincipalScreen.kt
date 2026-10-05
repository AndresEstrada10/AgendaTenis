package com.example.agendatenis.ui.screens.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.agendatenis.repository.AgendaTenisRepository
import com.example.agendatenis.ui.components.AvatarConIniciales
import com.example.agendatenis.ui.components.ConfirmacionDialog
import com.example.agendatenis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCoachesPrincipalScreen(
    repository: AgendaTenisRepository,
    onBack: () -> Unit,
    onNavigateToCrearHorarioCoach: (coachId: String) -> Unit,
    onNavigateToEditarHorarioCoach: (coachId: String, horarioId: String) -> Unit,
    onNavigateToDetalleJugador: (jugadorId: String) -> Unit
) {
    val usuarios by repository.usuarios.collectAsState()
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()
    val horarios by repository.horarios.collectAsState()
    val jugadores by repository.jugadores.collectAsState()

    val canchasMap = remember(canchas) { canchas.associateBy { it.id } }
    val ciudadesMap = remember(ciudades) { ciudades.associateBy { it.id } }

    var coachSeleccionadoId by remember { mutableStateOf<String?>(null) }
    var jugadorParaReasignar by remember { mutableStateOf<Jugador?>(null) }
    var horarioAEliminar by remember { mutableStateOf<Horario?>(null) }

    val coaches = usuarios.filter { it.rol == Rol.COACH }

    // Diálogo Reasignar Jugador
    if (jugadorParaReasignar != null) {
        val jug = jugadorParaReasignar!!
        AlertDialog(
            onDismissRequest = { jugadorParaReasignar = null },
            title = { Text("Reasignar Jugador: ${jug.nombre}") },
            text = {
                Column {
                    Text(
                        text = "Selecciona el coach a quien se le designará este jugador:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    coaches.forEach { c ->
                        val isCurrent = c.id == jug.coachId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.reasignarCoachAJugador(jug.id, c.id)
                                    jugadorParaReasignar = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isCurrent,
                                onClick = {
                                    repository.reasignarCoachAJugador(jug.id, c.id)
                                    jugadorParaReasignar = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${c.nombre} ${if (isCurrent) "(Actual)" else ""}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { jugadorParaReasignar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo Eliminar Horario
    if (horarioAEliminar != null) {
        ConfirmacionDialog(
            titulo = "¿Eliminar Horario Designado?",
            mensaje = "¿Deseas eliminar el horario de ${horarioAEliminar?.diaSemana?.nombreCompleto} (${horarioAEliminar?.horaInicio} - ${horarioAEliminar?.horaFin})?",
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
                        Text("Gestión de Coaches", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Coach Principal (Jorge Sanmartin)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panel de Supervisión y Asignación",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Como Coach Principal tienes la facultad de visualizar todos los coaches, supervisar sus jugadores designados, y asignarles canchas y horarios de entrenamiento.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(coaches) { coach ->
                val esElMismo = coach.id == "user_coach_1"
                val jugadoresDelCoach = jugadores.filter { it.coachId == coach.id && it.estado == EstadoJugador.ACTIVO }
                val horariosDelCoach = horarios.filter { it.coachId == coach.id }.sortedWith(compareBy({ it.diaSemana.diaIso }, { it.horaInicio }))
                val canchasDesignadas = horariosDelCoach.mapNotNull { canchasMap[it.canchaId] }.distinctBy { it.id }

                var expandido by remember { mutableStateOf(esElMismo) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esElMismo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarConIniciales(nombre = coach.nombre, size = 50.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = coach.nombre,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (esElMismo) {
                                        Surface(
                                            color = TennisYellowActionContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "COACH PRINCIPAL",
                                                color = TennisOnYellow,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = coach.correo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${jugadoresDelCoach.size} jugador(es)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${canchasDesignadas.size} cancha(s)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${horariosDelCoach.size} horario(s)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón de alternar detalles
                        OutlinedButton(
                            onClick = { expandido = !expandido },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (expandido) "Ocultar Detalles y Horarios" else "Administrar Canchas, Horarios y Jugadores")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (expandido) {
                            Spacer(modifier = Modifier.height(14.dp))

                            // SECCIÓN 1: CANCHAS Y HORARIOS DESIGNADOS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Horarios y Canchas Designadas",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(
                                    onClick = { onNavigateToCrearHorarioCoach(coach.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Designar Horario", fontSize = 12.sp)
                                }
                            }

                            if (horariosDelCoach.isEmpty()) {
                                Text(
                                    text = "Sin horarios de clase ni canchas designadas para este coach.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                horariosDelCoach.forEach { h ->
                                    val cancha = canchasMap[h.canchaId]
                                    val ciudad = cancha?.let { ciudadesMap[it.ciudadId] }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${h.diaSemana.nombreCompleto}: ${h.horaInicio} - ${h.horaFin}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                                Text(
                                                    text = "Cancha: ${cancha?.nombre ?: ""} (${ciudad?.nombre ?: ""})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = { onNavigateToEditarHorarioCoach(coach.id, h.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar horario", modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { horarioAEliminar = h },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar horario", tint = TennisRedDestructive, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // SECCIÓN 2: JUGADORES DESIGNADOS
                            Text(
                                text = "Jugadores Designados (${jugadoresDelCoach.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (jugadoresDelCoach.isEmpty()) {
                                Text(
                                    text = "Este coach no tiene jugadores designados actualmente.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                jugadoresDelCoach.forEach { jug ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { onNavigateToDetalleJugador(jug.id) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AvatarConIniciales(nombre = jug.nombre, size = 32.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = jug.nombre,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    Text(
                                                        text = "Tel: ${jug.telefono}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { jugadorParaReasignar = jug },
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reasignar", fontSize = 11.sp)
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
    }
}
