package com.despacho.palletscanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.despacho.palletscanner.viewmodels.TesteadorViewModel
import com.despacho.palletscanner.scanner.BarcodeScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesteadorScreen(
    viewModel: TesteadorViewModel,
    onNavigateBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val palletNumber by viewModel.palletNumber.collectAsState()
    val palletInfoResponse by viewModel.palletInfoResponse.collectAsState()
    val palletDeletionResult by viewModel.palletDeletionResult.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    var showCamera by remember { mutableStateOf(false) }

    // Dialog de confirmacion de eliminacion
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmDialog() },
            title = { Text("Confirmar Eliminacion") },
            text = {
                Text(
                    "Esta seguro que desea eliminar el pallet $palletNumber?\n\n" +
                    "Se eliminara de las siguientes tablas:\n" +
                    "- Palet_Listos\n" +
                    "- Cabecera_Palet\n" +
                    "- Detalles_Lecturas\n" +
                    "- DETALLE_PALLETIZADOR\n" +
                    "- PALLETIZADOR\n\n" +
                    "Esta accion NO se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeletePallet() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissDeleteConfirmDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Escaner de camara
    if (showCamera) {
        BarcodeScanner(
            onBarcodeDetected = { barcode ->
                viewModel.updatePalletNumber(barcode)
                viewModel.searchPallet()
            },
            onClose = { showCamera = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trazabilidad - Testeador") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearResults() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estado de conexion
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (connectionState)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = if (connectionState) "Conectado al servidor" else "Desconectado",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Seccion de busqueda
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Consultar Pallet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = palletNumber,
                            onValueChange = { viewModel.updatePalletNumber(it) },
                            label = { Text("Numero de Pallet") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.searchPallet() },
                                enabled = palletNumber.isNotBlank() && !isSearching && connectionState,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Buscar")
                            }

                            OutlinedButton(
                                onClick = { showCamera = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Escanear")
                            }
                        }
                    }
                }
            }

            // Mensajes de error
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            // Mensaje de exito
            successMessage?.let { success ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = success,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF2E7D32)
                            )
                            TextButton(onClick = { viewModel.dismissSuccess() }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            // Resultados de busqueda
            palletInfoResponse?.let { response ->
                // Info del pallet
                if (response.incompleto) {
                    // Pallet incompleto
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "PALLET INCOMPLETO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                response.mensaje?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                                }
                                response.tablasConRegistros?.let { tablas ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tablas con registros: ${tablas.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Pallet completo
                    response.pallet?.let { pallet ->
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Informacion del Pallet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    InfoRow("Pallet", pallet.numeroPallet)
                                    InfoRow("Variedad", pallet.variedad)
                                    InfoRow("Calibre", pallet.calibre)
                                    InfoRow("Embalaje", pallet.embalaje)
                                    InfoRow("Total Cajas", pallet.numeroDeCajas.toString())

                                    // Estado de validacion
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val validationColor = when {
                                        response.estadoValidacion.contains("OK", ignoreCase = true) -> Color(0xFF2E7D32)
                                        response.estadoValidacion.contains("DISCREPANCIA", ignoreCase = true) -> Color(0xFFE65100)
                                        else -> Color.Gray
                                    }
                                    Text(
                                        text = response.estadoValidacion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = validationColor
                                    )
                                }
                            }
                        }
                    }

                    // Titulo de lotes
                    if (response.lotes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lotes (${response.lotes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Lotes directamente como items del LazyColumn
                        items(response.lotes) { lote ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (lote.esMinoritario)
                                        Color(0xFFFFEBEE)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (lote.esMinoritario) {
                                        Text(
                                            text = "MINORITARIO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Cuartel: ${lote.codigoCuartel}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Predio: ${lote.nombrePredio}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Productor: ${lote.nombreProductor}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "CSG: ${lote.csgPredio}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${lote.cantidadCajas} cajas",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Cal: ${lote.calibreLote}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Emb: ${lote.embalajeLote}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Var: ${lote.variedadLote}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Boton eliminar
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.requestDeletePallet() },
                        enabled = !isDeleting && connectionState,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Eliminar Pallet")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
