package com.example.agendatenis.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.agendatenis.ui.components.BuscadorLista
import com.example.agendatenis.ui.components.ConfirmacionDialog
import com.example.agendatenis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    repository: AgendaTenisRepository,
    onNavigateToCoaches: () -> Unit,
    onNavigateToJugadores: () -> Unit,
    onNavigateToCatalogos: () -> Unit
) {
    val resumen = remember(repository.usuarios.collectAsState().value, repository.jugadores.collectAsState().value, repository.pagos.collectAsState().value) {
        repository.obtenerResumenAdmin()
    }

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
                            text = "Pagos Próximos / Vencidos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TennisOnYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Alerta global del sistema",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TennisOnYellow.copy(alpha = 0.8f)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Accesos Rápidos de Administración",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCoaches,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Coaches")
                }
                OutlinedButton(
                    onClick = onNavigateToCatalogos,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catálogos")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCoachesScreen(
    repository: AgendaTenisRepository,
    onNavigateToCrearCoach: () -> Unit,
    onNavigateToEditarCoach: (String) -> Unit
) {
    val usuarios by repository.usuarios.collectAsState()
    val jugadores by repository.jugadores.collectAsState()

    val coaches = usuarios.filter { it.rol == Rol.COACH }

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
                Icon(Icons.Default.Add, contentDescription = "Registrar nuevo coach")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Regla de Negocio (RN-21 & A-02): Los coaches no se eliminan para preservar el histórico de jugadores y pagos; se desactivan para bloquear su acceso.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            items(coaches) { coach ->
                val cantJugadores = jugadores.count { it.coachId == coach.id && it.estado == EstadoJugador.ACTIVO }
                val esActivo = coach.estado == EstadoUsuario.ACTIVO

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEditarCoach(coach.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esActivo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarConIniciales(nombre = coach.nombre, size = 48.dp)
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
                                Surface(
                                    color = if (esActivo) TennisGreenContainer else TennisRedContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (esActivo) "ACTIVO" else "INACTIVO",
                                        color = if (esActivo) TennisOnGreenContainer else TennisRedDestructive,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = coach.correo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$cantJugadores jugador(es) activo(s)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
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
    repository: AgendaTenisRepository,
    coachId: String?,
    onBack: () -> Unit
) {
    val usuarios by repository.usuarios.collectAsState()
    val coachExistente = remember(coachId) {
        usuarios.firstOrNull { it.id == coachId }
    }

    var nombre by remember { mutableStateOf(coachExistente?.nombre ?: "") }
    var correo by remember { mutableStateOf(coachExistente?.correo ?: "") }
    var contrasena by remember { mutableStateOf(if (coachExistente == null) "" else "") }
    var estado by remember { mutableStateOf(coachExistente?.estado ?: EstadoUsuario.ACTIVO) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (coachId == null) "Nuevo Coach" else "Editar / Desactivar Coach", fontWeight = FontWeight.Bold) },
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
                label = { Text("Nombre Completo *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text(if (coachId == null) "Contraseña inicial (min 6 car.) *" else "Nueva Contraseña (dejar en blanco para no cambiar)") },
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
                            text = "Estado de la cuenta: ${if (estado == EstadoUsuario.ACTIVO) "ACTIVO" else "DESACTIVADO"}",
                            fontWeight = FontWeight.Bold,
                            color = if (estado == EstadoUsuario.ACTIVO) TennisGreenLight else TennisRedDestructive
                        )
                        Text(
                            text = if (estado == EstadoUsuario.ACTIVO) "El coach puede iniciar sesión en la app" else "El coach no podrá iniciar sesión; sus datos se preservan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = estado == EstadoUsuario.ACTIVO,
                        onCheckedChange = { checked ->
                            estado = if (checked) EstadoUsuario.ACTIVO else EstadoUsuario.INACTIVO
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val res = repository.guardarCoachAdmin(
                        id = coachId,
                        nombre = nombre,
                        correo = correo,
                        contrasena = contrasena,
                        estado = estado
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
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJugadoresScreen(
    repository: AgendaTenisRepository,
    onNavigateToDetalleJugador: (String) -> Unit
) {
    val jugadores by repository.jugadores.collectAsState()
    val usuarios by repository.usuarios.collectAsState()
    val planes by repository.planes.collectAsState()
    val pagos by repository.pagos.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var coachFiltroId by remember { mutableStateOf<String?>(null) }
    var jugadorParaReasignar by remember { mutableStateOf<Jugador?>(null) }

    val coaches = usuarios.filter { it.rol == Rol.COACH }
    val coachesMap = coaches.associateBy { it.id }

    val jugadoresFiltrados = remember(jugadores, searchQuery, coachFiltroId) {
        jugadores.filter { j ->
            val matchesCoach = coachFiltroId == null || j.coachId == coachFiltroId
            val matchesQuery = searchQuery.isBlank() || j.nombre.contains(searchQuery, ignoreCase = true) || j.telefono.contains(searchQuery)
            matchesCoach && matchesQuery
        }.sortedBy { it.nombre }
    }

    // Modal para Reasignar Coach (A-06)
    if (jugadorParaReasignar != null) {
        val jug = jugadorParaReasignar!!
        AlertDialog(
            onDismissRequest = { jugadorParaReasignar = null },
            title = { Text("Reasignar Coach a ${jug.nombre}") },
            text = {
                Column {
                    Text(
                        text = "Selecciona el nuevo coach responsable (A-06):",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            BuscadorLista(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar jugador...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Selector rápido de filtro por coach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                        label = { Text(c.nombre.split(" ").first()) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)
            ) {
                items(jugadoresFiltrados) { jugador ->
                    val coach = coachesMap[jugador.coachId]
                    val plan = planes.firstOrNull { it.id == jugador.planId }
                    val calc = remember(jugador.id, pagos) {
                        repository.calcularEstadoPago(jugador.id)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetalleJugador(jugador.id) },
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
                            AvatarConIniciales(nombre = jugador.nombre, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = jugador.nombre,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Coach: ${coach?.nombre ?: "Sin coach"} • ${plan?.nombre ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (calc != null) {
                                    Text(
                                        text = "Vence: ${calc.proximaFecha} (${when (calc.estado) {
                                            EstadoPago.VENCIDO -> "Vencido"
                                            EstadoPago.PROXIMO -> "Próximo"
                                            EstadoPago.AL_DIA -> "Al día"
                                        }})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (calc.estado == EstadoPago.VENCIDO) TennisRedDestructive else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { jugadorParaReasignar = jugador }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reasignar Coach", tint = MaterialTheme.colorScheme.primary)
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
fun AdminCatalogosScreen(
    repository: AgendaTenisRepository
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Ciudades y Canchas", "Planes Globales")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Catálogos (A-03, A-04, A-05)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (tabIndex == 0) {
                AdminCiudadesYCanchasTab(repository = repository)
            } else {
                AdminPlanesGlobalesTab(repository = repository)
            }
        }
    }
}

@Composable
fun AdminCiudadesYCanchasTab(
    repository: AgendaTenisRepository
) {
    val ciudades by repository.ciudades.collectAsState()
    val canchas by repository.canchas.collectAsState()

    var ciudadParaEditar by remember { mutableStateOf<Ciudad?>(null) }
    var ciudadParaEliminar by remember { mutableStateOf<Ciudad?>(null) }
    var canchaParaEditar by remember { mutableStateOf<Cancha?>(null) }
    var canchaParaEliminar by remember { mutableStateOf<Cancha?>(null) }
    var ciudadIdParaNuevaCancha by remember { mutableStateOf<String?>(null) }

    var mostrarDialogNuevaCiudad by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

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
                    val res = repository.guardarCiudad(ciudadParaEditar?.id, nombreCiudad)
                    res.onSuccess {
                        mostrarDialogNuevaCiudad = false
                        ciudadParaEditar = null
                    }.onFailure { err ->
                        mensajeError = err.message
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
        ConfirmacionDialog(
            titulo = "¿Eliminar Ciudad?",
            mensaje = "¿Deseas eliminar la ciudad '${ciudadParaEliminar?.nombre}'? Recuerda que no se puede eliminar si posee canchas asociadas.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val cId = ciudadParaEliminar?.id
                ciudadParaEliminar = null
                if (cId != null) {
                    val res = repository.eliminarCiudad(cId)
                    res.onFailure { err -> mensajeError = err.message }
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
                    val res = repository.guardarCancha(canchaParaEditar?.id, nombreCancha, refCancha, cid)
                    res.onSuccess {
                        ciudadIdParaNuevaCancha = null
                        canchaParaEditar = null
                    }.onFailure { err ->
                        mensajeError = err.message
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
        ConfirmacionDialog(
            titulo = "¿Eliminar Cancha?",
            mensaje = "¿Deseas eliminar la cancha '${canchaParaEliminar?.nombre}'? No se puede eliminar si posee horarios de clase programados.",
            textoConfirmar = "Eliminar",
            esDestructivo = true,
            onConfirmar = {
                val cId = canchaParaEliminar?.id
                canchaParaEliminar = null
                if (cId != null) {
                    val res = repository.eliminarCancha(cId)
                    res.onFailure { err -> mensajeError = err.message }
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
        if (mensajeError != null) {
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
                            text = mensajeError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { mensajeError = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { mostrarDialogNuevaCiudad = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Nueva Ciudad")
            }
        }

        ciudades.forEach { ciudad ->
            val canchasDeCiudad = canchas.filter { it.ciudadId == ciudad.id }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ciudad: ${ciudad.nombre}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row {
                                IconButton(onClick = { ciudadParaEditar = ciudad }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Ciudad", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { ciudadParaEliminar = ciudad }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Ciudad", tint = TennisRedDestructive, modifier = Modifier.size(20.dp))
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

@Composable
fun AdminPlanesGlobalesTab(
    repository: AgendaTenisRepository
) {
    val planes by repository.planes.collectAsState()
    val usuarios by repository.usuarios.collectAsState()
    val coachesMap = usuarios.filter { it.rol == Rol.COACH }.associateBy { it.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        items(planes) { plan ->
            val coach = coachesMap[plan.coachId]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plan.nombre,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Coach: ${coach?.nombre ?: "Desconocido"} • ${plan.clasesPorSemana} clases/semana",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", plan.valor)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TennisGreenLight
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfiguracionScreen(
    repository: AgendaTenisRepository,
    adminId: String,
    onLogout: () -> Unit
) {
    val config = remember(adminId, repository.configs.collectAsState().value) {
        repository.obtenerConfigActual()
    }
    var tema by remember(config) { mutableStateOf(config.tema) }
    var mostrarConfirmarLogout by remember { mutableStateOf(false) }

    if (mostrarConfirmarLogout) {
        ConfirmacionDialog(
            titulo = "¿Cerrar Sesión de Administrador?",
            mensaje = "¿Deseas salir del panel de administración?",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                tema = TemaApp.CLARO
                                repository.actualizarConfig(TemaApp.CLARO, true, 3)
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
                                repository.actualizarConfig(TemaApp.OSCURO, true, 3)
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

            Spacer(modifier = Modifier.height(16.dp))

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
                Text("Cerrar Sesión de Administrador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
