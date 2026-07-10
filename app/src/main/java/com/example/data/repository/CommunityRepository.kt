package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.local.ProposalDao
import com.example.data.local.ReportDao
import com.example.data.model.Notification
import com.example.data.model.Proposal
import com.example.data.model.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take

class CommunityRepository(
    private val reportDao: ReportDao,
    private val proposalDao: ProposalDao,
    private val notificationDao: NotificationDao
) {
    val allReports: Flow<List<Report>> = reportDao.getAllReports()
    val allProposals: Flow<List<Proposal>> = proposalDao.getAllProposals()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getReportById(id: Int): Flow<Report?> = reportDao.getReportById(id)

    suspend fun insertReport(report: Report): Long {
        return reportDao.insertReport(report)
    }

    suspend fun updateReport(report: Report) {
        reportDao.updateReport(report)
    }

    suspend fun deleteReport(report: Report) {
        reportDao.deleteReport(report)
    }

    suspend fun insertProposal(proposal: Proposal): Long {
        return proposalDao.insertProposal(proposal)
    }

    suspend fun updateProposal(proposal: Proposal) {
        proposalDao.updateProposal(proposal)
    }

    suspend fun insertNotification(notification: Notification) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    /**
     * Seeds the local database with rich preloaded reports and proposals if tables are empty.
     */
    suspend fun populateInitialDataIfNeeded() {
        val currentReports = allReports.take(1).first()
        if (currentReports.isEmpty()) {
            // Seed Reports for Tarandacuao Guanajuato
            val initialReports = listOf(
                Report(
                    title = "Bache Crítico en Carretera Estatal",
                    description = "Hay baches profundos en el tramo estatal que va de Jerécuaro a Tarandacuao, dificultando el paso de vehículos y dañando neumáticos cerca del entronque.",
                    category = "Bache",
                    locationColonia = "San Felipe",
                    locationStreet = "Carretera Jerécuaro - Tarandacuao, km 4.2",
                    status = "Reportado",
                    votes = 24,
                    photoType = "bache_1"
                ),
                Report(
                    title = "Fuga de Agua Potable en Calle Hidalgo",
                    description = "Fuga severa de agua limpia brotando de la banqueta a unos metros de la Presidencia Municipal. Lleva dos días desperdiciándose.",
                    category = "Fuga de agua",
                    locationColonia = "Cabecera Centro",
                    locationStreet = "Calle Hidalgo #104, Tarandacuao",
                    status = "En proceso",
                    votes = 38,
                    photoType = "fuga_1"
                ),
                Report(
                    title = "Basura Acumulada en Acceso a El Guayabo",
                    description = "Se ha reportado acumulación constante de bolsas de residuos sólidos en la desviación hacia el camino de El Guayabo. Requiere recolección urgente.",
                    category = "Basura",
                    locationColonia = "El Guayabo",
                    locationStreet = "Camino de Acceso a El Guayabo, km 0.5",
                    status = "Reportado",
                    votes = 15,
                    photoType = "basura_1"
                ),
                Report(
                    title = "Baches Reparados en Tarandacuao - Curinhuato",
                    description = "Hundimientos severos del asfalto que obstruían el tránsito de camiones agrícolas ya han sido corregidos con bacheo caliente.",
                    category = "Bache",
                    locationColonia = "Curinhuato",
                    locationStreet = "Camino Tarandacuao - Curinhuato, km 1.8",
                    status = "Resuelto",
                    votes = 29,
                    photoType = "bache_2"
                )
            )

            for (report in initialReports) {
                reportDao.insertReport(report)
            }

            // Seed Proposals for voting in Tarandacuao
            val initialProposals = listOf(
                Proposal(
                    title = "Reforestación de la Cuenca del Río Lerma",
                    description = "Campaña municipal para sembrar 150 árboles nativos (sabinos y sauces) a lo largo de la ribera del Río Lerma en nuestro tramo municipal para mitigar erosión.",
                    category = "Parques",
                    yesVotes = 48,
                    noVotes = 1,
                    targetVotes = 60,
                    status = "En votación",
                    deadline = "15 de Julio",
                    creator = "Asociación Ecologista del Río Lerma"
                ),
                Proposal(
                    title = "Alumbrado LED en Acceso La Mora - Paso de las Ovejas",
                    description = "Instalar lámparas solares de tecnología LED de alta eficiencia en los puntos más oscuros de este transitado camino municipal.",
                    category = "Servicios",
                    yesVotes = 42,
                    noVotes = 0,
                    targetVotes = 40,
                    status = "Aprobado",
                    deadline = "30 de Junio",
                    creator = "Comité Vecinal Paso de las Ovejas"
                ),
                Proposal(
                    title = "Preservación de Cerámica Mayólica Tradicional",
                    description = "Crear un taller artesanal gratuito en la cabecera municipal los fines de semana para jóvenes, impulsando la famosa alfarería y cerámica decorada del municipio.",
                    category = "Cultura",
                    yesVotes = 31,
                    noVotes = 2,
                    targetVotes = 35,
                    status = "En votación",
                    deadline = "20 de Julio",
                    creator = "Artesanos Unidos de Tarandacuao"
                )
            )

            for (proposal in initialProposals) {
                proposalDao.insertProposal(proposal)
            }

            // Seed Notifications
            val initialNotifications = listOf(
                Notification(
                    title = "¡Proyecto Terminado!",
                    message = "La Dirección de Obras de Tarandacuao concluyó los trabajos de bacheo en el camino a Curinhuato. El reporte está RESUELTO.",
                    category = "Status",
                    relatedReportId = 4
                ),
                Notification(
                    title = "Nueva Consulta Ciudadana",
                    message = "Se ha abierto el proyecto de votación 'Reforestación de la Cuenca del Río Lerma'. ¡Apoya esta iniciativa comunitaria!",
                    category = "Proposal"
                ),
                Notification(
                    title = "¡En Atención!",
                    message = "La fuga de agua en Calle Hidalgo ha sido recibida por la cuadrilla de Agua Potable. La reparación inicia hoy por la tarde.",
                    category = "Status",
                    relatedReportId = 2
                )
            )

            for (notification in initialNotifications) {
                notificationDao.insertNotification(notification)
            }
        }
    }
}
