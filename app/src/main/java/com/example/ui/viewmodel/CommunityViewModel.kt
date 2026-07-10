package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Notification
import com.example.data.model.Proposal
import com.example.data.model.Report
import com.example.data.repository.CommunityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommunityViewModel(
    application: Application,
    private val repository: CommunityRepository
) : AndroidViewModel(application) {

    // Main states
    val reports: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val proposals: StateFlow<List<Proposal>> = repository.allProposals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User interaction filters
    private val _selectedCategoryFilter = MutableStateFlow("Todos")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedColoniaFilter = MutableStateFlow("Todas")
    val selectedColoniaFilter: StateFlow<String> = _selectedColoniaFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active detail views
    private val _selectedReportId = MutableStateFlow<Int?>(null)
    val selectedReport: StateFlow<Report?> = _selectedReportId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getReportById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Map screen selected report node (for map pin selection)
    private val _mapSelectedReportId = MutableStateFlow<Int?>(null)
    val mapSelectedReportId: StateFlow<Int?> = _mapSelectedReportId.asStateFlow()

    // Screen views: "HOME" (Dashboard/Todos), "EXPLORE" (Mapa/Cercanos), "CREATE" (Reportar), "PROPOSALS" (Votaciones), "NOTIFICATIONS" (Historial)
    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Authority integration mode
    private val _isAuthorityMode = MutableStateFlow(false)
    val isAuthorityMode: StateFlow<Boolean> = _isAuthorityMode.asStateFlow()

    // Road Section Inventory for Tarandacuao
    private val _roadSections = MutableStateFlow<List<RoadSection>>(emptyList())
    val roadSections: StateFlow<List<RoadSection>> = _roadSections.asStateFlow()

    val prefilledStreet = MutableStateFlow("")
    val prefilledColonia = MutableStateFlow("")

    fun setPrefilledValues(colonia: String, street: String) {
        prefilledColonia.value = colonia
        prefilledStreet.value = street
    }

    fun clearPrefilledValues() {
        prefilledColonia.value = ""
        prefilledStreet.value = ""
    }

    fun updateRoadState(roadId: Int, newState: String) {
        _roadSections.value = _roadSections.value.map {
            if (it.id == roadId) it.copy(conservationState = newState) else it
        }
    }

    fun setAuthorityMode(enabled: Boolean) {
        _isAuthorityMode.value = enabled
    }

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfNeeded()
        }
        _roadSections.value = listOf(
            RoadSection(1, "Carretera Federal 51 (Maravatío – Tarandacuao – Acámbaro)", 11.87, "Federal (SICT)", "Regular"),
            RoadSection(2, "Jerécuaro – Tarandacuao (E177)", 5.67, "Estatal (SICOM)", "Malo"),
            RoadSection(3, "La Mora – Paso de las Ovejas – Entronque a Tarandacuao", 7.63, "Municipal", "Regular"),
            RoadSection(4, "Acceso a El Guayabo", 2.86, "Municipal", "Malo"),
            RoadSection(5, "San Felipe – Entronque Maravatío–Acámbaro", 4.25, "Municipal", "Crítico"),
            RoadSection(6, "Ramal a San Joaquín", 1.14, "Municipal", "Bueno"),
            RoadSection(7, "Tarandacuao – Curinhuato", 3.39, "Municipal", "Bueno"),
            RoadSection(8, "Ramal a San José de Hidalgo", 1.70, "Municipal", "Regular"),
            RoadSection(9, "Ramal a La Virgen", 2.06, "Municipal", "Regular"),
            RoadSection(10, "Acceso a Tarandacuao", 2.25, "Municipal", "Excelente"),
            RoadSection(11, "Tarandacuao – San Juan de Dios", 2.63, "Municipal", "Regular"),
            RoadSection(12, "Hacienda Vieja – Tarandacuao", 1.14, "Municipal", "Regular"),
            RoadSection(13, "Ramal a Hacienda Vieja", 1.01, "Municipal", "Regular"),
            RoadSection(14, "Hacienda Vieja – La Purísima", 1.84, "Municipal", "Malo"),
            RoadSection(15, "Tarandacuao – La Purísima", 2.76, "Municipal", "Regular")
        )
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        // Clear selected report details when navigating
        if (screen != "HOME") {
            _selectedReportId.value = null
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setColoniaFilter(colonia: String) {
        _selectedColoniaFilter.value = colonia
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectReport(id: Int?) {
        _selectedReportId.value = id
    }

    fun selectMapReport(id: Int?) {
        _mapSelectedReportId.value = id
    }

    // Filter reports
    val filteredReports: StateFlow<List<Report>> = combine(
        reports,
        _selectedCategoryFilter,
        _selectedColoniaFilter,
        _searchQuery
    ) { reportList, category, colonia, query ->
        reportList.filter { report ->
            val matchCategory = category == "Todos" || report.category.equals(category, ignoreCase = true)
            val matchColonia = colonia == "Todas" || report.locationColonia.equals(colonia, ignoreCase = true)
            val matchQuery = query.isEmpty() ||
                    report.title.contains(query, ignoreCase = true) ||
                    report.description.contains(query, ignoreCase = true) ||
                    report.locationStreet.contains(query, ignoreCase = true)
            matchCategory && matchColonia && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun createReport(
        title: String,
        description: String,
        category: String,
        colonia: String,
        street: String,
        photoType: String
    ) {
        viewModelScope.launch {
            val report = Report(
                title = title.ifBlank { "Reporte de $category" },
                description = description.ifBlank { "Sin descripción detallada." },
                category = category,
                locationColonia = colonia,
                locationStreet = street.ifBlank { "Calle sin nombre específico" },
                status = "Reportado",
                votes = 1,
                hasVoted = true,
                photoType = photoType
            )
            val id = repository.insertReport(report)

            // Trigger notification
            repository.insertNotification(
                Notification(
                    title = "¡Reporte Creado con Éxito!",
                    message = "Tu reporte sobre '$title' en $colonia se envió correctamente. Se inició el seguimiento.",
                    category = "Status",
                    relatedReportId = id.toInt()
                )
            )

            // Auto navigate to home
            navigateTo("HOME")
            clearPrefilledValues()
        }
    }

    fun upvoteReport(report: Report) {
        if (report.hasVoted) return // Already voted on this device

        viewModelScope.launch {
            val updated = report.copy(
                votes = report.votes + 1,
                hasVoted = true
            )
            repository.updateReport(updated)

            // Insert progress notification
            repository.insertNotification(
                Notification(
                    title = "¡Reporte Apoyado!",
                    message = "Has votado por el reporte en ${report.locationStreet}. Suma ${updated.votes} apoyos.",
                    category = "Status",
                    relatedReportId = report.id
                )
            )
        }
    }

    fun voteProposal(proposalId: Int, opinion: String) {
        viewModelScope.launch {
            // Find proposal in database
            val proposalsList = proposals.value
            val match = proposalsList.find { it.id == proposalId } ?: return@launch

            if (match.userVote != null) return@launch // Already voted!

            val baseYes = if (opinion == "SI") match.yesVotes + 1 else match.yesVotes
            val baseNo = if (opinion == "NO") match.noVotes + 1 else match.noVotes
            val totalVotes = baseYes + baseNo

            val finalStatus = if (totalVotes >= match.targetVotes) {
                if (baseYes > baseNo) "Aprobado" else "Rechazado"
            } else {
                match.status
            }

            val updated = match.copy(
                yesVotes = baseYes,
                noVotes = baseNo,
                userVote = opinion,
                status = finalStatus
            )
            repository.updateProposal(updated)

            // Trigger notification
            val proposalStatusMsg = if (finalStatus == "Aprobado") {
                "¡PROPUESTA APROBADA! '${match.title}' alcanzó el quórum vecinal y pasa a ejecución con el Comité."
            } else {
                "Registramos tu voto '$opinion' en la consulta vecinal: '${match.title}'."
            }

            repository.insertNotification(
                Notification(
                    title = if (finalStatus == "Aprobado") "¡Meta de Votos Alcanzada!" else "Voto Registrado",
                    message = proposalStatusMsg,
                    category = "Proposal"
                )
            )
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    /**
     * Simulation action: Advance report status (Admin simulator/Case flow)
     * To make case tracking/notifications extremely visual and satisfying, the user can click
     * "Verificar/Cambiar Estado" in the detail page to simulate government progress!
     */
    fun advanceReportStatus(report: Report) {
        val nextStatus = when (report.status) {
            "Reportado" -> "En proceso"
            "En proceso" -> "Resuelto"
            else -> "Reportado"
        }

        viewModelScope.launch {
            val updated = report.copy(status = nextStatus)
            repository.updateReport(updated)

            val detailsUpdate = when (nextStatus) {
                "En proceso" -> "Un técnico ha sido asignado para revisar la avería."
                "Resuelto" -> "La cuadrilla comunitaria reparó el problema y el reporte está concluido."
                else -> "Reporte reabierto por revisión vecinal."
            }

            // Trigger real-time tracking notification
            repository.insertNotification(
                Notification(
                    title = "Actualización: $nextStatus",
                    message = "El caso '${report.title}' cambió a '$nextStatus'. $detailsUpdate",
                    category = "Status",
                    relatedReportId = report.id
                )
            )
        }
    }

    /**
     * Authority action: updates a report's status and assigned crew, and sends an alert.
     */
    fun updateReportByAuthority(reportId: Int, newStatus: String, assignedTo: String?, progressNote: String) {
        viewModelScope.launch {
            val reportList = reports.value
            val match = reportList.find { it.id == reportId } ?: return@launch

            val updated = match.copy(
                status = newStatus,
                assignedTo = if (assignedTo.isNullOrBlank()) null else assignedTo
            )
            repository.updateReport(updated)

            // Setup customized notification to citizen
            val defaultMessage = "El caso '${match.title}' se actualizó a '$newStatus'${if (!assignedTo.isNullOrBlank()) " y fue asignado a '$assignedTo'" else ""}."
            val finalMessage = if (progressNote.isNotBlank()) {
                "$defaultMessage Nota oficial: \"$progressNote\""
            } else {
                defaultMessage
            }

            repository.insertNotification(
                Notification(
                    title = "Avance: Reporte #${reportId}",
                    message = finalMessage,
                    category = "Status",
                    relatedReportId = match.id
                )
            )
        }
    }
}

// Factory for ViewModel
class CommunityViewModelFactory(
    private val application: Application,
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class RoadSection(
    val id: Int,
    val name: String,
    val lengthKm: Double,
    val responsible: String, // "Federal (SICT)", "Estatal (SICOM)", "Municipal"
    val conservationState: String // "Excelente", "Bueno", "Regular", "Malo", "Crítico"
)
