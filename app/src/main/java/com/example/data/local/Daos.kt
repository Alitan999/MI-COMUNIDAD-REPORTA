package com.example.data.local

import androidx.room.*
import com.example.data.model.Notification
import com.example.data.model.Proposal
import com.example.data.model.Report
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: Int): Flow<Report?>

    @Query("SELECT * FROM reports WHERE category = :category ORDER BY timestamp DESC")
    fun getReportsByCategory(category: String): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE locationColonia = :colonia ORDER BY timestamp DESC")
    fun getReportsByColonia(colonia: String): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report): Long

    @Update
    suspend fun updateReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)
}

@Dao
interface ProposalDao {
    @Query("SELECT * FROM proposals ORDER BY id DESC")
    fun getAllProposals(): Flow<List<Proposal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: Proposal): Long

    @Update
    suspend fun updateProposal(proposal: Proposal)

    @Query("SELECT * FROM proposals WHERE id = :id")
    suspend fun getProposalById(id: Int): Proposal?
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
