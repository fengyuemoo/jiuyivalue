package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Folder
import com.example.data.VaultFile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onNavigateBackToCalc: () -> Unit
) {
    // Collect DB triggers
    val currentScreen by viewModel.currentScreen.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val activeFiles by viewModel.activeFiles.collectAsState()
    val trashFiles by viewModel.trashFiles.collectAsState()

    // Collect query-based selections
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val gridMode by viewModel.gridMode.collectAsState()

    // Multiple collection parameters
    val selectedFileIds by viewModel.selectedFileIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val previewFile by viewModel.previewFile.collectAsState()

    val context = LocalContext.current

    // Local dialog controls
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    
    var showRenameFolderDialog by remember { mutableStateOf<Long?>(null) }
    var folderRenameInput by remember { mutableStateOf("") }

    var showMoveFolderDialog by remember { mutableStateOf(false) }

    // Media select pickers
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importSelectedFile(uri)
            Toast.makeText(context, "📥 正在安全加密并导入文件到隐藏文件夹...", Toast.LENGTH_SHORT).show()
        }
    }

    // Modern Stealth Midnight Palette
    val darkBg = Color(0xFF0F172A)          // Midnight
    val darkSurface = Color(0xFF1E293B)     // Deep Slate
    val primaryColor = Color(0xFF6366F1)    // Soft Indigo Accent
    val borderCol = Color(0xFF334155)       // Neutral borders

    // Filtered Files list logic
    val filteredFiles = remember(activeFiles, searchQuery, selectedCategory, selectedFolderId) {
        activeFiles.filter { file ->
            // Match folder folderId
            val matchFolder = selectedFolderId == null || file.folderId == selectedFolderId
            // Match search
            val matchSearch = searchQuery.isBlank() || file.fileName.contains(searchQuery, ignoreCase = true)
            // Match category type
            val matchCategory = when (selectedCategory) {
                FileCategory.ALL -> true
                FileCategory.PHOTOS -> file.mimeType.startsWith("image/")
                FileCategory.VIDEOS -> file.mimeType.startsWith("video/")
                FileCategory.DOCS -> !file.mimeType.startsWith("image/") && !file.mimeType.startsWith("video/")
            }
            matchFolder && matchSearch && matchCategory
        }
    }

    Scaffold(
        topBar = {
            if (isMultiSelectMode) {
                // =============== 1. FULL MULTI SELECT TOOLBAR (TOP INTERCEPT) ===============
                TopAppBar(
                    title = { Text("已选中 ${selectedFileIds.size} 项", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelections() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "clear selection", tint = Color.LightGray)
                        }
                    },
                    actions = {
                        // Move to folder target
                        IconButton(onClick = { showMoveFolderDialog = true }) {
                            Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "move", tint = primaryColor)
                        }
                        // Share Selected
                        IconButton(onClick = {
                            viewModel.shareSelectedFiles(context)
                            Toast.makeText(context, "📤 触发原生社交分享：解密中级临时链接并分享...", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "share", tint = primaryColor)
                        }
                        // Unarchive back to gallery
                        IconButton(onClick = {
                            viewModel.exportSelectedToGallery()
                            Toast.makeText(context, "🖼️ 文件已移回原生相册目录中", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.Unarchive, contentDescription = "unarchive", tint = primaryColor)
                        }
                        // Delete (Soft Delete)
                        IconButton(onClick = {
                            viewModel.deleteSelectedFiles()
                            Toast.makeText(context, "🗑️ 已移至回收站，30天后将自动删除", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkSurface)
                )
            } else {
                // =============== 2. DEFAULT HEADER TOOLBAR ===============
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "shield",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.VAULT_EXPLORER -> if (selectedFolderId != null) {
                                        folders.find { it.id == selectedFolderId }?.name ?: "久以暗箱"
                                    } else "久以隐私空间"
                                    AppScreen.TRASH -> "安全回收站"
                                    AppScreen.SETTINGS -> "关于久以"
                                    else -> "久以隐藏空间"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        if (selectedFolderId != null) {
                            IconButton(onClick = { viewModel.selectFolder(null) }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back path", tint = Color.LightGray)
                            }
                        } else {
                            IconButton(onClick = onNavigateBackToCalc) {
                                Icon(imageVector = Icons.Default.LockReset, contentDescription = "exit safe", tint = Color.LightGray)
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == AppScreen.VAULT_EXPLORER) {
                            IconButton(onClick = { viewModel.setGridMode(!gridMode) }) {
                                Icon(
                                    imageVector = if (gridMode) Icons.Default.GridOn else Icons.Default.List,
                                    contentDescription = "display style",
                                    tint = Color.LightGray
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "settings", tint = Color.LightGray)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
                )
            }
        },
        bottomBar = {
            // Navigation tab links bottom bar matches HTML templates
            NavigationBar(
                containerColor = darkSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.VAULT_EXPLORER && selectedFolderId == null,
                    onClick = {
                        viewModel.selectFolder(null)
                        viewModel.navigateTo(AppScreen.VAULT_EXPLORER)
                    },
                    icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = "文件") },
                    label = { Text("文件", color = Color.White) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.VAULT_EXPLORER && selectedFolderId != null,
                    onClick = {
                        viewModel.navigateTo(AppScreen.VAULT_EXPLORER)
                    },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "最近") },
                    label = { Text("最近", color = Color.White) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TRASH,
                    onClick = { viewModel.navigateTo(AppScreen.TRASH) },
                    icon = { Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "回收站") },
                    label = { Text("回收站", color = Color.White) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = { Icon(imageVector = Icons.Default.Security, contentDescription = "深度保存") },
                    label = { Text("深度保存", color = Color.White) }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.VAULT_EXPLORER && !isMultiSelectMode) {
                FloatingActionButton(
                    onClick = {
                        // OpenSAF to add typical file
                        filePickerLauncher.launch("*/*")
                    },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "导入", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = darkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.VAULT_EXPLORER -> {
                    // =============== MAIN WORKSPACE CONTEXT ===============
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Modern Search Field with folder triggers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("搜索文件...", color = Color.Gray, fontSize = 14.sp) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "search", tint = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = darkSurface,
                                    unfocusedContainerColor = darkSurface,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            // 新建文件夹按钮
                            Button(
                                onClick = { showCreateFolderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "new folder", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("新建", fontSize = 13.sp)
                            }
                        }

                        if (selectedFolderId == null) {
                            // SHOW FOLDERS GROUP (only in root path)
                            Text(
                                "文件夹",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(folders) { folder ->
                                    val filesInFolder = activeFiles.count { it.folderId == folder.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(darkSurface)
                                            .clickable { viewModel.selectFolder(folder.id) }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.FolderSpecial,
                                                contentDescription = "folder",
                                                tint = primaryColor,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(folder.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                Text("$filesInFolder 个项目", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }

                                        Row {
                                            // Rename
                                            IconButton(onClick = {
                                                folderRenameInput = folder.name
                                                showRenameFolderDialog = folder.id
                                            }) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "rename", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                            // Delete folder
                                            IconButton(onClick = { viewModel.deleteFolder(folder.id) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // FILES SUBSECTION CONTROLS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedFolderId != null) "文件夹文件" else "最近文件",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            // Quick Categories filter
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val categories = listOf(
                                    FileCategory.ALL to "全部",
                                    FileCategory.PHOTOS to "图片",
                                    FileCategory.VIDEOS to "视频",
                                    FileCategory.DOCS to "文档"
                                )
                                categories.forEach { (cat, label) ->
                                    val isSel = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable { viewModel.selectCategory(cat) }
                                            .border(
                                                width = 1.dp,
                                                color = if (isSel) primaryColor else Color.Gray.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(label, color = if (isSel) primaryColor else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        if (filteredFiles.isEmpty()) {
                            // Friendly Empty screen state
                            Box(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.CloudQueue, contentDescription = "empty", modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("该位置下没有符合的文件", color = Color.Gray)
                                    Text("点击右下角【+】可以导入私密图片和视频哦！", color = Color.Gray.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            }
                        } else {
                            if (gridMode) {
                                // =============== GRID REPRESENTATION ===============
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize().weight(1f)
                                ) {
                                    items(filteredFiles) { file ->
                                        FileGridItem(
                                            file = file,
                                            isSelected = selectedFileIds.contains(file.id),
                                            isMultiSelect = isMultiSelectMode,
                                            onTap = {
                                                if (isMultiSelectMode) {
                                                    viewModel.toggleFileSelection(file.id)
                                                } else {
                                                    viewModel.triggerPreviewFile(file)
                                                }
                                            },
                                            onLongTap = {
                                                viewModel.toggleFileSelection(file.id)
                                            }
                                        )
                                    }
                                }
                            } else {
                                // =============== LIST REPRESENTATION ===============
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize().weight(1f)
                                ) {
                                    items(filteredFiles) { file ->
                                        FileListRow(
                                            file = file,
                                            isSelected = selectedFileIds.contains(file.id),
                                            isMultiSelect = isMultiSelectMode,
                                            onTap = {
                                                if (isMultiSelectMode) {
                                                    viewModel.toggleFileSelection(file.id)
                                                } else {
                                                    viewModel.triggerPreviewFile(file)
                                                }
                                            },
                                            onLongTap = {
                                                viewModel.toggleFileSelection(file.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AppScreen.TRASH -> {
                    // =============== RECYCLE BIN ENVIRONMENT (TRASH) ===============
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Header retention info box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = darkSurface.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .border(width = 1.dp, color = primaryColor.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "hint", tint = primaryColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "项目将在 30 天后永久删除。这些文件目前仅可在此回收站中访问。可以点选或直接清空。",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Global action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    trashFiles.forEach { viewModel.itemRestoreFromTrash(it.id) }
                                    Toast.makeText(context, "🔄 全部删除文件已一键恢复原暗箱夹", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = "restore all", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("全部恢复")
                            }

                            Button(
                                onClick = {
                                    viewModel.clearEntireTrash()
                                    Toast.makeText(context, "🔥 回收站已完全清空，设备清净", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "clear bin", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("清空回收站")
                            }
                        }

                        if (trashFiles.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.AutoDelete, contentDescription = "empty trash", modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("回收站已空", color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("临时删除的项目会在这里保留30天", color = Color.Gray.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(trashFiles) { file ->
                                    TrashListItem(
                                        file = file,
                                        onRestore = {
                                            viewModel.itemRestoreFromTrash(file.id)
                                            Toast.makeText(context, "已恢复：${file.fileName}", Toast.LENGTH_SHORT).show()
                                        },
                                        onDeletePermanently = {
                                            viewModel.itemDeletePermanently(file.id)
                                            Toast.makeText(context, "已彻底永久除名删除！", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                AppScreen.SETTINGS -> {
                    // =============== INTEGRATED DEPTH SAVE SCREEN ===============
                    DepthDownloadScreen(viewModel = viewModel) {
                        viewModel.navigateTo(AppScreen.VAULT_EXPLORER)
                    }
                }

                else -> {}
            }
        }
    }

    // ==========================================
    // MULTIPLE CONTEXT DIALOGS
    // ==========================================

    // CREATE FOLDER DIALOG
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("文件夹名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderNameInput.isNotBlank()) {
                        viewModel.createFolder(folderNameInput)
                        folderNameInput = ""
                        showCreateFolderDialog = false
                    }
                }) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("取消") }
            }
        )
    }

    // RENAME FOLDER DIALOG
    if (showRenameFolderDialog != null) {
        val fid = showRenameFolderDialog!!
        AlertDialog(
            onDismissRequest = { showRenameFolderDialog = null },
            title = { Text("重命名文件夹") },
            text = {
                OutlinedTextField(
                    value = folderRenameInput,
                    onValueChange = { folderRenameInput = it },
                    label = { Text("名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderRenameInput.isNotBlank()) {
                        viewModel.renameFolder(fid, folderRenameInput)
                        showRenameFolderDialog = null
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameFolderDialog = null }) { Text("取消") }
            }
        )
    }

    // BULK ARCHIVE TO FOLDER DIALOG
    if (showMoveFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveFolderDialog = false },
            title = { Text("移动归档到目标文件夹") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option 1: Root Dir
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.moveSelectedToFolder(null)
                                showMoveFolderDialog = false
                                Toast.makeText(context, "已移动归档至最外层隐私空间", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text("外层目录 (无文件夹归属)", modifier = Modifier.padding(14.dp))
                    }

                    // Options Folders
                    folders.forEach { folder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveSelectedToFolder(folder.id)
                                    showMoveFolderDialog = false
                                    Toast.makeText(context, "已安全移动归档至：${folder.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(folder.name, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveFolderDialog = false }) { Text("关闭") }
            }
        )
    }

    // FULL SCREEN PREVIEW POPUP
    if (previewFile != null) {
        val file = previewFile!!
        val isImage = file.mimeType.startsWith("image/")
        val isVideo = file.mimeType.startsWith("video/")

        Dialog(
            onDismissRequest = { viewModel.triggerPreviewFile(null) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Main Content Visual Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    // Head Actions Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.triggerPreviewFile(null) }) {
                            Text("返回", color = Color.White, fontSize = 16.sp)
                        }
                        Text(file.fileName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = "vault verified", tint = primaryColor)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isImage) {
                            // Perfect Visual Image preview placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(darkSurface)
                            ) {
                                // Dynamic aesthetic rendering
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.radialGradient(listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.InsertPhoto, contentDescription = "photo", modifier = Modifier.size(96.dp), tint = primaryColor)
                                }
                            }
                        } else if (isVideo) {
                            // Video player simulation screen mockup
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(darkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "play video", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            }
                        } else {
                            // Doc / PDF Layout representation
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "doc", modifier = Modifier.size(80.dp), tint = Color(0xFFF97316))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(file.fileName, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("文件大小：${formatFileSize(file.fileSize)}", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }

                    // Bottom info block
                    Card(
                        colors = CardDefaults.cardColors(containerColor = darkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("安全信息详情", color = primaryColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("原物理路径：${file.originalPath}", color = Color.LightGray, fontSize = 12.sp)
                            Text("多重保险位置：${file.encryptedPath}", color = Color.LightGray, fontSize = 12.sp)
                            Text("导入时间：${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.addedAt))}", color = Color.LightGray, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.itemRestoreFromTrash(file.id)
                                        viewModel.triggerPreviewFile(null)
                                        Toast.makeText(context, "文件已安全移回手机相册，重新在公共目录可见。", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("恢复至相册", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        viewModel.itemMoveToTrash(file.id)
                                        viewModel.triggerPreviewFile(null)
                                        Toast.makeText(context, "已移入安全回收站", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("移入回收站")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// GRID ITEM VIEW
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: VaultFile,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onTap: () -> Unit,
    onLongTap: () -> Unit
) {
    val outlineCol = if (isSelected) Color(0xFF6366F1) else Color.Transparent
    val primaryColor = Color(0xFF6366F1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(width = 2.dp, color = outlineCol, shape = RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongTap
            )
    ) {
        // Thumbnail mock visual pairing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF0F172A)))),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                file.mimeType.startsWith("image/") -> Icons.Default.Image
                file.mimeType.startsWith("video/") -> Icons.Default.PlayCircle
                file.mimeType.startsWith("application/pdf") -> Icons.Default.PictureAsPdf
                else -> Icons.Default.Description
            }
            Icon(imageVector = icon, contentDescription = "type", tint = primaryColor.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
        }

        // Selected overlay checkbox check sphere
        if (isMultiSelect) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primaryColor else Color.Black.copy(alpha = 0.4f))
                    .border(width = 1.5.dp, color = if (isSelected) primaryColor else Color.LightGray, shape = CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "checked", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Bottom filename transparent container banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.fileName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatFileSize(file.fileSize),
                    color = Color.LightGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// LIST ITEM ROW
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListRow(
    file: VaultFile,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onTap: () -> Unit,
    onLongTap: () -> Unit
) {
    val primaryColor = Color(0xFF6366F1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF312E81) else Color(0xFF1E293B))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongTap
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val icon = when {
                file.mimeType.startsWith("image/") -> Icons.Default.Image
                file.mimeType.startsWith("video/") -> Icons.Default.PlayCircle
                file.mimeType.startsWith("application/pdf") -> Icons.Default.PictureAsPdf
                else -> Icons.Default.Description
            }
            Icon(imageVector = icon, contentDescription = "type", tint = primaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(file.fileName, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(formatFileSize(file.fileSize), color = Color.Gray, fontSize = 11.sp)
            }
        }

        if (isMultiSelect) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primaryColor else Color.Transparent)
                    .border(width = 1.5.dp, color = if (isSelected) primaryColor else Color.LightGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "checked", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

// RECYCLE BIN ROW
@Composable
fun TrashListItem(
    file: VaultFile,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val primaryColor = Color(0xFF6366F1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val icon = if (file.mimeType.startsWith("video/")) Icons.Default.Videocam else Icons.Default.InsertPhoto
            Icon(imageVector = icon, contentDescription = "type", tint = Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(file.fileName, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("大小：${formatFileSize(file.fileSize)} • 30天后自动清除", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Row {
            IconButton(onClick = onRestore) {
                Icon(imageVector = Icons.Default.SettingsBackupRestore, contentDescription = "restore", tint = primaryColor)
            }
            IconButton(onClick = onDeletePermanently) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "delete permanently", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Helper formatting method
fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val k = 1024L
    val m = k * k
    return when {
        size >= m -> String.format("%.1f MB", size.toDouble() / m)
        size >= k -> String.format("%.1f KB", size.toDouble() / k)
        else -> "$size B"
    }
}
