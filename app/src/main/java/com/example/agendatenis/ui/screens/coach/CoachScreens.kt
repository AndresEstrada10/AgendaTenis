package com.example.agendatenis.ui.screens.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendatenis.model.*
import com.example.agendatenis.repository.AgendaTenisRepository
import com.example.agendatenis.ui.components.*
import com.example.agendatenis.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachHomeScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    onNavigateToDetalleJugador: (String) -> Unit,
    onNavigateToRegistrarPago: (String) -> Unit
) {
    val bannerMensaje by repository.bannerMensaje.collectAsState()
    val recordatorios = remember(coachId, repository.pagos.collectAsState().value, repository.jugadores.collectAsState().value) {
        repository.obtenerRecordatoriosParaCoach(coachId)
    }
    val clasesHoy = remember(coachId, repository.horarios.collectAsState().value, repository.asignaciones.collectAsState().value) {
        repository.obtenerClasesDeHoyParaCoach(coachId)
    }
    val jugadores by repository.jugadores.collectAsState()
    val coachJugadores = jugadores.filter { it.coachId == coachId && it.estado == EstadoJugador.ACTIVO }

    var mostrarDialogRecordatorios by remember { mutableStateOf(false) }

    if (mostrarDialogRecordatorios) {
        RecordatoriosDialog(
            recordatorios = recordatorios,
            onRecordatorioClick = { jugadorId ->
                onNavigateToDetalleJugador(jugadorId)
            },
            onDismiss = { mostrarDialogRecordatorios = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AgendaTenis",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Panel de Inicio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    CampanaConContador(
                        contador = recordatorios.size,
                        onClick = { mostrarDialogRecordatorios = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de confirmación "Pago registrado exitosamente" (componente prototipo)
            if (bannerMensaje != null) {
                item {
                    BannerPagoRegistrado(
                        mensaje = bannerMensaje ?: "",
                        onDismiss = { repository.setBannerMensaje(null) }
                    )
                }
            }

            // Sección 1: Pagos Próximos y Vencidos (RF-15, RN-09)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pagos Próximos y Vencidos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${recordatorios.size} alerta(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (recordatorios.any { it.esVencido }) TennisRedDestructive else TennisYellowAction
                    )
                }
            }

            if (recordatorios.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Todos los pagos de tus jugadores están al día.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recordatorios) { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetalleJugador(rec.jugadorId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rec.esVencido) TennisRedContainer.copy(alpha = 0.4f) else TennisYellowActionContainer.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarConIniciales(nombre = rec.jugadorNombre, size = 42.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.jugadorNombre,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = rec.mensaje,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (rec.esVencido) TennisRedDestructive else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = { onNavigateToRegistrarPago(rec.jugadorId) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (rec.esVencido) TennisRedDestructive else TennisYellowAction,
                                    contentColor = if (rec.esVencido) Color.White else TennisOnYellow
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Cobrar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Sección 2: Clases de Hoy (RF-10)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clases de Hoy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${clasesHoy.size} programada(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (clasesHoy.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No tienes clases programadas para el día de hoy.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(clasesHoy) { clase ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetalleJugador(clase.jugadorId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clase.jugadorNombre,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${clase.horaInicio} - ${clase.horaFin}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${clase.canchaNombre} (${clase.ciudadNombre})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Ver detalle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadoresListScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    onNavigateToDetalleJugador: (String) -> Unit,
    onNavigateToCrearJugador: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val jugadores by repository.jugadores.collectAsState()
    val planes by repository.planes.collectAsState()
    val horarios by repository.horarios.collectAsState()
    val asignaciones by repository.asignaciones.collectAsState()
    val pagos by repository.pagos.collectAsState()

    val coachJugadores = remember(jugadores, coachId, searchQuery) {
        jugadores
            .filter { it.coachId == coachId }
            .filter {
                if (searchQuery.isBlank()) true
                else it.nombre.contains(searchQuery, ignoreCase = true) || it.telefono.contains(searchQuery)
            }
            .sortedBy { it.nombre }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jugadores", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrearJugador,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar nuevo jugador")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Buscador en lista (componente prototipo)
            BuscadorLista(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar por nombre o teléfono...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (coachJugadores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No tienes jugadores registrados aún. Pulsa el botón (+) para agregar uno."
                        else "No se encontraron jugadores que coincidan con la búsqueda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)
                ) {
                    items(coachJugadores) { jugador ->
                        val plan = planes.firstOrNull { it.id == jugador.planId }
                        val jugadorAsignaciones = asignaciones.filter { it.jugadorId == jugador.id }
                        val jugadorHorarios = horarios.filter { h -> jugadorAsignaciones.any { it.horarioId == h.id } }
                        val diasActivos = jugadorHorarios.map { it.diaSemana }.toSet()
                        val horasTexto = jugadorHorarios.joinToString(", ") { "${it.horaInicio}-${it.horaFin}" }

                        val calc = remember(jugador.id, pagos) {
                            repository.calcularEstadoPago(jugador.id)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetalleJugador(jugador.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (jugador.estado == EstadoJugador.INACTIVO)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar con iniciales
                                    AvatarConIniciales(nombre = jugador.nombre, size = 44.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = jugador.nombre,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (jugador.estado == EstadoJugador.INACTIVO) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "INACTIVO",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            } else if (calc != null) {
                                                val badgeColor = when (calc.estado) {
                                                    EstadoPago.VENCIDO -> TennisRedDestructive
                                                    EstadoPago.PROXIMO -> TennisYellowAction
                                                    EstadoPago.AL_DIA -> TennisGreenLight
                                                }
                                                val textoEstado = when (calc.estado) {
                                                    EstadoPago.VENCIDO -> "Vencido"
                                                    EstadoPago.PROXIMO -> "Próximo"
                                                    EstadoPago.AL_DIA -> "Al día"
                                                }
                                                Surface(
                                                    color = badgeColor.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = textoEstado,
                                                        color = badgeColor,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = plan?.nombre ?: "Sin plan asignado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Días L M X J V S D resaltados (componente prototipo)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DiasSemanaResaltados(diasActivos = diasActivos)

                                    if (horasTexto.isNotBlank()) {
                                        Text(
                                            text = horasTexto,
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
    }
}
