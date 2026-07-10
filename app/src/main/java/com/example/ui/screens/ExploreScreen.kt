package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Report
import com.example.ui.theme.ColorPothole
import com.example.ui.theme.ColorTrash
import com.example.ui.theme.ColorWater
import com.example.ui.viewmodel.CommunityViewModel

fun getReportCoordinates(report: Report): Pair<Double, Double> {
    val base = when (report.locationColonia) {
        "Cabecera Centro" -> Pair(19.9986, -100.5186)
        "Curinhuato" -> Pair(20.0100, -100.5080)
        "San José de Hidalgo" -> Pair(19.9810, -100.5360)
        "La Purísima" -> Pair(19.9900, -100.5290)
        "Hacienda Vieja" -> Pair(19.9750, -100.5150)
        "El Guayabo" -> Pair(20.0120, -100.5280)
        "San Felipe" -> Pair(19.9850, -100.5060)
        "La Mora" -> Pair(20.0240, -100.5400)
        "San Juan de Dios" -> Pair(19.9910, -100.5020)
        "La Virgen" -> Pair(19.9710, -100.5320)
        "San Joaquín" -> Pair(20.0260, -100.5140)
        "Paso de las Ovejas" -> Pair(20.0350, -100.5480)
        "Buenavista" -> Pair(20.0420, -100.5230)
        "El Frayle" -> Pair(20.0310, -100.5590)
        "La Calera" -> Pair(20.0480, -100.5510)
        "San Ignacio" -> Pair(19.9650, -100.5220)
        "La Caja" -> Pair(19.9570, -100.5080)
        else -> Pair(19.9986, -100.5186)
    }
    // Tiny deterministic jitter so multiple reports in the same colonia are offset slightly
    val jitterLat = ((report.id * 17) % 100 - 50) / 300000.0
    val jitterLng = ((report.id * 31) % 100 - 50) / 300000.0
    return Pair(base.first + jitterLat, base.second + jitterLng)
}

