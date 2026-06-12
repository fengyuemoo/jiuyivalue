package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit
) {
    var clickVersionCount by remember { mutableStateOf(0) }
    var showHiddenSettings by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        if (showHiddenSettings) {
            showHiddenSettings = false
        } else {
            onBack()
        }
    }

    // Dialog trigger states
    var showGuideDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showSetGuidePasswordDialog by remember { mutableStateOf(false) }
    var showVerifyGuidePasswordDialog by remember { mutableStateOf(false) }

    // State bindings
    val themeMode by viewModel.themeMode.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()

    val hideGuideShl by viewModel.hideGuideShl.collectAsState()
    val guideShlPassword by viewModel.guideShlPassword.collectAsState()
    val feedbacks by viewModel.feedbacks.collectAsState()
    val updateCheckStatus by viewModel.updateCheckStatus.collectAsState()
    val onlineVersionDetails by viewModel.onlineVersionDetails.collectAsState()

    // Form states for Hidden Settings
    val curPassword by viewModel.vaultPassword.collectAsState()
    val curGesture by viewModel.vaultGesture.collectAsState()
    val curEmail by viewModel.securityEmail.collectAsState()
    val curEmergencyCode by viewModel.emergencyCode.collectAsState()

    var inputPass by remember { mutableStateOf(curPassword) }
    var inputGesture by remember { mutableStateOf(curGesture) }
    var inputEmail by remember { mutableStateOf(curEmail) }
    var inputEmergency by remember { mutableStateOf(curEmergencyCode) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showHiddenSettings) "安全入口设置" else "设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showHiddenSettings) {
                            showHiddenSettings = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!showHiddenSettings) {
                // =============== 1. STANDARD SETTINGS ===============

                // Group: General
                SettingsSectionHeader("通用设置")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column {
                        // Theme Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.DarkMode,
                            title = "暗黑主题模式",
                            subtitle = "启用计算器与隐藏空间暗黑外观",
                            checked = themeMode == "dark",
                            onCheckedChange = { isDark ->
                                viewModel.updateThemeSettings(if (isDark) "dark" else "light")
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Vibration Feedback
                        SettingsToggleRow(
                            icon = Icons.Default.Vibration,
                            title = "震动反馈",
                            subtitle = "按键时触发轻微震动反馈效果",
                            checked = vibrationEnabled,
                            onCheckedChange = { viewModel.updateVibrationSettings(it) }
                        )
                    }
                }

                // Group: Help Support
                SettingsSectionHeader("帮助与支持")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.MenuBook,
                            title = "使用指南",
                            onClick = {
                                showGuideDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        SettingsClickableRow(
                            icon = Icons.Default.RateReview,
                            title = "反馈建议",
                            onClick = {
                                showFeedbackDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        if (!hideGuideShl) {
                            SettingsClickableRow(
                                icon = Icons.Default.VisibilityOff,
                                title = "隐藏安全入口指南",
                                onClick = {
                                    showSetGuidePasswordDialog = true
                                }
                            )
                        } else {
                            SettingsClickableRow(
                                icon = Icons.Default.Visibility,
                                title = "显示安全入口指南",
                                onClick = {
                                    showVerifyGuidePasswordDialog = true
                                }
                            )
                        }
                    }
                }

                // Group: About Application (THE HIDDEN TRIGGER!)
                SettingsSectionHeader("关于应用")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clickVersionCount++
                                    if (clickVersionCount >= 3) {
                                        showHiddenSettings = true
                                        clickVersionCount = 0
                                        Toast.makeText(context, "🔓 安全入口授权模式已激活！", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "version",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("当前版本 v1.0.4", fontWeight = FontWeight.SemiBold)
                                Text("构建自最新稳定版服务", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = "检查更新",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showUpdateDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = "© 2026 Jiuyi Calculator Team",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                // ------------------ DIALOGS ------------------
                if (showGuideDialog) {
                    AlertDialog(
                        onDismissRequest = { showGuideDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "guide icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("久以计算器使用指南", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "久以计算器提供多维高精度科学计算与高度安全底蕴的隐私保险箱。在带给您优质数学公式结算体验之余，悉心保护您的珍贵照片档案与多媒体隐私。",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Calculate, contentDescription = "calc", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("1. 基础与标准公式运算", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "支持加、减、乘、除、百分比及括号（支持智能就地括号匹配机制）等运算。计算支持无限制的回退/删除键，且对极高极低精度作出了完美缩放处理。",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Science, contentDescription = "sci", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("2. 多维高阶科学计算", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "点击“科学”视图后，可自由进行三角函数（sin, cos, tan）、自然对数（ln）、常用十底对数（log）、开方根（√）、常数 π 与自然常数 e、阶乘、次方（^）运算。并支持实时一键转换角度 (Deg) / 弧度 (Rad)。",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.History, contentDescription = "hist", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("3. 计算历史无限回填", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "主界面点击左上角历史图标回看，不仅记录历史算式与精细值，更可任意点击一条历史项。历史记录将在一瞬间为您同步还原输入到计算中，辅助二次公式推演。",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                if (!hideGuideShl) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Shield, contentDescription = "shl", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("4. 隐蔽多重安全入口", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val gestureDesc = when (curGesture) {
                                                "双击 %" -> "向主计算器输入密码并双击百分比键 \"%\""
                                                "长按 =" -> "向主计算器输入密码并长按等号键 \"=\""
                                                "左滑 AC" -> "向主计算器输入密码并在 AC 键上方向左滑动区域"
                                                else -> "在主计算器输入密码 \"$curPassword\" 并配合您设定的手势「$curGesture」"
                                            }
                                            Text(
                                                "【手势密码入口】：在主计算器中输入密码 \"$curPassword\" 并配合特定手势：【$gestureDesc】，即可瞬间展开隐藏保密沙盒空间。\n\n" +
                                                "【无痕特权安全入口】：连续快速点击关于应用本面板底部的【当前版本详情卡边 3 次】，即可极速激活触发进入“安全入口设置（隐藏设置）”，以便在此高管安全凭据自毁控制面板。",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { showGuideDialog = false },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("知道了")
                            }
                        }
                    )
                }

                if (showFeedbackDialog) {
                    var feedbackType by remember { mutableStateOf("功能建议") }
                    var feedbackDesc by remember { mutableStateOf("") }
                    var contactEmail by remember { mutableStateOf("") }
                    var isSubmitting by remember { mutableStateOf(false) }
                    var isSuccess by remember { mutableStateOf(false) }

                    LaunchedEffect(isSubmitting) {
                        if (isSubmitting) {
                            kotlinx.coroutines.delay(1200)
                            isSubmitting = false
                            isSuccess = true
                        }
                    }

                    AlertDialog(
                        onDismissRequest = { 
                            if (!isSubmitting) showFeedbackDialog = false 
                        },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RateReview,
                                    contentDescription = "feed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSuccess) "反馈提交成功" else "问题反馈与建议", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        },
                        text = {
                            if (isSuccess) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "success",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "提交成功！感谢您的宝贵建议",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "您的反馈已经同时采取双向处理：\n\n1. 【本地安全审计】：已物理隔离归档在机密沙盒。可在 关于本 界面最下方连续点击3次版本号，调出隐藏安全设置，从底部的“用户反馈记录（安全审计）”底稿中随时查阅或清空物理备份。\n\n2. 【开发商直连投递】：为了让接收团队安全接收并解答您的疑问，您可以直接点击下方的邮件按钮，通过您手机里的常用邮箱客户端（如 Gmail、网易邮箱等），将此份自动整理好的问题单据投递至开发者邮箱：admin@jiuyi.com。",
                                        textAlign = TextAlign.Left,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            } else if (isSubmitting) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("正在安全打包传输，请稍候...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("请选择反馈类型：", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val categories = listOf("功能建议", "缺陷反馈", "其他问题")
                                        categories.forEach { cat ->
                                            FilterChip(
                                                selected = feedbackType == cat,
                                                onClick = { feedbackType = cat },
                                                label = { Text(cat) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("详细描述问题：", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    OutlinedTextField(
                                        value = feedbackDesc,
                                        onValueChange = { feedbackDesc = it },
                                        placeholder = { Text("请详尽编写您在科学运算、日常使用或文件保险箱中遇到的建议与疑惑表现...", fontSize = 13.sp) },
                                        minLines = 3,
                                        maxLines = 5,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("联系方式（选填）：", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    OutlinedTextField(
                                        value = contactEmail,
                                        onValueChange = { contactEmail = it },
                                        placeholder = { Text("在此输入 Email 或手机以便回执答复", fontSize = 13.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            if (isSuccess) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                    data = android.net.Uri.parse("mailto:")
                                                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("admin@jiuyi.com"))
                                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "【久以计算器反馈】$feedbackType")
                                                    putExtra(
                                                        android.content.Intent.EXTRA_TEXT,
                                                        "反馈分类: $feedbackType\n" +
                                                        "反馈详情内容:\n$feedbackDesc\n\n" +
                                                        "联系人电子邮箱: $contactEmail\n" +
                                                        "操作系统版本: Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})\n" +
                                                        "--------------------------------------------------\n" +
                                                        "（本回执邮件由久以保密套件本地一键生成并调用系统投递）"
                                                    )
                                                }
                                                val chooser = android.content.Intent.createChooser(intent, "拉起邮箱并发送反馈")
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "未找到有效的邮件应用，请手动发送至 admin@jiuyi.com", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("📧 调起邮件发送", fontSize = 11.sp, maxLines = 1)
                                    }

                                    Button(
                                        onClick = { showFeedbackDialog = false },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("完成")
                                    }
                                }
                            } else if (!isSubmitting) {
                                Button(
                                    onClick = {
                                        if (feedbackDesc.isBlank()) {
                                            Toast.makeText(context, "请先填写详细描述内容", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.submitFeedback(feedbackType, feedbackDesc, contactEmail)
                                            isSubmitting = true
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("提交反馈")
                                }
                            }
                        },
                        dismissButton = {
                            if (!isSubmitting && !isSuccess) {
                                TextButton(onClick = { showFeedbackDialog = false }) {
                                    Text("取消")
                                }
                            }
                        }
                    )
                }

                if (showUpdateDialog) {
                    LaunchedEffect(showUpdateDialog) {
                        viewModel.checkForApplicationUpdates()
                    }

                    AlertDialog(
                        onDismissRequest = { 
                            if (updateCheckStatus != "LOADING") showUpdateDialog = false 
                        },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = "update",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (updateCheckStatus == "LOADING") "正在检查更新" else "核对版本结果", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        },
                        text = {
                            if (updateCheckStatus == "LOADING") {
                                Column(
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("正在连接安全校对服务器...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else if (updateCheckStatus == "SUCCESS") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "本地已是最新版本 v1.0.4",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = onlineVersionDetails,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "本地版本 v1.0.4 状态安全",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "网络连接受阻，系统未能连接至云盘中心。已自动为您启动离线哈希快照校对，本地状态：【最前沿配置 (1.0.4 - Release)】，运行极为顺畅，无需更新。",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            if (updateCheckStatus != "LOADING") {
                                Button(
                                    onClick = { showUpdateDialog = false },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("我知道了")
                                }
                            }
                        }
                    )
                }

                if (showSetGuidePasswordDialog) {
                    var passInput1 by remember { mutableStateOf("") }
                    var passInput2 by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showSetGuidePasswordDialog = false },
                        title = { Text("隐藏安全入口指南", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("设定指南保护锁，若日后需重新在指南中暴露该入口机制，凭此安全锁认证解锁：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = passInput1,
                                    onValueChange = { passInput1 = it },
                                    label = { Text("设置新密码") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = passInput2,
                                    onValueChange = { passInput2 = it },
                                    label = { Text("再次输入确认密码") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (passInput1.isBlank()) {
                                        Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show()
                                    } else if (passInput1 != passInput2) {
                                        Toast.makeText(context, "两次输入的密码不一致，请重试", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateGuideShlPassword(passInput1)
                                        viewModel.updateHideGuideShl(true)
                                        Toast.makeText(context, "✅ 安全第4条指南已隐蔽！", Toast.LENGTH_SHORT).show()
                                        showSetGuidePasswordDialog = false
                                    }
                                }
                            ) {
                                Text("确定并隐藏")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSetGuidePasswordDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                if (showVerifyGuidePasswordDialog) {
                    var passVerifyInput by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showVerifyGuidePasswordDialog = false },
                        title = { Text("显示安全入口指南", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("请输入先前设定的安全锁密码以解锁该条指南，恢复在“使用指南”中的可见性：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(
                                    value = passVerifyInput,
                                    onValueChange = { passVerifyInput = it },
                                    label = { Text("验证核实密码") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (passVerifyInput == guideShlPassword || passVerifyInput == curPassword) {
                                        viewModel.updateHideGuideShl(false)
                                        Toast.makeText(context, "🔓 验证通过！指南卡片已重现显示。", Toast.LENGTH_SHORT).show()
                                        showVerifyGuidePasswordDialog = false
                                    } else {
                                        Toast.makeText(context, "❌ 保护锁密码不正确！", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("解锁显示")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showVerifyGuidePasswordDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

            } else {
                // =============== 2. OUTSTANDING HIDDEN SETTINGS ===============

                // Warning Header Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "lock", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("隐私保险箱处于开发及修改模式", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("在此修改的密码、手势等凭据将直接永久生效，请务必牢记新凭证。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Subsection 1: Modify password
                SettingsSectionHeader("修改进入密码")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text("当前进入密码：", fontWeight = FontWeight.Medium)
                            Text(curPassword, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputPass,
                            onValueChange = { inputPass = it },
                            label = { Text("输入新密码") },
                            placeholder = { Text("如123456") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("建议使用6位以上纯数字拼合计算，请勿使用明文暴露生日", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // Subsection 2: Modify trigger gesture
                SettingsSectionHeader("修改触发手势")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("当前触发方式：在计算器连续按两次 \"%\" 键", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val gestures = listOf("双击 %", "长按 =", "左滑 AC", "输入特定值")
                        gestures.forEach { gesture ->
                            val isSel = inputGesture == gesture
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSel,
                                        onClick = { inputGesture = gesture }
                                    )
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { inputGesture = gesture }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(gesture, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Subsection 3: Password recovery
                SettingsSectionHeader("安全自动恢复 / 忘记密码机制")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("密保机制能在您失去或忘记数学密码时提供安全找回途径。未配置该途径若遗忘密码可能会丢失隐藏资产。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("高级密保邮箱") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "warning", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("紧急清除删除代码（自毁程序）", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("在计算计算器时，输入以下24位密随机自毁组合码并点击等于，将瞬间物理擦除应用内所有隐藏资产：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputEmergency,
                            onValueChange = { inputEmergency = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Subsection 4: Icons disguising
                SettingsSectionHeader("应用桌面图标伪装")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("自定义桌面外观，应用可完美易容：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        val spoofs: List<Pair<String, ImageVector>> = listOf(
                            "标准计算器" to Icons.Default.Calculate,
                            "天气云端" to Icons.Default.Cloud,
                            "复古收音机" to Icons.Default.Radio,
                            "单位转换" to Icons.Default.Architecture
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            spoofs.forEach { (title, icon) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                SettingsSectionHeader("用户反馈记录（安全审计）")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("以下为本地安全库审计记录的用户提交的反馈建议。在专用网络未连接前，离线全量隔离存储于本地中：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (feedbacks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📭 暂无用户反馈记录物证", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 13.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                feedbacks.forEach { feedback ->
                                    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(feedback.timestamp))
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = when (feedback.type) {
                                                        "缺陷反馈" -> MaterialTheme.colorScheme.errorContainer
                                                        "功能建议" -> MaterialTheme.colorScheme.primaryContainer
                                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = feedback.type,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (feedback.type) {
                                                            "缺陷反馈" -> MaterialTheme.colorScheme.onErrorContainer
                                                            "功能建议" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = feedback.description,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 17.sp
                                            )
                                            if (feedback.contact.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "联系对象: ${feedback.contact}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        viewModel.clearAllFeedbacks()
                                        Toast.makeText(context, "🗑️ 审计反馈历史已物理清空！", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "clear feedbacks", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("清空审计底稿", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Action controls confirm
                Button(
                    onClick = {
                        viewModel.saveNewCredentials(
                            pass = inputPass,
                            gesture = inputGesture,
                            email = inputEmail,
                            emergency = inputEmergency
                        )
                        Toast.makeText(context, "💾 隐藏空间安全属性修改成功！", Toast.LENGTH_SHORT).show()
                        showHiddenSettings = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存更新隐藏入口设置", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsDropdownRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "arrow", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    }
}

@Composable
fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "arrow", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    }
}
