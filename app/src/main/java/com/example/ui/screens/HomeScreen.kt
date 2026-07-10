package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Report
import com.example.ui.theme.*
import com.example.ui.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.filteredReports.collectAsState()
    val allReportsRaw by viewModel.reports.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedColonia by viewModel.selectedColoniaFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedReportDetails by viewModel.selectedReport.collectAsState()
    val isAuthMode by viewModel.isAuthorityMode.collectAsState()

    val roadSections by viewModel.roadSections.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0 = Incidencias, 1 = Inventario Vial

    val categories = listOf("Todos", "Bache", "Fuga de agua", "Basura")
    val colonias = listOf(
        "Todas", "Cabecera Centro", "Curinhuato", "San José de Hidalgo", 
        "La Purísima", "Hacienda Vieja", "El Guayabo", "San Felipe", 
        "La Mora", "San Juan de Dios", "La Virgen", "San Joaquín",
        "Paso de las Ovejas", "Buenavista", "El Frayle", "La Calera",
        "San Ignacio", "La Caja"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-navigation: Incidencias vs Inventario Vial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Incidencias",
                        color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Red Vial (52.2 km)",
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTab == 0) {
                // Quick Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar baches, fugas o calles...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stat Counter Row (or Authority Banner)
                if (isAuthMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("authority_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Buzón de Gestión de Autoridades",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Elige cualquier reporte para asignar cuadrillas de obras, cambiar estados y notificar a los ciudadanos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    CommunityStatCounter(allReportsRaw)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category select row
                Text(
                    text = "Filtrar por Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategoryFilter(cat) },
                            label = { Text(cat) },
                            leadingIcon = {
                                val icon = getCategoryIcon(cat)
                                if (icon != null) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            modifier = Modifier.testTag("chip_cat_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Colonia Select Row
                Text(
                    text = "Comunidad o Barrio (Tarandacuao)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colonias) { col ->
                        val isSelected = col == selectedColonia
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setColoniaFilter(col) },
                            label = { Text(col) },
                            leadingIcon = {
                                Icon(
                                    if (isSelected) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("chip_col_$col")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Reports List
                if (reports.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "No hay problemas",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡Excelente, limpia de reportes!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No hay incidencias que coincidan con los filtros seleccionados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("reports_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onCardClick = { viewModel.selectReport(report.id) },
                                onUpvoteClick = { viewModel.upvoteReport(report) }
                            )
                        }
                    }
                }
            } else {
                // RED VIAL INVENTORY TAB
                val totalLength = roadSections.sumOf { it.lengthKm }
                val stateCounts = roadSections.groupBy { it.conservationState }.mapValues { it.value.size }
                val criticoCount = stateCounts["Crítico"] ?: 0
                val maloCount = stateCounts["Malo"] ?: 0
                val regularCount = stateCounts["Regular"] ?: 0
                val buenoCount = stateCounts["Bueno"] ?: 0
                val excelenteCount = stateCounts["Excelente"] ?: 0

                // Card explaining what the Road Inventory is used for
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "¿Para qué sirve la Red Vial?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este inventario municipal de Tarandacuao es una herramienta técnica que sirve para:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Planificar el mantenimiento preventivo oportuno en tramos antes de que sufran deterioro grave.\n" +
                                   "• Gestionar formalmente recursos ante el Estado (SICOM) o Federación (SICT) según la competencia de cada vía.\n" +
                                   "• Canalizar las cuadrillas de obras públicas asociando las incidencias de los ciudadanos con tramos específicos para optimizar recursos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card showing physical stats of the road network
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Estado Físico de la Red Carretera",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total de Red Analizada: ${String.format(java.util.Locale.getDefault(), "%.1f", totalLength)} km",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Interactive Graph bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val totalRoads = roadSections.size.toFloat()
                            val goodPortion = (excelenteCount + buenoCount) / totalRoads
                            val regularPortion = regularCount / totalRoads
                            val badPortion = (maloCount + criticoCount) / totalRoads
                            
                            if (goodPortion > 0) {
                                Box(modifier = Modifier.weight(goodPortion).fillMaxHeight().background(Color(0xFF10B981)))
                            }
                            if (regularPortion > 0) {
                                Box(modifier = Modifier.weight(regularPortion).fillMaxHeight().background(Color(0xFFF59E0B)))
                            }
                            if (badPortion > 0) {
                                Box(modifier = Modifier.weight(badPortion).fillMaxHeight().background(Color(0xFFEF4444)))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text("Buen Estado: ${excelenteCount + buenoCount} tramos", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Text("Regular: $regularCount tramos", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Text("Crítico: ${maloCount + criticoCount} tramos", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(roadSections, key = { it.id }) { road ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = road.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val (badgeBg, badgeFg) = when (road.responsible) {
                                        "Federal (SICT)" -> Pair(Color(0xFFDBEAFE), Color(0xFF1E40AF))
                                        "Estatal (SICOM)" -> Pair(Color(0xFFF3E8FF), Color(0xFF6B21A8))
                                        else -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = road.responsible,
                                            color = badgeFg,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Longitud: ${road.lengthKm} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Estado: ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val (stateColor, stateText) = when (road.conservationState) {
                                            "Excelente" -> Pair(Color(0xFF10B981), "Excelente")
                                            "Bueno" -> Pair(Color(0xFF34D399), "Bueno")
                                            "Regular" -> Pair(Color(0xFFF59E0B), "Regular")
                                            "Malo" -> Pair(Color(0xFFF97316), "Malo")
                                            "Crítico" -> Pair(Color(0xFFEF4444), "Crítico")
                                            else -> Pair(Color.Gray, "Desconocido")
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(stateColor.copy(alpha = 0.15f))
                                                .border(BorderStroke(1.dp, stateColor), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stateText,
                                                color = stateColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val (actionText, priorityText, priorityColor) = when (road.conservationState) {
                                        "Excelente" -> Triple("Mantenimiento Preventivo", "Prioridad: Baja", Color(0xFF10B981))
                                        "Bueno" -> Triple("Limpieza y Sello", "Prioridad: Baja", Color(0xFF34D399))
                                        "Regular" -> Triple("Bacheo Superficial", "Prioridad: Media", Color(0xFFF59E0B))
                                        "Malo" -> Triple("Bacheo y Reencarpetado", "Prioridad: Alta", Color(0xFFF97316))
                                        "Crítico" -> Triple("Reconstrucción Total", "Prioridad: Crítica", Color(0xFFEF4444))
                                        else -> Triple("Diagnóstico Requerido", "Prioridad: Pendiente", Color.Gray)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = actionText,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = priorityText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Ajustar Estado (Simulador):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val statesList = listOf("Excelente", "Bueno", "Regular", "Malo", "Crítico")
                                    statesList.forEach { state ->
                                        val isSelected = road.conservationState == state
                                        val buttonColor = when (state) {
                                            "Excelente" -> Color(0xFF10B981)
                                            "Bueno" -> Color(0xFF34D399)
                                            "Regular" -> Color(0xFFF59E0B)
                                            "Malo" -> Color(0xFFF97316)
                                            "Crítico" -> Color(0xFFEF4444)
                                            else -> Color.Gray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) buttonColor else buttonColor.copy(alpha = 0.08f))
                                                .border(
                                                    BorderStroke(1.dp, if (isSelected) buttonColor else buttonColor.copy(alpha = 0.3f)),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { viewModel.updateRoadState(road.id, state) }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = state.take(3),
                                                color = if (isSelected) Color.White else buttonColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        val (col, str) = when (road.id) {
                                            1 -> Pair("San Felipe", "Carretera Federal 51")
                                            2 -> Pair("San Felipe", "Carretera Estatal Jerécuaro-Tarandacuao")
                                            3 -> Pair("La Mora", "Camino La Mora - Paso de las Ovejas")
                                            4 -> Pair("El Guayabo", "Acceso Principal El Guayabo")
                                            5 -> Pair("San Felipe", "Camino San Felipe")
                                            6 -> Pair("San Joaquín", "Ramal a San Joaquín")
                                            7 -> Pair("Curinhuato", "Camino Tarandacuao - Curinhuato")
                                            8 -> Pair("San José de Hidalgo", "Ramal a San José de Hidalgo")
                                            9 -> Pair("La Virgen", "Ramal a La Virgen")
                                            10 -> Pair("Cabecera Centro", "Acceso Principal Tarandacuao")
                                            11 -> Pair("San Juan de Dios", "Camino Tarandacuao - San Juan de Dios")
                                            12 -> Pair("Hacienda Vieja", "Camino Hacienda Vieja - Tarandacuao")
                                            13 -> Pair("Hacienda Vieja", "Ramal a Hacienda Vieja")
                                            14 -> Pair("La Purísima", "Camino Hacienda Vieja - La Purísima")
                                            15 -> Pair("La Purísima", "Camino Tarandacuao - La Purísima")
                                            else -> Pair("Cabecera Centro", road.name)
                                        }
                                        viewModel.setPrefilledValues(col, str)
                                        viewModel.navigateTo("CREATE")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Reportar bache o fuga en esta vía",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Animated Modal Overlay for Report Details
        AnimatedVisibility(
            visible = selectedReportDetails != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedReportDetails?.let { report ->
                ReportDetailsOverlay(
                    report = report,
                    isAuthMode = isAuthMode,
                    onClose = { viewModel.selectReport(null) },
                    onUpvote = { viewModel.upvoteReport(report) },
                    onAdvanceStatus = { viewModel.advanceReportStatus(report) },
                    onUpdateByAuthority = { reportId, status, assigned, note ->
                        viewModel.updateReportByAuthority(reportId, status, assigned, note)
                    }
                )
            }
        }
    }
}

@Composable
fun CommunityStatCounter(reports: List<Report>) {
    val total = reports.size
    val pending = reports.count { it.status == "Reportado" }
    val solved = reports.count { it.status == "Resuelto" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "Total casos", count = total.toString(), color = MaterialTheme.colorScheme.primary)
        VerticalDivider(modifier = Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatItem(label = "Reportados", count = pending.toString(), color = ColorPothole)
        VerticalDivider(modifier = Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatItem(label = "Resueltos", count = solved.toString(), color = ColorOther)
    }
}

@Composable
fun StatItem(label: String, count: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportCard(
    report: Report,
    onCardClick: () -> Unit,
    onUpvoteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("report_item_${report.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Photo representation
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getCategoryColor(report.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                // Pre-draw standard graphic representation for evidence
                val (vector, color) = getCategoryResourceBundle(report.category)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = vector,
                        contentDescription = report.category,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = report.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    StatusBadge(report.status)

                    // Date
                    val dateFormatted = remember(report.timestamp) {
                        try {
                            SimpleDateFormat("dd/MMM HH:mm", Locale.getDefault()).format(Date(report.timestamp))
                        } catch (e: Exception) {
                            "Reciente"
                        }
                    }
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${report.locationColonia} • ${report.locationStreet}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Upvote Counter Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Votes badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = "Apoyos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${report.votes} apoyos",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Crew assignment badge if set
                        if (report.assignedTo != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = "Asignado",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = report.assignedTo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 100.dp)
                                )
                            }
                        }
                    }

                    // Upvote button
                    IconButton(
                        onClick = onUpvoteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("upvote_button_${report.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (report.hasVoted) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (report.hasVoted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !report.hasVoted
                    ) {
                        Icon(
                            imageVector = if (report.hasVoted) Icons.Filled.Check else Icons.Filled.ThumbUp,
                            contentDescription = "Apoyar reporte",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Reportado" -> ColorPothole.copy(alpha = 0.15f) to ColorPothole
        "En proceso" -> ColorWater.copy(alpha = 0.15f) to ColorWater
        "Resuelto" -> ColorOther.copy(alpha = 0.15f) to ColorOther
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
fun ReportDetailsOverlay(
    report: Report,
    isAuthMode: Boolean,
    onClose: () -> Unit,
    onUpvote: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onUpdateByAuthority: (Int, String, String?, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .testTag("report_details_overlay"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(report.status)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = getCategoryColor(report.category)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_details_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Photo Evidence Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(getCategoryColor(report.category).copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                val (vector, color) = getCategoryResourceBundle(report.category)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.6f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EVIDENCIA FOTOGRÁFICA REGISTRADA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "ID reporte #${report.id} • Guardado en base de datos local",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Colonia o Pueblo: ${report.locationColonia}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calle: ${report.locationStreet}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Descripción del problema:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log / Case update info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Seguimiento: ${if (report.status == "Reportado") "Reportado y en Lista de Espera" else if (report.status == "En proceso") "Asignado a Técnicos Municipales" else "Caso Concluido por el Comité"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Dynamics for Authority Control vs citizen view
            if (isAuthMode) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "PANEL DE ASIGNACIÓN Y GESTIÓN (AUTORIDAD)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Status dropdown/pill selecting
                Text(
                    text = "1. Cambiar Estado del Reporte:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var selectedStatus by remember(report) { mutableStateOf(report.status) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf("Reportado", "En proceso", "Resuelto")
                    statusOptions.forEach { statusOption ->
                        val isSel = selectedStatus == statusOption
                        val activeColor = when(statusOption) {
                            "Reportado" -> ColorPothole
                            "En proceso" -> ColorWater
                            else -> ColorOther
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(1.5.dp, if (isSel) activeColor else MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(12.dp))
                                .clickable { selectedStatus = statusOption }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = statusOption,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Assigned Crew
                Text(
                    text = "2. Personal / Cuadrilla Asignada:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var assignedCrewInput by remember(report) { mutableStateOf(report.assignedTo ?: "") }
                
                OutlinedTextField(
                    value = assignedCrewInput,
                    onValueChange = { assignedCrewInput = it },
                    placeholder = { Text("Ej. Cuadrilla Centro, Obras Sector 3...") },
                    modifier = Modifier.fillMaxWidth().testTag("assign_crew_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )

                // Fast Crew Suggestion chips!
                val crewSuggestions = when (report.category) {
                    "Bache" -> listOf("Cuadrilla 1 - Bacheo Centro", "Pavimentación Sector Norte")
                    "Fuga de agua" -> listOf("Sistema de Aguas S-2", "Plomeros Municipales")
                    "Basura" -> listOf("Recolectora C-4", "Servicios de Limpieza")
                    else -> listOf("Cuadrilla Obras Ayuntamiento", "Mantenimiento General")
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    crewSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(8.dp))
                                .clickable { assignedCrewInput = suggestion }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ $suggestion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom citizen note
                Text(
                    text = "3. Nota de Avance (Se envía al Ciudadano):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var progressNoteInput by remember(report) { mutableStateOf("") }
                
                OutlinedTextField(
                    value = progressNoteInput,
                    onValueChange = { progressNoteInput = it },
                    placeholder = { Text("Ej. El personal de obras iniciará labores mañana en la mañana.") },
                    modifier = Modifier.fillMaxWidth().testTag("authority_progress_note"),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Action
                Button(
                    onClick = {
                        onUpdateByAuthority(report.id, selectedStatus, assignedCrewInput, progressNoteInput)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_authority_update"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar y Notificar Vecinos", fontWeight = FontWeight.Bold)
                }
            } else {
                // Citizen view - showing assigned crew if present, and standard upvote/sim buttons
                report.assignedTo?.let { assigned ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = "Asignado",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Personal de Atención Asignado:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = assigned,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons for citizen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAdvanceStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_flow_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simular Avance",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onUpvote,
                        enabled = !report.hasVoted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (report.hasVoted) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (report.hasVoted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_upvote_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (report.hasVoted) Icons.Filled.Check else Icons.Filled.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (report.hasVoted) "Apoyado" else "Me Afecta",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Global category icons helper
fun getCategoryIcon(cat: String): ImageVector? {
    return when (cat) {
        "Bache" -> Icons.Default.Build
        "Fuga de agua" -> Icons.Default.PlayArrow // representing flowing fluids
        "Basura" -> Icons.Default.Delete
        else -> null
    }
}

fun getCategoryColor(cat: String): Color {
    return when (cat) {
        "Bache" -> ColorPothole
        "Fuga de agua" -> ColorWater
        "Basura" -> ColorTrash
        else -> ColorOther
    }
}

fun getCategoryResourceBundle(cat: String): Pair<ImageVector, Color> {
    return when (cat) {
        "Bache" -> Icons.Filled.Warning to ColorPothole
        "Fuga de agua" -> Icons.Filled.Refresh to ColorWater // In compose material icons, Refresh or Water drop is best. Let's use Refresh for flow water
        "Basura" -> Icons.Filled.DeletePin to ColorTrash // Or customized
        else -> Icons.Filled.Info to ColorOther
    }
}

// Custom mock for DeletePin fallback
val Icons.Filled.DeletePin: ImageVector
    get() = Icons.Default.Delete
