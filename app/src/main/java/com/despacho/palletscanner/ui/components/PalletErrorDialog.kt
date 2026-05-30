package com.despacho.palletscanner.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Diálogo para errores enviados desde AplicacionDespacho vía SignalR (PalletError).
 * Muestra mensajes multilínea de validación de embalaje / gestión de pesos.
 */
@Composable
fun PalletErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val title = resolveErrorTitle(message)
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Entendido")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

private fun resolveErrorTitle(message: String): String {
    val normalized = message.lowercase()
    return when {
        normalized.contains("no existe en el catálogo") ||
            normalized.contains("no existe en gestión") -> "Embalaje no encontrado"

        normalized.contains("no tiene valores guardados") ||
            normalized.contains("peso unitario es cero") ||
            normalized.contains("peso unitario") && normalized.contains("cero") -> "Datos incompletos en Gestión de Pesos"

        normalized.contains("ficha técnica") -> "Ficha técnica requerida (PC)"

        normalized.contains("sin conexión") -> "Sin conexión"

        else -> "Validación de embalaje"
    }
}
