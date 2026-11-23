package com.example.agendatenis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agendatenis.model.TemaApp

/** Ambos roles editan la misma preferencia; null elimina la selección manual. */
@Composable
fun SelectorTema(temaSeleccionado: TemaApp?, onCambiarTema: (TemaApp?) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TemaApp.entries.forEach { tema ->
                val seleccionado = temaSeleccionado == tema
                FilledTonalButton(
                    onClick = { onCambiarTema(tema) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (seleccionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (tema == TemaApp.CLARO) "Claro" else "Oscuro")
                }
            }
        }
        TextButton(onClick = { onCambiarTema(null) }) {
            Text(if (temaSeleccionado == null) "Tema del dispositivo (activo)" else "Usar tema del dispositivo")
        }
    }
}
