package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_files")
data class VaultFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long? = null,
    val originalPath: String,
    val encryptedPath: String, // Internal location of the renamed file
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(tableName = "calc_settings")
data class CalcSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "calc_history")
data class CalcHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "feedbacks")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val description: String,
    val contact: String,
    val timestamp: Long = System.currentTimeMillis()
)
