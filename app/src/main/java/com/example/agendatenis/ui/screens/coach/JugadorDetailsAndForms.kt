package com.example.agendatenis.ui.screens.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendatenis.model.*
import com.example.agendatenis.ui.components.*
import com.example.agendatenis.ui.theme.*
import com.example.agendatenis.ui.viewmodel.coach.DetalleJugadorUiState
import com.example.agendatenis.ui.viewmodel.coach.FormJugadorUiState
import com.example.agendatenis.ui.viewmodel.coach.FormPagoUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleJugadorScreen(
    uiState: DetalleJugadorUiState,
    onBack: () -> Unit,
    onNavigateToEditarJugador: (String) -> Unit,
    onNavigateToRegistrarPago: (String) -> Unit,
    onNavigateToEditarPago: (jugadorId: String, pagoId: String) -> Unit,
    onSetMostrarConfirmarEliminar: (Boolean) -> Unit,
    onConfirmarEliminarJugador: () -> Unit,
    onSetPagoAEliminar: (Pago?) -> Unit,
    onConfirmarEliminarPago: () -> Unit
) {
    val jugador = uiState.jugador

    if (jugador == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de Jugador") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

    val coachAsignado = uiState.coachAsignado
    val jugadorHorarios = uiState.horariosAsignados
    val diasActivos = remember(jugadorHorarios) { jugadorHorarios.map { it.diaSemana }.toSet() }
    val historialPagos = uiState.pagos
    val estadoCalculado = uiState.pagoCalculado
    val canchasMap = uiState.canchasMap

    // Diálogo de confirmación para eliminar jugador (RN-20)
    if (uiState.mostrarDialogEliminarJugador) {
        val tienePagos = historialPagos.isNotEmpty()
        ConfirmacionDialog(
            titulo = if (tienePagos) "¿Desactivar Jugador?" else "¿Eliminar Jugador?",
            mensaje = if (tienePagos)
                "El jugador '${jugador.nombre}' tiene pagos registrados en su historial. Por la regla del sistema, pasará a estado INACTIVO para preservar sus registros."
            else
                "El jugador '${jugador.nombre}' no registra pagos. Se eliminará definitivamente de la base de datos.",
            textoConfirmar = if (tienePagos) "Desactivar" else "Eliminar definitivamente",
            esDestructivo = true,
            onConfirmar = onConfirmarEliminarJugador,
            onCancelar = { onSetMostrarConfirmarEliminar(false) }
        )
    }

    // Diálogo para eliminar pago
    if (uiState.pagoParaEliminar != null) {
        val pago = uiState.pagoParaEliminar
        ConfirmacionDialog(
            titulo = "¿Eliminar Registro de Pago?",
            mensaje = "Se eliminará el pago de $${pago.monto} registrado el ${pago.fecha}. Esto recalculará automáticamente la próxima fecha de vencimiento.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = onConfirmarEliminarPago,
            onCancelar = { onSetPagoAEliminar(null) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(jugador.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
        ) {
            // Tarjeta de Perfil
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarConIniciales(nombre = jugador.nombre, size = 56.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = jugador.nombre,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Coach: ${coachAsignado?.nombre ?: "Sin asignar"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Inicio: ${jugador.fechaInicio}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Teléfono
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = jugador.telefono, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Correo propio del jugador
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = jugador.correo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Estado de Pago y Cuota Mensual Individual
            if (estadoCalculado != null) {
                item {
                    val calc = estadoCalculado
                    val badgeColor = when (calc.estado) {
                        EstadoPago.VENCIDO -> TennisRedDestructive
                        EstadoPago.PROXIMO -> TennisYellowAction
                        EstadoPago.AL_DIA -> TennisGreenLight
                    }
                    val estadoTitulo = when (calc.estado) {
                        EstadoPago.VENCIDO -> "PAGO VENCIDO"
                        EstadoPago.PROXIMO -> "PAGO PRÓXIMO A VENCER"
                        EstadoPago.AL_DIA -> "PAGO AL DÍA"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = estadoTitulo,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "$${String.format("%.2f", calc.valorMensual)} / mes",
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Próxima Fecha de Vencimiento: ${calc.proximaFecha}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Fecha Ancla del Calendario: ${calc.fechaAncla}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (calc.diasRestantes < 0) "Venció hace ${-calc.diasRestantes} día(s)"
                                else if (calc.diasRestantes == 0L) "Vence hoy"
                                else "Faltan ${calc.diasRestantes} día(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Horarios Asignados al Jugador
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Horarios y Días de Clase",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            DiasSemanaResaltados(diasActivos = diasActivos)
                        }

                        if (jugadorHorarios.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "El jugador no tiene horarios asignados en la agenda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            jugadorHorarios.forEach { h ->
                                val cancha = canchasMap[h.canchaId]
                                val canchaNombre = cancha?.nombre ?: ""
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = TennisGreenLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${h.diaSemana.nombreCompleto} (${h.horaInicio}-${h.horaFin}) • $canchaNombre",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Historial de Pagos con botón de acción
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de Pagos (${historialPagos.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = { onNavigateToRegistrarPago(jugador.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TennisYellowAction,
                            contentColor = TennisOnYellow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar Pago", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (historialPagos.isEmpty()) {
                item {
                    Text(
                        text = "No hay registros de pago. El estado se calcula a partir de la fecha de inicio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            IconButton(onClick = { onSetPagoAEliminar(pago) }) {
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
                    onClick = { onSetMostrarConfirmarEliminar(true) },
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
    uiState: FormJugadorUiState,
    isCoachPrincipal: Boolean,
    isAdmin: Boolean,
    onNombreChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onFechaInicioChange: (LocalDate) -> Unit,
    onValorMensualChange: (String) -> Unit,
    onCoachIdChange: (String) -> Unit,
    onToggleHorario: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onBack: () -> Unit
) {
    var fechaInicioStr by remember(uiState.fechaInicio) { mutableStateOf(uiState.fechaInicio.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (!uiState.esEdicion) "Nuevo Jugador" else "Editar Jugador", fontWeight = FontWeight.Bold) },
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

            // Nombre
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre Completo *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Teléfono
            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = onTelefonoChange,
                label = { Text("Teléfono de Contacto *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Correo propio del jugador
            OutlinedTextField(
                value = uiState.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo Electrónico (para recordatorios de cobro) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Fecha de Inicio
            OutlinedTextField(
                value = fechaInicioStr,
                onValueChange = {
                    fechaInicioStr = it
                    try {
                        val parsed = LocalDate.parse(it.trim())
                        onFechaInicioChange(parsed)
                    } catch (_: Exception) {}
                },
                label = { Text("Fecha de Inicio (AAAA-MM-DD) *") },
                placeholder = { Text("2026-03-01") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Valor mensual individual
            OutlinedTextField(
                value = uiState.valorMensual,
                onValueChange = onValorMensualChange,
                label = { Text("Cuota Mensual Individual ($) *") },
                placeholder = { Text("Ej. 75.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Asignación de Coach si es Administrador o Coach Principal
            if (isAdmin || isCoachPrincipal) {
                Text(
                    text = "Coach Asignado *",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
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
                        Text(text = c.nombre, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider()
            }

            // Selección de Horarios del Coach
            Text(
                text = "Horarios Semanales Disponibles del Coach",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            if (uiState.horariosDisponibles.isEmpty()) {
                Text(
                    text = "Este coach no tiene horarios registrados en la agenda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.horariosDisponibles.forEach { h ->
                    val isChecked = uiState.horariosSeleccionados.contains(h.id)
                    val cancha = uiState.canchasMap[h.canchaId]
                    val ciudad = cancha?.let { uiState.ciudadesMap[it.ciudadId] }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleHorario(h.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleHorario(h.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${h.diaSemana.nombreCompleto} (${h.horaInicio} - ${h.horaFin})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${cancha?.nombre ?: "Cancha"} - ${ciudad?.nombre ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    try {
                        val parsed = LocalDate.parse(fechaInicioStr.trim())
                        onFechaInicioChange(parsed)
                    } catch (_: Exception) {}
                    onGuardarClick()
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
                    Text("Guardar Jugador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPagoScreen(
    uiState: FormPagoUiState,
    onFechaChange: (LocalDate) -> Unit,
    onMontoChange: (String) -> Unit,
    onObservacionChange: (String) -> Unit,
    onGuardarClick: () -> Unit,
    onBack: () -> Unit
) {
    val jugador = uiState.jugador
    var fechaStr by remember(uiState.fecha) { mutableStateOf(uiState.fecha.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (!uiState.esEdicion) "Registrar Pago" else "Editar Pago", fontWeight = FontWeight.Bold) },
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
                        text = "Cuota mensual individual: $${String.format("%.2f", jugador?.valorMensual ?: 0.0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

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

            // Monto precargado con el valor mensual individual del jugador, editable, > 0
            OutlinedTextField(
                value = uiState.monto,
                onValueChange = onMontoChange,
                label = { Text("Monto a registrar ($) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = fechaStr,
                onValueChange = {
                    fechaStr = it
                    try {
                        val parsed = LocalDate.parse(it.trim())
                        onFechaChange(parsed)
                    } catch (_: Exception) {}
                },
                label = { Text("Fecha de Pago (AAAA-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = uiState.observacion,
                onValueChange = onObservacionChange,
                label = { Text("Observación (opcional)") },
                placeholder = { Text("Ej. Efectivo / Transferencia Banco Pichincha") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    try {
                        val parsed = LocalDate.parse(fechaStr.trim())
                        onFechaChange(parsed)
                    } catch (_: Exception) {}
                    onGuardarClick()
                },
                enabled = !uiState.isGuardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TennisYellowAction,
                    contentColor = TennisOnYellow
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.isGuardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
