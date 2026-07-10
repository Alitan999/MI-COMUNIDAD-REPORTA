package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorPothole
import com.example.ui.theme.ColorTrash
import com.example.ui.theme.ColorWater
import com.example.ui.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val prefilledCol by viewModel.prefilledColonia.collectAsState()
    val prefilledStr by viewModel.prefilledStreet.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Bache") }
    var selectedColonia by remember(prefilledCol) { mutableStateOf(prefilledCol.ifBlank { "Cabecera Centro" }) }
    var street by remember(prefilledStr) { mutableStateOf(prefilledStr) }

    // Evidence simulator state
    var isSimulatedCaptureActive by remember { mutableStateOf(false) }
    var attachPhotoType by remember { mutableStateOf("default_bache") }

    val categories = listOf("Bache", "Fuga de agua", "Basura")
    val colonias = listOf(
        "Cabecera Centro",
        "Curinhuato",
        "San José de Hidalgo",
        "La Purísima",
        "Hacienda Vieja",
        "El Guayabo",
        "San Felipe",
        "La Mora",
        "San Juan de Dios",
        "La Virgen",
        "San Joaquín",
        "Paso de las Ovejas",
        "Buenavista",
        "El Frayle",
        "La Calera",
        "San Ignacio",
        "La Caja"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("create_report_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Crear Nuevo Reporte",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        // Step 1: Broad Category Selector
        Text(
            text = "1. Selecciona la categoría del problema",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                val color = getCategoryColor(cat)
                val icon = when (cat) {
                    "Bache" -> Icons.Default.Warning
                    "Fuga de agua" -> Icons.Default.Refresh
                    "Basura" -> Icons.Default.Delete
                    else -> Icons.Default.Info
                }

                Surface(
                    onClick = {
                        selectedCategory = cat
                        // Set standard evidence mockup type
                        attachPhotoType = when (cat) {
                            "Bache" -> "bache_1"
                            "Fuga de agua" -> "fuga_1"
                            "Basura" -> "basura_1"
                            else -> "other_1"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_cat_$cat"),
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, if (isSelected) color else MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = if (isSelected) 0.dp else 1.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Step 2: Evidence attachment (Simulated Media capture)
        Text(
            text = "2. Añadir evidencia fotográfica",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .placeholderBorder(RoundedCornerShape(16.dp), MaterialTheme.colorScheme.outlineVariant)
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    isSimulatedCaptureActive = true
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = "Simular Foto",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isSimulatedCaptureActive) "¡EVIDENCIA CAPTURADA CON ÉXITO!" else "Tomar Foto o Seleccionar Archivo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSimulatedCaptureActive) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isSimulatedCaptureActive) "Simulador de dispositivo listo (${selectedCategory})" else "Se cargará una imagen representativa para tu reporte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Step 3: Location Inputs
        Text(
            text = "3. Ubicación del problema",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Colonia chip bar selection
        Column {
            Text(
                text = "Selecciona Colonia o Pueblo de pertenencia:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(colonias) { col ->
                    val isColSelected = col == selectedColonia
                    FilterChip(
                        selected = isColSelected,
                        onClick = { selectedColonia = col },
                        label = { Text(col) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("create_col_$col")
                    )
                }
            }
        }

        // Exact street address text field
        OutlinedTextField(
            value = street,
            onValueChange = { street = it },
            placeholder = { Text("Calle, número aproximado o entre calles") },
            label = { Text("Dirección Exacta") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_street_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Step 4: Describe Issue text fields
        Text(
            text = "4. Detalles del Reporte",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("E.g., Bache hondo frente a panadería") },
            label = { Text("Título Corto del Reporte") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_title_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
 
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Describe el problema para ayudar al Comité de Obras y a vecinos a entender la urgencia...") },
            label = { Text("Descripción Detallada") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("create_desc_input"),
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Submit form button
        Button(
            onClick = {
                viewModel.createReport(
                    title = title,
                    description = description,
                    category = selectedCategory,
                    colonia = selectedColonia,
                    street = street,
                    photoType = attachPhotoType
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_report_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Done, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enviar Reporte Oficial",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// Border simulator custom extension
private fun Modifier.placeholderBorder(shape: RoundedCornerShape, color: Color): Modifier {
    return this.border(androidx.compose.foundation.BorderStroke(1.5.dp, color), shape)
}

@Composable
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}
