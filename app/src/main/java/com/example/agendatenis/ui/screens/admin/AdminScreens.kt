package com.example.agendatenis.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.agendatenis.ui.components.AvatarConIniciales
import com.example.agendatenis.ui.components.BuscadorLista
import com.example.agendatenis.ui.components.ConfirmacionDialog
import com.example.agendatenis.ui.theme.*
import com.example.agendatenis.ui.components.SelectorTema
import com.example.agendatenis.ui.viewmodel.admin.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    uiState: AdminHomeUiState,
    onNavigateToCoaches: () -> Unit,
    onNavigateToJugadores: () -> Unit,
    onNavigateToCatalogos: () -> Unit
) {
    val resumen = uiState.resumen

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AgendaTenis Admin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Panel Central del Sistema", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(
                text = "Resumen General del Sistema (A-09)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Tarjeta Resumen Coaches
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCoaches() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Coaches Registrados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${resumen.coachesActivos} activos / ${resumen.totalCoaches} totales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = "${resumen.totalCoaches}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Tarjeta Resumen Jugadores Activos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToJugadores() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Jugadores Activos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "En todos los coaches",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${resumen.jugadoresActivos}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Tarjeta Pagos Próximos Globales
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToJugadores() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TennisYellowActionContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pagos por Cobrar Globales",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TennisOnYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Próximos o vencidos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TennisOnYellow.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = "${resumen.pagosProximosGlobales}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TennisOnYellow
                        )
                    )
                }
            }

            // Acceso rápido a Catálogos
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCatalogos() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gestión de Catálogos (A-03, A-04)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Administra ciudades y canchas deportivas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCoachesScreen(
    uiState: AdminCoachesUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleEstadoCoach: (Usuario) -> Unit,
    onNavigateToCrearCoach: () -> Unit,
    onNavigateToEditarCoach: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Coaches (A-02)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrearCoach,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Coach")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            BuscadorLista(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Buscar coach por nombre o correo...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (uiState.coachesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron cuentas de coaches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(uiState.coachesFiltrados) { coach ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEditarCoach(coach.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (coach.estado == EstadoUsuario.INACTIVO)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarConIniciales(nombre = coach.nombre, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = coach.nombre,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = coach.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (coach.estado == EstadoUsuario.ACTIVO) "Estado: ACTIVO" else "Estado: DESACTIVADO (RN-21)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (coach.estado == EstadoUsuario.ACTIVO) TennisGreenLight else TennisRedDestructive
                                    )
                                }
                                Switch(
                                    checked = coach.estado == EstadoUsuario.ACTIVO,
                                    onCheckedChange = {
                                        onToggleEstadoCoach(coach)
                                    }
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
fun AdminFormCoachScreen(
    uiState: AdminFormCoachUiState,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onEstadoChange: (EstadoUsuario) -> Unit,
    onGuardarClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (!uiState.esEdicion) "Nuevo Coach (A-02)" else "Editar Coach", fontWeight = FontWeight.Bold) },
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

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre Completo *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = uiState.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo Electrónico *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = uiState.contrasena,
                onValueChange = onContrasenaChange,
                label = { Text(if (!uiState.esEdicion) "Contraseña inicial (min 6 car.) *" else "Nueva Contraseña (dejar en blanco para no cambiar)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Switch Estado (Activo / Inactivo) -> RN-21
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
                            text = "Estado de la cuenta: ${if (uiState.estado == EstadoUsuario.ACTIVO) "ACTIVO" else "DESACTIVADO"}",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.estado == EstadoUsuario.ACTIVO) TennisGreenLight else TennisRedDestructive
                        )
                        Text(
                            text = if (uiState.estado == EstadoUsuario.ACTIVO) "El coach puede iniciar sesión en la app" else "El coach no podrá iniciar sesión; sus datos se preservan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.estado == EstadoUsuario.ACTIVO,
                        onCheckedChange = { checked ->
                            onEstadoChange(if (checked) EstadoUsuario.ACTIVO else EstadoUsuario.INACTIVO)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onGuardarClick,
                enabled = !uiState.isGuardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.isGuardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJugadoresScreen(
    uiState: AdminJugadoresUiState,
    onSearchQueryChange: (String) -> Unit,
    onCoachFiltroChange: (String?) -> Unit,
    onSeleccionarJugadorParaReasignar: (Jugador?) -> Unit,
    onConfirmarReasignarCoach: (String, String) -> Unit,
    onNavigateToDetalleJugador: (String) -> Unit,
    onNavigateToCrearJugador: () -> Unit = {}
) {
    // Modal para Reasignar Coach (A-06)
    if (uiState.jugadorParaReasignar != null) {
        val jug = uiState.jugadorParaReasignar
        AlertDialog(
            onDismissRequest = { onSeleccionarJugadorParaReasignar(null) },
            title = { Text("Reasignar Coach a ${jug.nombre}") },
            text = {
                Column {
                    Text(
                        text = "Selecciona el nuevo coach responsable (A-06):",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    uiState.coaches.forEach { c ->
                        val isCurrent = c.id == jug.coachId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onConfirmarReasignarCoach(jug.id, c.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isCurrent,
                                onClick = {
                                    onConfirmarReasignarCoach(jug.id, c.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${c.nombre} ${if (isCurrent) "(Actual)" else ""}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onSeleccionarJugadorParaReasignar(null) }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jugadores Globales (A-06)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCrearJugador,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Jugador")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            BuscadorLista(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Buscar jugador...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Selector de filtro de coach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.coachFiltroId == null,
                    onClick = { onCoachFiltroChange(null) },
                    label = { Text("Todos") }
                )
                uiState.coaches.forEach { c ->
                    FilterChip(
                        selected = uiState.coachFiltroId == c.id,
                        onClick = { onCoachFiltroChange(if (uiState.coachFiltroId == c.id) null else c.id) },
                        label = { Text(c.nombre.split(" ").first()) }
                    )
                }
            }

            if (uiState.jugadoresFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron jugadores.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(uiState.jugadoresFiltrados) { jugador ->
                        val coach = uiState.coachesMap[jugador.coachId]

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
                                            }
                                        }

                                        Text(
                                            text = "Coach: ${coach?.nombre ?: "Sin asignar"} • Cuota: $${String.format("%.2f", jugador.valorMensual)}/mes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Tel: ${jugador.telefono} • Correo: ${jugador.correo}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(onClick = { onSeleccionarJugadorParaReasignar(jugador) }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reasignar Coach (A-06)",
                                            tint = MaterialTheme.colorScheme.primary
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

@Composable
fun AdminCatalogosScreen(
    uiState: AdminCatalogosUiState,
    onGuardarCiudad: (id: String?, nombre: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onEliminarCiudad: (id: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onGuardarCancha: (id: String?, nombre: String, referencia: String?, ciudadId: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onEliminarCancha: (id: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onLimpiarError: () -> Unit
) {
    val ciudades = uiState.ciudades
    val canchas = uiState.canchas

    var mostrarDialogNuevaCiudad by remember { mutableStateOf(false) }
    var ciudadParaEditar by remember { mutableStateOf<Ciudad?>(null) }
    var ciudadParaEliminar by remember { mutableStateOf<Ciudad?>(null) }

    var ciudadIdParaNuevaCancha by remember { mutableStateOf<String?>(null) }
    var canchaParaEditar by remember { mutableStateOf<Cancha?>(null) }
    var canchaParaEliminar by remember { mutableStateOf<Cancha?>(null) }

    var localError by remember { mutableStateOf<String?>(null) }
    val displayError = localError ?: uiState.errorMessage

    // Dialog Crear / Editar Ciudad
    if (mostrarDialogNuevaCiudad || ciudadParaEditar != null) {
        var nombreCiudad by remember { mutableStateOf(ciudadParaEditar?.nombre ?: "") }
        AlertDialog(
            onDismissRequest = {
                mostrarDialogNuevaCiudad = false
                ciudadParaEditar = null
            },
            title = { Text(if (ciudadParaEditar == null) "Nueva Ciudad" else "Editar Ciudad") },
            text = {
                OutlinedTextField(
                    value = nombreCiudad,
                    onValueChange = { nombreCiudad = it },
                    label = { Text("Nombre de la ciudad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onGuardarCiudad(ciudadParaEditar?.id, nombreCiudad) { success, err ->
                        if (success) {
                            mostrarDialogNuevaCiudad = false
                            ciudadParaEditar = null
                            localError = null
                        } else {
                            localError = err
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogNuevaCiudad = false
                    ciudadParaEditar = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog Eliminar Ciudad (RN-19: no eliminar si tiene canchas)
    if (ciudadParaEliminar != null) {
        val ciudad = ciudadParaEliminar
        ConfirmacionDialog(
            titulo = "¿Eliminar Ciudad?",
            mensaje = "¿Deseas eliminar la ciudad '${ciudad?.nombre}'? Recuerda que no se puede eliminar si posee canchas asociadas.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val cId = ciudad?.id
                ciudadParaEliminar = null
                if (cId != null) {
                    onEliminarCiudad(cId) { success, err ->
                        if (!success) localError = err
                    }
                }
            },
            onCancelar = { ciudadParaEliminar = null }
        )
    }

    // Dialog Crear / Editar Cancha
    if (ciudadIdParaNuevaCancha != null || canchaParaEditar != null) {
        val cid = ciudadIdParaNuevaCancha ?: canchaParaEditar?.ciudadId ?: ""
        var nombreCancha by remember { mutableStateOf(canchaParaEditar?.nombre ?: "") }
        var refCancha by remember { mutableStateOf(canchaParaEditar?.referencia ?: "") }

        AlertDialog(
            onDismissRequest = {
                ciudadIdParaNuevaCancha = null
                canchaParaEditar = null
            },
            title = { Text(if (canchaParaEditar == null) "Nueva Cancha" else "Editar Cancha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreCancha,
                        onValueChange = { nombreCancha = it },
                        label = { Text("Nombre de la Cancha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = refCancha,
                        onValueChange = { refCancha = it },
                        label = { Text("Referencia (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onGuardarCancha(canchaParaEditar?.id, nombreCancha, refCancha, cid) { success, err ->
                        if (success) {
                            ciudadIdParaNuevaCancha = null
                            canchaParaEditar = null
                            localError = null
                        } else {
                            localError = err
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    ciudadIdParaNuevaCancha = null
                    canchaParaEditar = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog Eliminar Cancha (RN-19: no eliminar si tiene horarios)
    if (canchaParaEliminar != null) {
        val cancha = canchaParaEliminar
        ConfirmacionDialog(
            titulo = "¿Eliminar Cancha?",
            mensaje = "¿Deseas eliminar la cancha '${cancha?.nombre}'? No se puede eliminar si posee horarios de clase programados.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val cId = cancha?.id
                canchaParaEliminar = null
                if (cId != null) {
                    onEliminarCancha(cId) { success, err ->
                        if (!success) localError = err
                    }
                }
            },
            onCancelar = { canchaParaEliminar = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        if (displayError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            localError = null
                            onLimpiarError()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { mostrarDialogNuevaCiudad = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Ciudad (A-03)")
            }
        }

        if (ciudades.isEmpty()) {
            item {
                Text(
                    text = "No hay ciudades registradas. Presiona el botón para agregar una.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(ciudades) { ciudad ->
                val canchasDeCiudad = canchas.filter { it.ciudadId == ciudad.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ciudad: ${ciudad.nombre}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row {
                                IconButton(onClick = { ciudadParaEditar = ciudad }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Ciudad", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { ciudadParaEliminar = ciudad }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Ciudad", tint = TennisRedDestructive, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (canchasDeCiudad.isEmpty()) {
                            Text(
                                text = "Sin canchas asociadas en esta ciudad.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            canchasDeCiudad.forEach { cancha ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cancha.nombre,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            if (!cancha.referencia.isNullOrBlank()) {
                                                Text(
                                                    text = "Ref: ${cancha.referencia}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Row {
                                            IconButton(onClick = { canchaParaEditar = cancha }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar Cancha", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { canchaParaEliminar = cancha }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar Cancha", tint = TennisRedDestructive, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { ciudadIdParaNuevaCancha = ciudad.id },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar Cancha a ${ciudad.nombre}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfiguracionScreen(
    uiState: AdminConfiguracionUiState,
    temaSeleccionado: TemaApp?,
    onCambiarTema: (TemaApp?) -> Unit,
    onSetMostrarConfirmarLogout: (Boolean) -> Unit,
    onLogoutConfirmado: () -> Unit
) {
    if (uiState.mostrarConfirmarLogout) {
        ConfirmacionDialog(
            titulo = "¿Cerrar Sesión de Administrador?",
            mensaje = "¿Deseas salir del panel de administración?",
            textoConfirmar = "Cerrar Sesión",
            esDestructivo = true,
            onConfirmar = onLogoutConfirmado,
            onCancelar = { onSetMostrarConfirmarLogout(false) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de Administrador (A-10)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tema de la Aplicación (A-10)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectorTema(
                        temaSeleccionado = temaSeleccionado,
                        onCambiarTema = onCambiarTema
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSetMostrarConfirmarLogout(true) },
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
                Text("Cerrar Sesión de Administrador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
