package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class VaultRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val folderDao = db.folderDao()
    private val vaultFileDao = db.vaultFileDao()
    private val calcSettingDao = db.calcSettingDao()
    private val calcHistoryDao = db.calcHistoryDao()
    private val feedbackDao = db.feedbackDao()

    private val vaultDir = File(context.filesDir, "hidden_vault").apply {
        if (!exists()) mkdirs()
    }

    // Flows
    val foldersFlow: Flow<List<Folder>> = folderDao.getAllFolders()
    val activeFilesFlow: Flow<List<VaultFile>> = vaultFileDao.getAllActiveFiles()
    val recentFilesFlow: Flow<List<VaultFile>> = vaultFileDao.getRecentFiles()
    val trashFilesFlow: Flow<List<VaultFile>> = vaultFileDao.getTrashFiles()
    val settingsFlow: Flow<List<CalcSetting>> = calcSettingDao.getAllSettingsFlow()
    val historyFlow: Flow<List<CalcHistory>> = calcHistoryDao.getAllHistory()
    val feedbacksFlow: Flow<List<Feedback>> = feedbackDao.getAllFeedbacksFlow()

    suspend fun submitFeedback(type: String, description: String, contact: String) {
        feedbackDao.insertFeedback(Feedback(type = type, description = description, contact = contact))
    }

    suspend fun clearAllFeedbacks() {
        feedbackDao.clearFeedbacks()
    }

    init {
        // Run seed or defaults asynchronously if needed
    }

    suspend fun seedDefaults() {
        withContext(Dispatchers.IO) {
            // Check if folders are empty, if so, seed dummy folders and settings
            val folders = foldersFlow.first()
            if (folders.isEmpty()) {
                val famPhotoId = folderDao.insertFolder(Folder(name = "家庭照片"))
                val privVideoId = folderDao.insertFolder(Folder(name = "私密视频"))
                val docId = folderDao.insertFolder(Folder(name = "个人文档"))

                // Seeds mock items in Room to match the HTML design layout perfectly!
                // This makes preview gorgeous out of the box.
                val file1 = VaultFile(
                    id = 11,
                    folderId = famPhotoId,
                    originalPath = "/storage/emulated/0/DCIM/Camera/IMG_0821.jpg",
                    encryptedPath = createMockFile("IMG_0821.jpg", 124 * 1024),
                    fileName = "IMG_0821.jpg",
                    fileSize = 4404019, // 4.2 MB
                    mimeType = "image/jpeg"
                )
                val file2 = VaultFile(
                    id = 22,
                    folderId = privVideoId,
                    originalPath = "/storage/emulated/0/Movies/Vacation_Clip.mp4",
                    encryptedPath = createMockFile("Vacation_Clip.mp4", 45 * 1024 * 1024),
                    fileName = "Vacation_Clip.mp4",
                    fileSize = 48024780, // 45.8 MB
                    mimeType = "video/mp4"
                )
                val file3 = VaultFile(
                    id = 33,
                    folderId = docId,
                    originalPath = "/storage/emulated/0/Documents/Tax_Report_2023.pdf",
                    encryptedPath = createMockFile("Tax_Report_2023.pdf", 342 * 1024),
                    fileName = "Tax_Report_2023.pdf",
                    fileSize = 350208, // 342 KB
                    mimeType = "application/pdf"
                )
                val file4 = VaultFile(
                    id = 44,
                    folderId = docId,
                    originalPath = "/storage/emulated/0/Documents/Journal_Pass.txt",
                    encryptedPath = createMockFile("Journal_Pass.txt", 12 * 1024),
                    fileName = "Journal_Pass.txt",
                    fileSize = 12288, // 12 KB
                    mimeType = "text/plain"
                )

                // Trash Items (Mocks matching the HTML TRASH EXACTLY!)
                val fileTrash1 = VaultFile(
                    id = 55,
                    folderId = famPhotoId,
                    originalPath = "/storage/emulated/0/DCIM/Camera/private_memo_01.jpg",
                    encryptedPath = createMockFile("private_memo_01.jpg", 1024 * 1024),
                    fileName = "private_memo_01.jpg",
                    fileSize = 1258291, // 1.2 MB
                    mimeType = "image/jpeg",
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
                )
                val fileTrash2 = VaultFile(
                    id = 66,
                    folderId = privVideoId,
                    originalPath = "/storage/emulated/0/Movies/vacation_clip_draft.mp4",
                    encryptedPath = createMockFile("vacation_clip_draft.mp4", 45 * 1024 * 1024),
                    fileName = "vacation_clip_draft.mp4",
                    fileSize = 48024780,
                    mimeType = "video/mp4",
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 1 week ago
                )
                val fileTrash3 = VaultFile(
                    id = 77,
                    folderId = docId,
                    originalPath = "/storage/emulated/0/Documents/scanned_doc_2023.pdf",
                    encryptedPath = createMockFile("scanned_doc_2023.pdf", 342 * 1024),
                    fileName = "scanned_doc_2023.pdf",
                    fileSize = 350208,
                    mimeType = "application/pdf",
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000L) // 14 days ago
                )
                val fileTrash4 = VaultFile(
                    id = 88,
                    folderId = null,
                    originalPath = "/storage/emulated/0/Documents/notes_archive.txt",
                    encryptedPath = createMockFile("notes_archive.txt", 12 * 1024),
                    fileName = "notes_archive.txt",
                    fileSize = 12288,
                    mimeType = "text/plain",
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis() - (28 * 24 * 60 * 60 * 1000L) // 28 days ago
                )

                vaultFileDao.insertFile(file1)
                vaultFileDao.insertFile(file2)
                vaultFileDao.insertFile(file3)
                vaultFileDao.insertFile(file4)
                vaultFileDao.insertFile(fileTrash1)
                vaultFileDao.insertFile(fileTrash2)
                vaultFileDao.insertFile(fileTrash3)
                vaultFileDao.insertFile(fileTrash4)
            }

            // Seed default settings if empty
            if (calcSettingDao.getSetting("password") == null) {
                calcSettingDao.insertSetting(CalcSetting("password", "123456"))
                calcSettingDao.insertSetting(CalcSetting("gesture", "双击 %"))
                calcSettingDao.insertSetting(CalcSetting("fingerprint_enabled", "false"))
                calcSettingDao.insertSetting(CalcSetting("vibration_enabled", "true"))
                calcSettingDao.insertSetting(CalcSetting("theme_mode", "system"))
                calcSettingDao.insertSetting(CalcSetting("auto_lock_time", "立即"))
                calcSettingDao.insertSetting(CalcSetting("emergency_code", "998877665544332211242424"))
                calcSettingDao.insertSetting(CalcSetting("security_email", "admin@jiuyi.com"))
            }

            // Run auto cleanup for > 30 days trash
            vaultFileDao.autoPurgeTrash(System.currentTimeMillis())
        }
    }

    private fun createMockFile(name: String, sizeBytes: Int): String {
        val file = File(vaultDir, "mock_${UUID.randomUUID()}_$name")
        if (!file.exists()) {
            try {
                file.createNewFile()
                // write mini placeholder text
                file.writeText("Mock encrypted file representation of $name ($sizeBytes bytes)")
            } catch (e: Exception) {
                Log.e("Repository", "Error seeding mock file: ${e.message}")
            }
        }
        return file.absolutePath
    }

    // Folders
    suspend fun createFolder(name: String) = folderDao.insertFolder(Folder(name = name))
    suspend fun renameFolder(id: Long, newName: String) {
        val folder = folderDao.getFolderById(id)
        if (folder != null) {
            folderDao.updateFolder(folder.copy(name = newName))
        }
    }
    suspend fun deleteFolder(id: Long) {
        val folder = folderDao.getFolderById(id)
        if (folder != null) {
            folderDao.deleteFolder(folder)
        }
    }

    // Files
    fun getFilesByFolder(folderId: Long): Flow<List<VaultFile>> = vaultFileDao.getFilesByFolder(folderId)

    // Move file to folder
    suspend fun moveFileToFolder(fileId: Long, folderId: Long?) {
        vaultFileDao.moveToFileFolder(fileId, folderId)
    }

    // Add visual content (Photo/Video/Doc) using SAF content resolver
    suspend fun importFile(uri: Uri, folderId: Long?): VaultFile? = withContext(Dispatchers.IO) {
        try {
            var fileName = "imported_${System.currentTimeMillis()}"
            var fileSize = 0L
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            // Query name & size
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val internalName = "vault_${UUID.randomUUID()}_$fileName"
            val destFile = File(vaultDir, internalName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            if (fileSize == 0L && destFile.exists()) {
                fileSize = destFile.length()
            }

            val vaultFile = VaultFile(
                folderId = folderId,
                originalPath = uri.toString(),
                encryptedPath = destFile.absolutePath,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType
            )

            val id = vaultFileDao.insertFile(vaultFile)
            vaultFileDao.getFileById(id)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to import file: ${e.message}", e)
            null
        }
    }

    // Trash operations
    suspend fun moveToTrash(id: Long) = vaultFileDao.moveToTrash(id)
    suspend fun restoreFromTrash(id: Long) = vaultFileDao.restoreFromTrash(id)
    suspend fun clearTrash() = vaultFileDao.clearTrash()
    suspend fun deleteFilePermanently(id: Long) = withContext(Dispatchers.IO) {
        val file = vaultFileDao.getFileById(id)
        if (file != null) {
            try {
                val encryptedFile = File(file.encryptedPath)
                if (encryptedFile.exists()) {
                    encryptedFile.delete()
                }
            } catch (e: Exception) {
                Log.e("Repository", "Failed deleting physical file: ${e.message}")
            }
            vaultFileDao.deleteFile(file)
        }
    }

    // Settings
    suspend fun getSetting(key: String): String? {
        return calcSettingDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        calcSettingDao.insertSetting(CalcSetting(key, value))
    }

    // Calculator History
    suspend fun addHistory(expression: String, result: String) {
        calcHistoryDao.insertHistory(CalcHistory(expression = expression, result = result))
    }

    suspend fun clearHistory() {
        calcHistoryDao.clearHistory()
    }
}