@Composable
fun RealLeafletMapView(
    reports: List<Report>,
    highlightedReport: Report?,
    onSelectReport: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlData = remember(reports, highlightedReport) {
        val markersJs = StringBuilder()
        reports.forEach { report ->
            val coords = getReportCoordinates(report)
            markersJs.append("""
                {
                    id: ${report.id},
                    title: "${report.title.replace("\"", "\\\"")}",
                    description: "${report.description.replace("\"", "\\\"")}",
                    category: "${report.category.replace("\"", "\\\"")}",
                    colonia: "${report.locationColonia.replace("\"", "\\\"")}",
                    street: "${report.locationStreet.replace("\"", "\\\"")}",
                    status: "${report.status.replace("\"", "\\\"")}",
                    lat: ${coords.first},
                    lng: ${coords.second}
                },
            """.trimIndent())
        }

        val highlightCoords = highlightedReport?.let { getReportCoordinates(it) }
        val centerLat = highlightCoords?.first ?: 19.9986
        val centerLng = highlightCoords?.second ?: -100.5186
        val zoomLevel = if (highlightedReport != null) 16 else 14

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                html, body, #map {
                    margin: 0; padding: 0; width: 100%; height: 100%; background: #f4f4f5;
                }
                .leaflet-popup-content-wrapper {
                    background: #1e293b !important;
                    color: #ffffff !important;
                    border-radius: 16px;
                    font-family: system-ui, -apple-system, sans-serif;
                    border: 1px solid #334155;
                    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3), 0 4px 6px -2px rgba(0, 0, 0, 0.1) !important;
                    padding: 4px;
                }
                .leaflet-popup-tip {
                    background: #1e293b !important;
                }
                .popup-container {
                    padding: 6px;
                }
                .popup-title {
                    font-weight: bold;
                    font-size: 14px;
                    margin-bottom: 4px;
                    color: #f8fafc;
                }
                .popup-desc {
                    font-size: 11px;
                    color: #cbd5e1;
                    margin-bottom: 8px;
                    line-height: 1.4;
                }
                .popup-colonia {
                    font-size: 10px;
                    font-weight: 600;
                    color: #93c5fd;
                    margin-bottom: 6px;
                }
                .popup-status {
                    display: inline-block;
                    padding: 3px 8px;
                    border-radius: 9999px;
                    font-size: 9px;
                    font-weight: bold;
                }
                .status-reportado { background: #3b82f6; color: white; }
                .status-proceso { background: #f59e0b; color: white; }
                .status-resuelto { background: #10b981; color: white; }

                /* Custom CSS Pin Styling */
                .pin-wrapper {
                    position: relative;
                    width: 32px;
                    height: 32px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .pin-body {
                    width: 24px;
                    height: 24px;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
                    border: 1.5px solid #ffffff;
                }
                .pin-inner {
                    width: 8px;
                    height: 8px;
                    background: #ffffff;
                    border-radius: 50%;
                    transform: rotate(45deg);
                }
                /* Pulsing ripple effect for selected */
                .pin-ripple {
                    position: absolute;
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    border: 2px solid;
                    animation: ripple 1.2s infinite ease-out;
                    opacity: 0;
                    pointer-events: none;
                }
                @keyframes ripple {
                    0% { transform: scale(0.5); opacity: 0.8; }
                    100% { transform: scale(1.6); opacity: 0; }
                }
                /* Bounce effect for selected */
                .pin-bounce {
                    animation: pin-bounce-anim 0.8s infinite alternate ease-in-out;
                }
                @keyframes pin-bounce-anim {
                    0% { transform: translateY(0); }
                    100% { transform: translateY(-6px); }
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                }).setView([$centerLat, $centerLng], $zoomLevel);

                // Google Maps Roadmap Layer - interactive, high contrast, real Google Map tiles
                L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                    maxZoom: 20,
                    subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
                }).addTo(map);

                function getIconColor(category) {
                    if (category === 'Bache') return '#EF4444';
                    if (category === 'Fuga de agua') return '#3B82F6';
                    if (category === 'Basura') return '#F59E0B';
                    return '#10B981';
                }

                L.control.zoom({ position: 'bottomright' }).addTo(map);

                var reports = [
                    $markersJs
                ];

                var markers = {};

                reports.forEach(function(r) {
                    var isSelected = r.id === ${highlightedReport?.id ?: -1};
                    var pinColor = getIconColor(r.category);
                    
                    var htmlContent = '<div class="pin-wrapper' + (isSelected ? ' pin-bounce' : '') + '">';
                    if (isSelected) {
                        htmlContent += '<div class="pin-ripple" style="border-color: ' + pinColor + '"></div>';
                    }
                    htmlContent += '<div class="pin-body" style="background: ' + pinColor + '">';
                    htmlContent += '<div class="pin-inner"></div>';
                    htmlContent += '</div>';
                    htmlContent += '</div>';

                    var customIcon = L.divIcon({
                        className: 'custom-leaflet-pin',
                        html: htmlContent,
                        iconSize: [32, 32],
                        iconAnchor: [16, 32],
                        popupAnchor: [0, -32]
                    });

                    var marker = L.marker([r.lat, r.lng], {
                        icon: customIcon
                    }).addTo(map);

                    markers[r.id] = marker;

                    var statusClass = r.status === 'Reportado' ? 'status-reportado' : (r.status === 'Resuelto' ? 'status-resuelto' : 'status-proceso');
                    var popupContent = 
                        '<div class="popup-container">' +
                        '<div class="popup-title">' + r.title + '</div>' +
                        '<div class="popup-colonia">' + r.colonia + ' • ' + r.street + '</div>' +
                        '<div class="popup-desc">' + r.description + '</div>' +
                        '<span class="popup-status ' + statusClass + '">' + r.status + '</span>' +
                        '</div>';

                    marker.bindPopup(popupContent);

                    marker.on('click', function() {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onReportSelected(r.id);
                        }
                    });

                    if (isSelected) {
                        marker.openPopup();
                    }
                });

                function centerOn(lat, lng, id) {
                    map.setView([lat, lng], 16);
                    if (markers[id]) {
                        markers[id].openPopup();
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(highlightedReport) {
        if (highlightedReport != null && webViewInstance != null) {
            val coords = getReportCoordinates(highlightedReport)
            webViewInstance?.evaluateJavascript("centerOn(${coords.first}, ${coords.second}, ${highlightedReport.id});", null)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewInstance = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            isLoading = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            highlightedReport?.let {
                                val coords = getReportCoordinates(it)
                                evaluateJavascript("centerOn(${coords.first}, ${coords.second}, ${it.id});", null)
                            }
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        supportZoom()
                        builtInZoomControls = true
                        displayZoomControls = false
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                    }
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun onReportSelected(id: Int) {
                            post {
                                onSelectReport(id)
                            }
                        }
                    }, "AndroidBridge")

                    loadDataWithBaseURL("https://localhost", htmlData, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                // Keep state intact
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Cargando Mapa de Tarandacuao...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.filteredReports.collectAsState()
    val mapSelectedId by viewModel.mapSelectedReportId.collectAsState()
    val selectedColoniaFilter by viewModel.selectedColoniaFilter.collectAsState()

    // Find the report currently highlighted on the map
    val highlightedReport = remember(reports, mapSelectedId) {
        reports.find { it.id == mapSelectedId } ?: reports.firstOrNull()
    }

    // Capture first highlight if none matches or selection is out of filter bounds
    LaunchedEffect(highlightedReport) {
        if (highlightedReport != null && highlightedReport.id != mapSelectedId) {
            viewModel.selectMapReport(highlightedReport.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Explanatory Top Bar
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
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
                        text = "Ver problemas cercanos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Mapa interactivo de reportes en ${if (selectedColoniaFilter == "Todas") "tu comunidad" else selectedColoniaFilter}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Interactive Filters Row
        val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
        val categories = listOf("Todos", "Bache", "Fuga de agua", "Basura")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgColor)
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
                        .clickable { viewModel.setCategoryFilter(category) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("map_filter_$category"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }

        val mapQuery = remember(highlightedReport, selectedColoniaFilter) {
            if (highlightedReport != null) {
                "${highlightedReport.locationStreet}, ${highlightedReport.locationColonia}, Tarandacuao, Guanajuato"
            } else if (selectedColoniaFilter != "Todas") {
                "$selectedColoniaFilter, Tarandacuao, Guanajuato"
            } else {
                "Tarandacuao, Guanajuato"
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Community Map Shell
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(20.dp))
                .testTag("interactive_map_canvas")
        ) {
            RealLeafletMapView(
                reports = reports,
                highlightedReport = highlightedReport,
                onSelectReport = { id ->
                    viewModel.selectMapReport(id)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Real Map Indicator Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Text(
                        text = "Mapa Interactivo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Highlighted bottom card showing selection updates
        AnimatedContent(
            targetState = highlightedReport,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "highlight_report_transition"
        ) { report ->
            if (report != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_highlighted_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Simulated Photo Evidence Box
                            val catColor = getCategoryColor(report.category)
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(catColor.copy(alpha = 0.1f))
                                    .border(BorderStroke(1.dp, catColor.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Foto de evidencia",
                                        tint = catColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "EVIDENCIA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        color = catColor
                                    )
                                    Text(
                                        text = "FOTO #${report.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 7.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Right Detail Texts
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(report.status)
                                    Text(
                                        text = "Distancia: ~150m de ti",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${report.locationColonia} • ${report.locationStreet}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Upvote button
                            Button(
                                onClick = { viewModel.upvoteReport(report) },
                                enabled = !report.hasVoted,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apoyar (${report.votes})", style = MaterialTheme.typography.bodyMedium)
                            }

                            // View full details overlay
                            Button(
                                onClick = { viewModel.selectReport(report.id) },
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver Evidencia", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "No hay incidencias para mostrar en el mapa.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveNeighborhoodCanvas(
    reports: List<Report>,
    selectedReportId: Int,
    onSelectReport: (Int) -> Unit
) {
    // Generate constant positions for reports index on first render/change
    val nodePositions = remember(reports) {
        val random = java.util.Random(42) // Consistent seeding
        reports.map { report ->
            // Distribute points based on a responsive coordinates map
            val xFactor = 0.15f + random.nextFloat() * 0.70f
            val yFactor = 0.15f + random.nextFloat() * 0.70f
            report.id to Pair(xFactor, yFactor)
        }.toMap()
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reports) {
                detectTapGestures { offset ->
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        // Check if tap hits any node close enough (~28dp tap radius)
                        val touchRadius = 40.dp.toPx()
                        var closestReportId: Int? = null
                        var minDistance = Float.MAX_VALUE

                        nodePositions.forEach { (id, coords) ->
                            val nodeX = coords.first * canvasSize.width
                            val nodeY = coords.second * canvasSize.height
                            val dist = Math.hypot((offset.x - nodeX).toDouble(), (offset.y - nodeY).toDouble()).toFloat()

                            if (dist < touchRadius && dist < minDistance) {
                                minDistance = dist
                                closestReportId = id
                            }
                        }

                        closestReportId?.let { onSelectReport(it) }
                    }
                }
            }
    ) {
        canvasSize = size
        val w = size.width
        val h = size.height

        // 1. Draw grid / streets simulation background
        val path = Path()
        // Horizontal main avenue
        path.moveTo(0f, h * 0.35f)
        path.lineTo(w, h * 0.35f)
        // Secondary horizontal
        path.moveTo(0f, h * 0.72f)
        path.lineTo(w, h * 0.72f)
        // Vertical avenue
        path.moveTo(w * 0.3f, 0f)
        path.lineTo(w * 0.3f, h)
        // Secondary vertical
        path.moveTo(w * 0.75f, 0f)
        path.lineTo(w * 0.75f, h)

        // Diagonal street
        path.moveTo(w * 0.1f, h * 0.9f)
        path.lineTo(w * 0.9f, h * 0.1f)

        drawPath(
            path = path,
            color = Color.LightGray.copy(alpha = 0.25f),
            style = Stroke(width = 24.dp.toPx())
        )
        drawPath(
            path = path,
            color = Color.LightGray.copy(alpha = 0.45f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Central community park
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.12f),
            radius = w * 0.15f,
            center = Offset(w * 0.52f, h * 0.55f)
        )
        // Central park border
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.3f),
            radius = w * 0.15f,
            center = Offset(w * 0.52f, h * 0.55f),
            style = Stroke(width = 1.dp.toPx())
        )

        // Text map labels
        // We can draw simple textual labels if text capabilities are configured,
        // but drawing beautiful nodes represents the user pins perfectly!

        // 2. Draw Report Node Pins
        nodePositions.forEach { (id, coords) ->
            val report = reports.find { it.id == id } ?: return@forEach
            val nodeX = coords.first * w
            val nodeY = coords.second * h

            val color = when (report.category) {
                "Bache" -> ColorPothole
                "Fuga de agua" -> ColorWater
                "Basura" -> ColorTrash
                else -> Color.Gray
            }

            val isSelected = id == selectedReportId

            // Interactive Outer Ripple Glowing Indicator
            if (isSelected) {
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = 24.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
                drawCircle(
                    color = color.copy(alpha = 0.45f),
                    radius = 16.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            } else {
                drawCircle(
                    color = color.copy(alpha = 0.15f),
                    radius = 12.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            }

            // Core Solid Pin Node
            drawCircle(
                color = color,
                radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )

            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = if (isSelected) 3f.dp.toPx() else 2f.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.borderStroke(width: androidx.compose.ui.unit.Dp, color: Color): Modifier {
    return this.border(BorderStroke(width, color), RoundedCornerShape(20.dp))
}
