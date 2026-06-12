package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

val LocalVibrationEnabled = staticCompositionLocalOf { true }

class MainActivity : ComponentActivity() {
    private var activeViewModel: VaultViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VaultViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(LocalVibrationEnabled provides vibrationEnabled) {
                    activeViewModel = viewModel

                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val showBiometricPrompt by viewModel.showBiometricPrompt.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                    // Safe Area edge transitions
                    when (currentScreen) {
                        AppScreen.CALCULATOR -> {
                            CalculatorScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = {
                                    viewModel.navigateTo(AppScreen.SECURITY_SETTINGS)
                                }
                            )
                        }
                        AppScreen.VAULT_EXPLORER, AppScreen.TRASH -> {
                            VaultScreen(
                                viewModel = viewModel,
                                onNavigateBackToCalc = {
                                    viewModel.navigateTo(AppScreen.CALCULATOR)
                                }
                            )
                        }
                        AppScreen.SETTINGS -> {
                            // This is the Simulated/Explanatory "Depth Saving" information screen
                            // accessible via direct main screen, back goes back to calculator entry
                            VaultScreen(
                                viewModel = viewModel,
                                onNavigateBackToCalc = {
                                    viewModel.navigateTo(AppScreen.CALCULATOR)
                                }
                            )
                        }
                        AppScreen.SECURITY_SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateTo(AppScreen.CALCULATOR)
                                }
                            )
                        }
                    }

                    // Simulated Biometrics (Fingerprint) Validator Dialog Card
                    if (showBiometricPrompt) {
                        BiometricSimulationDialog(
                            onSuccess = { viewModel.dismissBiometricAndAccess(true) },
                            onCancel = { viewModel.dismissBiometricAndAccess(false) }
                        )
                    }
                }
            }
        }
    }
}

    override fun onStop() {
        super.onStop()
        // Anti-leak screen background exit auto-locking!
        activeViewModel?.onBackgrounded()
    }
}

@Composable
fun BiometricSimulationDialog(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "fingerprint validation",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .clickable { onSuccess() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "久以安全屏内指纹验证",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请轻触或点击指纹传感器进行安全隐私身份核实以解锁进入隐私空间中。",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("取消验证", color = Color.Gray)
                    }
                    Button(
                        onClick = onSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = "mock unlock")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("点击模拟通过")
                    }
                }
            }
        }
    }
}
