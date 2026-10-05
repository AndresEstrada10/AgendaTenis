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
import com.example.agendatenis.ui.components.*
import com.example.agendatenis.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleJugadorScreen(
    repository: AgendaTenisRepository,
    jugadorId: String,
    onBack: () -> Unit,
    onNavigateToEditarJugador: (String) -> Unit,
    onNavigateToRegistrarPago: (String) -> Unit,
    onNavigateToEditarPago: (jugadorId: String, pagoId: String) -> Unit
) {
    val jugadores by repository.jugadores.collectAsState()
    val planes by repository.planes.collectAsState()
    val canchas by repository.canchas.collectAsState()
    val ciudades by repository.ciudades.collectAsState()
    val horarios by repository.horarios.collectAsState()
    val asignaciones by repository.asignaciones.collectAsState()
    val pagos by repository.pagos.collectAsState()
    val usuarios by repository.usuarios.collectAsState()

    val jugador = jugadores.firstOrNull { it.id == jugadorId }

    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var mensajeEliminacion by remember { mutableStateOf<String?>(null) }
    var pagoAEliminar by remember { mutableStateOf<Pago?>(null) }

    if (jugador == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de Jugador") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Jugador no encontrado")
            }
        }
        return
    }

    val coachAsignado = usuarios.firstOrNull { it.id == jugador.coachId }
    val plan = planes.firstOrNull { it.id == jugador.planId }
    val jugadorAsignaciones = asignaciones.filter { it.jugadorId == jugador.id }
    val jugadorHorarios = horarios.filter { h -> jugadorAsignaciones.any { it.horarioId == h.id } }
    val diasActivos = jugadorHorarios.map { it.diaSemana }.toSet()
    val historialPagos = remember(pagos, jugadorId) {
        repository.obtenerPagosPorJugador(jugadorId)
    }
    val estadoCalculado = remember(jugadorId, pagos) {
        repository.calcularEstadoPago(jugadorId)
    }

    val canchasMap = canchas.associateBy { it.id }
    val ciudadesMap = ciudades.associateBy { it.id }

    // Diálogo de confirmación para eliminar jugador (RN-20)
    if (mostrarConfirmarEliminar) {
        val tienePagos = historialPagos.isNotEmpty()
        ConfirmacionDialog(
            titulo = if (tienePagos) "¿Desactivar Jugador?" else "¿Eliminar Jugador?",
            mensaje = if (tienePagos)
                "El jugador '${jugador.nombre}' tiene pagos registrados en su historial. Por la regla del sistema, pasará a estado INACTIVO para preservar sus registros."
            else
                "El jugador '${jugador.nombre}' no registra pagos. Se eliminará definitivamente de la base de datos.",
            textoConfirmar = if (tienePagos) "Desactivar" else "Eliminar definitivamente",
            esDestructivo = true,
            onConfirmar = {
                mostrarConfirmarEliminar = false
                val res = repository.eliminarJugador(jugador.id)
                res.onSuccess {
                    onBack()
                }
            },
            onCancelar = { mostrarConfirmarEliminar = false }
        )
    }

    // Diálogo para eliminar pago
    if (pagoAEliminar != null) {
        ConfirmacionDialog(
            titulo = "¿Eliminar Registro de Pago?",
            mensaje = "Se eliminará el pago de $${pagoAEliminar?.monto} registrado el ${pagoAEliminar?.fecha}. Esto recalculará automáticamente la próxima fecha de vencimiento.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val pId = pagoAEliminar?.id
                pagoAEliminar = null
                if (pId != null) {
                    repository.eliminarPago(pId)
                }
            },
            onCancelar = { pagoAEliminar = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(jugador.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditarJugador(jugador.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Jugador")
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
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Perfil general
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarConIniciales(nombre = jugador.nombre, size = 60.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = jugador.nombre,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = jugador.telefono,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coach a cargo: ${coachAsignado?.nombre ?: "No asignado"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (jugador.estado == EstadoJugador.ACTIVO) "Estado: Activo" else "Estado: Inactivo",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (jugador.estado == EstadoJugador.ACTIVO) TennisGreenLight else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Datos del Plan y Próxima Fecha de Pago (RN-03, RN-04, RN-05)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Plan y Estado de Pagos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Plan actual:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = plan?.let { "${it.nombre} ($${it.valor})" } ?: "No asignado",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha de inicio:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = jugador.fechaInicio.toString(), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha ancla:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = estadoCalculado?.fechaAncla.toString(), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Próximo pago:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = estadoCalculado?.proximaFecha?.toString() ?: "N/A",
                                fontWeight = FontWeight.Bold,
                                color = when (estadoCalculado?.estado) {
                                    EstadoPago.VENCIDO -> TennisRedDestructive
                                    EstadoPago.PROXIMO -> TennisYellowAction
                                    else -> TennisGreenLight
                                }
                            )
                        }

                        if (estadoCalculado != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estado de cobro:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val (textoEstado, colorEstado) = when (estadoCalculado.estado) {
                                    EstadoPago.VENCIDO -> "Vencido (${-estadoCalculado.diasRestantes} días atrás)" to TennisRedDestructive
                                    EstadoPago.PROXIMO -> "Vence pronto (${estadoCalculado.diasRestantes} días)" to TennisYellowAction
                                    EstadoPago.AL_DIA -> "Al día (en ${estadoCalculado.diasRestantes} días)" to TennisGreenLight
                                }
                                Text(
                                    text = textoEstado,
                                    fontWeight = FontWeight.Bold,
                                    color = colorEstado
                                )
                            }
                        }
                    }
                }
            }

            // Horarios y Canchas asignados
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Días y Horarios Asignados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        DiasSemanaResaltados(diasActivos = diasActivos)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (jugadorHorarios.isEmpty()) {
                            Text(
                                text = "Sin horarios de clase asignados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            jugadorHorarios.forEach { h ->
                                val cancha = canchasMap[h.canchaId]
                                val ciudad = cancha?.let { ciudadesMap[it.ciudadId] }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${h.diaSemana.nombreCompleto}: ${h.horaInicio} - ${h.horaFin}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${cancha?.nombre ?: ""} (${ciudad?.nombre ?: ""})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tarjeta amarilla "Registrar pago" (componente prototipo)
            item {
                TarjetaRegistrarPago(
                    jugadorNombre = jugador.nombre,
                    montoPlan = plan?.valor,
                    onClick = { onNavigateToRegistrarPago(jugador.id) }
                )
            }

            // Historial de Pagos (RF-12, RF-13)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de Pagos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${historialPagos.size} pago(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (historialPagos.isEmpty()) {
                item {
                    Text(
                        text = "No hay registros de pago para este jugador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(historialPagos) { pago ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$${String.format("%.2f", pago.monto)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TennisGreenLight
                                    )
                                )
                                Text(
                                    text = "Fecha: ${pago.fecha}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!pago.observacion.isNullOrBlank()) {
                                    Text(
                                        text = "Obs: ${pago.observacion}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            IconButton(onClick = { onNavigateToEditarPago(jugador.id, pago.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Pago", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { pagoAEliminar = pago }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar Pago", tint = TennisRedDestructive)
                            }
                        }
                    }
                }
            }

            // Botón Eliminar / Desactivar Jugador (rojo)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { mostrarConfirmarEliminar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TennisRedDestructive
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (historialPagos.isNotEmpty()) "Desactivar Jugador (Conservar Historial)" else "Eliminar Jugador",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormJugadorScreen(
    repository: AgendaTenisRepository,
    coachId: String,
    jugadorId: String?,
    onBack: () -> Unit
) {
    val usuarioActual by repository.usuarioActual.collectAsState()
    val isCoachPrincipal = usuarioActual?.esCoachPrincipal == true
    val usuarios by repository.usuarios.collectAsState()
    val coaches = usuarios.filter { it.rol == Rol.COACH }

    val jugadores by repository.jugadores.collectAsState()
    val planes by repository.planes.collectAsState()
    val horarios by repository.horarios.collectAsState()
    val asignaciones by repository.asignaciones.collectAsState()
    val canchas by repository.canchas.collectAsState()

    val jugadorExistente = remember(jugadorId) {
        jugadores.firstOrNull { it.id == jugadorId }
    }

    var coachDesignadoId by remember { mutableStateOf(jugadorExistente?.coachId ?: coachId) }
    var nombre by remember { mutableStateOf(jugadorExistente?.nombre ?: "") }
    var telefono by remember { mutableStateOf(jugadorExistente?.telefono ?: "") }
    var fechaInicioStr by remember { mutableStateOf(jugadorExistente?.fechaInicio?.toString() ?: LocalDate.now().toString()) }
    var planSeleccionadoId by remember { mutableStateOf(jugadorExistente?.planId ?: (planes.firstOrNull { it.coachId == coachDesignadoId }?.id ?: "")) }

    // Horarios asignados
    val initialHorarios = remember(jugadorExistente, asignaciones) {
        if (jugadorExistente != null) {
            asignaciones.filter { it.jugadorId == jugadorExistente.id }.map { it.horarioId }.toSet()
        } else {
            emptySet()
        }
    }
    var horariosSeleccionados by remember { mutableStateOf(initialHorarios) }

    var mensajeError by remember { mutableStateOf<String?>(null) }

    val coachPlanes = planes.filter { it.coachId == coachDesignadoId }
    val coachHorarios = horarios.filter { it.coachId == coachDesignadoId }
    val planActual = planes.firstOrNull { it.id == planSeleccionadoId }

    // RN-15: advertencia si dias asignados != clasesPorSemana del plan
    val clasesAsignadasCount = horariosSeleccionados.size
    val requiereAdvertenciaRN15 = planActual != null && clasesAsignadasCount != planActual.clasesPorSemana

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (jugadorId == null) "Nuevo Jugador" else "Editar Jugador", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "Coach Designado *",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                coaches.forEach { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coachDesignadoId = c.id
                                // reset plan if not matching
                                if (!planes.any { it.id == planSeleccionadoId && it.coachId == c.id }) {
                                    planSeleccionadoId = planes.firstOrNull { it.coachId == c.id }?.id ?: ""
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = coachDesignadoId == c.id,
                            onClick = {
                                coachDesignadoId = c.id
                                if (!planes.any { it.id == planSeleccionadoId && it.coachId == c.id }) {
                                    planSeleccionadoId = planes.firstOrNull { it.coachId == c.id }?.id ?: ""
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${c.nombre} ${if (c.id == coachId) "(Tú - Coach Principal)" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = fechaInicioStr,
                onValueChange = { fechaInicioStr = it },
                label = { Text("Fecha de Inicio (AAAA-MM-DD) *") },
                placeholder = { Text("Ej. 2026-03-01") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Selector de Plan
            Text(
                text = "Seleccionar Plan *",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            if (coachPlanes.isEmpty()) {
                Text(
                    text = "Aún no hay planes creados para este coach.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                coachPlanes.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { planSeleccionadoId = p.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = planSeleccionadoId == p.id,
                            onClick = { planSeleccionadoId = p.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${p.nombre} (${p.clasesPorSemana} clases/sem) - $${String.format("%.2f", p.valor)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Asignación de Horarios
            Text(
                text = "Asignar Horarios de Clase",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            if (coachHorarios.isEmpty()) {
                Text(
                    text = "No hay horarios configurados para este coach. Puedes programarlos en la sección Agenda o Gestión de Coaches.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                coachHorarios.forEach { h ->
                    val cancha = canchas.firstOrNull { it.id == h.canchaId }
                    val isChecked = horariosSeleccionados.contains(h.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                horariosSeleccionados = if (isChecked) {
                                    horariosSeleccionados - h.id
                                } else {
                                    horariosSeleccionados + h.id
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                horariosSeleccionados = if (checked) {
                                    horariosSeleccionados + h.id
                                } else {
                                    horariosSeleccionados - h.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${h.diaSemana.nombreCompleto} (${h.horaInicio}-${h.horaFin}) • ${cancha?.nombre ?: ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // RN-15: Advertencia si días asignados != clasesPorSemana del plan
            if (requiereAdvertenciaRN15) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TennisYellowActionContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TennisOnYellow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aviso (RN-15): El plan contempla ${planActual?.clasesPorSemana} clase(s)/sem, pero tienes seleccionados $clasesAsignadasCount horario(s). El sistema permite guardar, pero asegúrate de ajustar las clases.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TennisOnYellow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    try {
                        val fechaInicio = LocalDate.parse(fechaInicioStr.trim())
                        val res = repository.guardarJugador(
                            id = jugadorId,
                            nombre = nombre,
                            telefono = telefono,
                            fechaInicio = fechaInicio,
                            planId = planSeleccionadoId,
                            coachId = if (isCoachPrincipal) coachDesignadoId else coachId,
                            horariosIds = horariosSeleccionados.toList()
                        )
                        res.onSuccess {
                            onBack()
                        }.onFailure { err ->
                            mensajeError = err.message
                        }
                    } catch (e: Exception) {
                        mensajeError = "Formato de fecha inválido. Utiliza el formato AAAA-MM-DD (ej: 2026-03-01)"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Jugador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPagoScreen(
    repository: AgendaTenisRepository,
    jugadorId: String,
    pagoId: String?,
    onBack: () -> Unit
) {
    val jugadores by repository.jugadores.collectAsState()
    val planes by repository.planes.collectAsState()
    val pagos by repository.pagos.collectAsState()

    val jugador = jugadores.firstOrNull { it.id == jugadorId }
    val plan = planes.firstOrNull { it.id == jugador?.planId }
    val pagoExistente = remember(pagoId) {
        pagos.firstOrNull { it.id == pagoId }
    }

    val valorSugerido = remember(plan, pagoExistente) {
        if (pagoExistente != null) pagoExistente.monto.toString()
        else (plan?.valor ?: 50.0).toString()
    }

    var montoStr by remember { mutableStateOf(valorSugerido) }
    var fechaStr by remember { mutableStateOf(pagoExistente?.fecha?.toString() ?: LocalDate.now().toString()) }
    var observacion by remember { mutableStateOf(pagoExistente?.observacion ?: "") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pagoId == null) "Registrar Pago" else "Editar Pago", fontWeight = FontWeight.Bold) },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Jugador: ${jugador?.nombre ?: "N/A"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Plan: ${plan?.nombre ?: "Sin plan"} • Cuota fijada: $${plan?.valor ?: 0.0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

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

            // RN-13: Monto precargado con valor del plan, editable, > 0
            OutlinedTextField(
                value = montoStr,
                onValueChange = { montoStr = it },
                label = { Text("Monto a registrar ($) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = fechaStr,
                onValueChange = { fechaStr = it },
                label = { Text("Fecha de Pago (AAAA-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación (opcional)") },
                placeholder = { Text("Ej. Efectivo / Transferencia Banco Pichincha") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val montoDouble = montoStr.toDoubleOrNull()
                    if (montoDouble == null || montoDouble <= 0) {
                        mensajeError = "El monto debe ser un número válido mayor a 0"
                        return@Button
                    }
                    try {
                        val fechaLocalDate = LocalDate.parse(fechaStr.trim())
                        if (pagoId == null) {
                            val res = repository.registrarPago(
                                jugadorId = jugadorId,
                                fecha = fechaLocalDate,
                                monto = montoDouble,
                                observacion = observacion
                            )
                            res.onSuccess {
                                onBack()
                            }.onFailure { err ->
                                mensajeError = err.message
                            }
                        } else {
                            val res = repository.actualizarPago(
                                pagoId = pagoId,
                                fecha = fechaLocalDate,
                                monto = montoDouble,
                                observacion = observacion
                            )
                            res.onSuccess {
                                onBack()
                            }.onFailure { err ->
                                mensajeError = err.message
                            }
                        }
                    } catch (e: Exception) {
                        mensajeError = "Formato de fecha inválido. Usa AAAA-MM-DD"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TennisYellowAction,
                    contentColor = TennisOnYellow
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
