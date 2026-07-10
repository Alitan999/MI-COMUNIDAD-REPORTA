package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Bache", "Fuga", "Basura"
    val locationColonia: String, // e.g., "Centro", "San Ángel", "Coyoacán", "Xochimilco"
    val locationStreet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Reportado", // "Reportado", "En proceso", "Resuelto"
    val votes: Int = 1,
    val hasVoted: Boolean = false, // Track if local user clicked "Me afecta" / Upvote
    val photoType: String = "bache_1", // Type of image to show (bache_1, bache_2, fuga_1, basura_1, etc.)
    val customPhotoPath: String? = null, // For simulated photo captured
    val assignedTo: String? = null // Assigned personnel/crew from Authorities (e.g. "SAD-3 Obras Públicas")
)

@Entity(tableName = "proposals")
data class Proposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Parques", "Seguridad", "Servicios", "Cultura"
    val yesVotes: Int = 0,
    val noVotes: Int = 0,
    val targetVotes: Int = 50,
    val status: String = "En votación", // "En votación", "Aprobado", "Rechazado"
    val deadline: String, // Deadlines for voting
    val userVote: String? = null, // "SI", "NO" or null if not voted
    val creator: String = "Comité Vecinal"
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedReportId: Int? = null,
    val category: String = "Status", // "Status", "Proposal", "Alert"
    val isRead: Boolean = false
)
