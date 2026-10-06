package com.example.agendatenis.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agendatenis.model.DiaSemana
import com.example.agendatenis.model.RecordatorioItem
import com.example.agendatenis.ui.theme.*

/**
 * Avatar circular con iniciales del nombre
 */
@Composable
fun AvatarConIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "J" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

/**
 * Fila con los días L M X J V S D resaltados según actividad
 */
@Composable
fun DiasSemanaResaltados(
    diasActivos: Set<DiaSemana>,
    modifier: Modifier = Modifier,
    onDiaClick: ((DiaSemana) -> Unit)? = null,
    itemSize: Dp = 32.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DiaSemana.entries.forEach { dia ->
            val estaActivo = diasActivos.contains(dia)
            val clickableModifier = if (onDiaClick != null) {
                Modifier.clickable { onDiaClick(dia) }
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(
                        if (estaActivo) TennisGreenLight else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .then(clickableModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dia.abrev,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (estaActivo) FontWeight.Bold else FontWeight.Normal,
                        color = if (estaActivo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Tarjeta amarilla destacada para "Registrar pago"
 */
@Composable
fun TarjetaRegistrarPago(
    jugadorNombre: String,
    montoPlan: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TennisYellowAction
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Registrar pago",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TennisOnYellow
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (montoPlan != null) "$jugadorNombre - Cuota sugerida: $${String.format("%.2f", montoPlan)}" else jugadorNombre,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TennisOnYellow.copy(alpha = 0.85f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = TennisOnYellow,
                    contentColor = TennisYellowAction
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(text = "Cobrar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Banner superior: "Pago registrado exitosamente"
 */
@Composable
fun BannerPagoRegistrado(
    mensaje: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = TennisGreenLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar banner",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Campana con contador numérico de recordatorios próximos/vencidos
 */
@Composable
fun CampanaConContador(
    contador: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Recordatorios",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(26.dp)
            )
            if (contador > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(TennisRedDestructive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (contador > 9) "9+" else contador.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Buscador de texto para listas
 */
@Composable
fun BuscadorLista(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/**
 * Diálogo de confirmación accesible
 */
@Composable
fun ConfirmacionDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "Confirmar",
    esDestructivo: Boolean = false,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = mensaje, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = if (esDestructivo) {
                    ButtonDefaults.buttonColors(containerColor = TennisRedDestructive, contentColor = Color.White)
                } else {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                }
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Modal dialog para visualizar los recordatorios activos
 */
@Composable
fun RecordatoriosDialog(
    recordatorios: List<RecordatorioItem>,
    onRecordatorioClick: (jugadorId: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recordatorios de Pago", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            if (recordatorios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes pagos próximos ni vencidos pendientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    recordatorios.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onDismiss()
                                    onRecordatorioClick(item.jugadorId)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.esVencido) TennisRedContainer else TennisYellowActionContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.jugadorNombre,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.esVencido) TennisRedDestructive else TennisOnYellow
                                    )
                                    Text(
                                        text = if (item.esVencido) "VENCIDO" else if (item.esMismoDia) "VENCE HOY" else "PRÓXIMO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.esVencido) TennisRedDestructive else TennisOnYellow
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.mensaje,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.esVencido) TennisRedDestructive else TennisOnYellow
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}
