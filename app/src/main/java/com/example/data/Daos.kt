package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?
}

@Dao
interface VaultFileDao {
    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 ORDER BY addedAt DESC")
    fun getAllActiveFiles(): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE folderId = :folderId AND isDeleted = 0 ORDER BY addedAt DESC")
    fun getFilesByFolder(folderId: Long): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 ORDER BY addedAt DESC LIMIT 10")
    fun getRecentFiles(): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashFiles(): Flow<List<VaultFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFile): Long

    @Update
    suspend fun updateFile(file: VaultFile)

    @Delete
    suspend fun deleteFile(file: VaultFile)

    @Query("SELECT * FROM vault_files WHERE id = :id")
    suspend fun getFileById(id: Long): VaultFile?

    @Query("UPDATE vault_files SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET isDeleted = 0, deletedAt = null WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("UPDATE vault_files SET folderId = :folderId WHERE id = :id")
    suspend fun moveToFileFolder(id: Long, folderId: Long?)

    @Query("DELETE FROM vault_files WHERE isDeleted = 1")
    suspend fun clearTrash()

    @Query("DELETE FROM vault_files WHERE isDeleted = 1 AND :now - deletedAt > :thirtyDaysInMillis")
    suspend fun autoPurgeTrash(now: Long, thirtyDaysInMillis: Long = 30 * 24 * 60 * 60 * 1000L)
}

@Dao
interface CalcSettingDao {
    @Query("SELECT * FROM calc_settings")
    fun getAllSettingsFlow(): Flow<List<CalcSetting>>

    @Query("SELECT * FROM calc_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): CalcSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: CalcSetting)

    @Query("DELETE FROM calc_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}

@Dao
interface CalcHistoryDao {
    @Query("SELECT * FROM calc_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalcHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CalcHistory)

    @Query("DELETE FROM calc_history")
    suspend fun clearHistory()
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacksFlow(): Flow<List<Feedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: Feedback)

    @Query("DELETE FROM feedbacks")
    suspend fun clearFeedbacks()
}
