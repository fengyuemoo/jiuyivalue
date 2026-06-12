package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DepthDownloadScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit
) {
    var selectedSimSource by remember { mutableStateOf("微信") }
    var simStatusText by remember { mutableStateOf("") }
    var isSimulating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val darkSurface = Color(0xFF1E293B)
    val darkBg = Color(0xFF0F172A)
    val primaryColor = Color(0xFF6366F1) // Indigo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("返回空间", color = primaryColor)
            }
            Text(
                text = "“深度保存 / 下载”功能方案分析",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Technical explanation card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = darkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "深度保存是否可行？",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "在系统原生层面，第三方应用无法直接侵入修改其他应用（如微信、Telegram）内的原生“保存”按钮。但我们可以通过在系统后台监听或接管，完成类似的安全替代方案。下面是行业主流方案优缺点剖析：",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Comparison
                ComparisonRow(
                    title = "1. 分享重定向（推荐 - 100%安全合规）",
                    desc = "用户在微信选中图片，点击系统“分享”，选择“久以安全深度保存”。应用接收到文件后，直接将其物理存储移动到安全内部空间，原路径不留痕。",
                    pro = "优点：完全合规，不依赖高级系统权限，支持双端，零变砖封号风险。",
                    con = "缺点：需要用户多操作一步分享。"
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComparisonRow(
                    title = "2. 无障碍辅助接管（Accessibility Service）",
                    desc = "开启特殊辅助服务后，在系统屏幕检测到微信“保存”或“下载”文字按钮点击时光速拦截逻辑，后台自动捕获该图片并将之导入隐藏夹，然后删除公共相册原生文件。",
                    pro = "优点：真正的免用户多余操作接管按钮。",
                    con = "缺点：权限极其敏感，部分手机会被系统警告或自动杀死辅助服务。"
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComparisonRow(
                    title = "3. 剪贴板中转监听（Clipboard Sync）",
                    desc = "当用户在外部应用复制一个下载直链或文件数据，久以计算器瞬间探知格式，自动触发静默冷下载直接灌入保险箱。",
                    pro = "优点：极快的一键下载触发，流畅体验。",
                    con = "缺点：Android 11及以上版本对后台剪贴板有严格获取频率限制。"
                )
            }
        }

        // Sandbox Simulator Section
        Text(
            text = "功能交互效果演练场",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = darkSurface),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "在此可以模拟从外部第三方软件直接“深度下载/保存”到隐私保险箱中。模拟成功后，您将在【久以隐私空间】的默认文件夹看到它，而公共相册完全不受影响！",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "第一步：选择想要模拟的外部第三方应用",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val apps = listOf("微信", "QQ", "Telegram", "浏览器")
                    apps.forEach { appName ->
                        val isSel = selectedSimSource == appName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) primaryColor else Color.Black.copy(alpha = 0.3f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSel) primaryColor else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSimSource = appName }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(appName, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "第二步：模拟下载包含敏感信息的文件",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isSimulating = true
                        simStatusText = "正在监听自第三方应用的深度拦截事件..."
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1000)
                            simStatusText = "检测到【$selectedSimSource】下载请求！已安全代理接管..."
                            kotlinx.coroutines.delay(1000)
                            simStatusText = "拦截本地文件：depth_${selectedSimSource.lowercase()}_secure_asset.png"
                            // save mock item
                            viewModel.simDepthDownload(
                                name = "depth_${selectedSimSource}_secure_asset.png",
                                sizeBytes = 1024 * 512,
                                mimeType = "image/png"
                            )
                            kotlinx.coroutines.delay(800)
                            simStatusText = "深度下载成功！文件已存入本地久以隐私空间，公共相册无足迹。"
                            isSimulating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSimulating
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "download")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始模拟【$selectedSimSource】一键深度下载")
                }

                AnimatedVisibility(visible = simStatusText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = "state",
                                tint = if (isSimulating) Color.Yellow else Color.Green,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = simStatusText,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(
    title: String,
    desc: String,
    pro: String,
    con: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(desc, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(pro, color = Color(0xFF4ADE80), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(con, color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
