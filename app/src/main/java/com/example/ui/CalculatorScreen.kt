package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: VaultViewModel,
    onNavigateToSettings: () -> Unit
) {
    val expression by viewModel.calcExpression.collectAsState()
    val historyText by viewModel.calcHistoryText.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()
    val historyList by viewModel.history.collectAsState()

    var isScientificMode by remember { mutableStateOf(false) }
    var showHistoryBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "历史",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    showHistoryBottomSheet = true
                                }
                        )
                        Text(
                            text = "久以计算器",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "关于/设置",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Display Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                if (historyText.isNotEmpty()) {
                    Text(
                        text = historyText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Dynamic text size depending on expression length
                val displayFontSize = when {
                    expression.length > 22 -> 22.sp
                    expression.length > 17 -> 28.sp
                    expression.length > 12 -> 36.sp
                    expression.length > 8 -> 44.sp
                    else -> 54.sp
                }

                Text(
                    text = expression,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = displayFontSize,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    lineHeight = displayFontSize * 1.1f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_display")
                )
            }

            // Standard vs Scientific custom capsule segmented controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.widthIn(max = 240.dp)
                ) {
                    SegmentedButton(
                        selected = !isScientificMode,
                        onClick = { isScientificMode = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("标准", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = isScientificMode,
                        onClick = { isScientificMode = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("科学", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Keypad Grid layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (isScientificMode) 8.dp else 10.dp)
            ) {
                if (!isScientificMode) {
                    // Standard Keypad (4 columns)
                    // Row 1: AC, (), ⌫, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalcButton(text = "AC", isOperator = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("AC") }
                        CalcButton(text = "( )", isOperator = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("( )") }
                        CalcButton(text = "⌫", isOperator = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("⌫") }
                        CalcButton(text = "÷", isAction = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("÷") }
                    }

                    // Row 2: 7, 8, 9, ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalcButton(text = "7", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("7") }
                        CalcButton(text = "8", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("8") }
                        CalcButton(text = "9", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("9") }
                        CalcButton(text = "×", isAction = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("×") }
                    }

                    // Row 3: 4, 5, 6, −
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalcButton(text = "4", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("4") }
                        CalcButton(text = "5", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("5") }
                        CalcButton(text = "6", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("6") }
                        CalcButton(text = "−", isAction = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("−") }
                    }

                    // Row 4: 1, 2, 3, +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalcButton(text = "1", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("1") }
                        CalcButton(text = "2", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("2") }
                        CalcButton(text = "3", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("3") }
                        CalcButton(text = "+", isAction = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("+") }
                    }

                    // Row 5: %, 0, ., =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CalcButton(text = "%", isOperator = true, modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("%") }
                        CalcButton(text = "0", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress("0") }
                        CalcButton(text = ".", modifier = Modifier.weight(1f)) { viewModel.handleCalcKeyPress(".") }
                        CalcButton(
                            text = "=",
                            isSecondaryAction = true,
                            modifier = Modifier.weight(1f),
                            testTag = "vault_trigger"
                        ) { viewModel.handleCalcKeyPress("=") }
                    }
                } else {
                    // Scientific Keypad (5 columns, high density)
                    // Row 1: Deg/Rad, sin, cos, tan, ⌫
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(
                            text = if (isDegreeMode) "Deg" else "Rad",
                            isOperator = true,
                            modifier = Modifier.weight(1f),
                            isScientific = true
                        ) { viewModel.toggleDegreeMode() }
                        CalcButton(text = "sin", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("sin") }
                        CalcButton(text = "cos", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("cos") }
                        CalcButton(text = "tan", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("tan") }
                        CalcButton(text = "⌫", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("⌫") }
                    }

                    // Row 2: ln, log, √, ^, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "ln", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("ln") }
                        CalcButton(text = "log", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("log") }
                        CalcButton(text = "√", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("√") }
                        CalcButton(text = "^", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("^") }
                        CalcButton(text = "÷", isAction = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("÷") }
                    }

                    // Row 3: π, e, (, ), ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "π", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("π") }
                        CalcButton(text = "e", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("e") }
                        CalcButton(text = "(", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("(") }
                        CalcButton(text = ")", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress(")") }
                        CalcButton(text = "×", isAction = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("×") }
                    }

                    // Row 4: 7, 8, 9, −, !
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "7", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("7") }
                        CalcButton(text = "8", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("8") }
                        CalcButton(text = "9", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("9") }
                        CalcButton(text = "−", isAction = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("−") }
                        CalcButton(text = "!", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("!") }
                    }

                    // Row 5: 4, 5, 6, +, %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "4", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("4") }
                        CalcButton(text = "5", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("5") }
                        CalcButton(text = "6", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("6") }
                        CalcButton(text = "+", isAction = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("+") }
                        CalcButton(text = "%", isOperator = true, modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("%") }
                    }

                    // Row 6: 1, 2, 3, 0, .
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "1", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("1") }
                        CalcButton(text = "2", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("2") }
                        CalcButton(text = "3", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("3") }
                        CalcButton(text = "0", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress("0") }
                        CalcButton(text = ".", modifier = Modifier.weight(1f), isScientific = true) { viewModel.handleCalcKeyPress(".") }
                    }

                    // Row 7: AC, =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcButton(text = "AC", isOperator = true, modifier = Modifier.weight(2f), isScientific = true) { viewModel.handleCalcKeyPress("AC") }
                        CalcButton(
                            text = "=",
                            isSecondaryAction = true,
                            modifier = Modifier.weight(3f),
                            isScientific = true,
                            testTag = "vault_trigger"
                        ) { viewModel.handleCalcKeyPress("=") }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for displaying clean history lists
    if (showHistoryBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistoryBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "计算历史记录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            viewModel.clearCalculationHistory()
                        }
                    ) {
                        Text("清空历史记录", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无计算历史记录",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(historyList) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Load historical expression directly to display
                                        viewModel.loadExpression(item.expression, "= ${item.result}")
                                        showHistoryBottomSheet = false
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = item.expression,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "= ${item.result}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showHistoryBottomSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("完成", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isAction: Boolean = false,
    isSecondaryAction: Boolean = false,
    isScientific: Boolean = false,
    testTag: String? = null,
    onClick: () -> Unit
) {
    val containerColor = when {
        isAction -> MaterialTheme.colorScheme.primary
        isSecondaryAction -> MaterialTheme.colorScheme.secondary
        isOperator -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isAction -> MaterialTheme.colorScheme.onPrimary
        isSecondaryAction -> MaterialTheme.colorScheme.onSecondary
        isOperator -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = if (isScientific) 18.sp else 23.sp,
        fontWeight = FontWeight.Bold
    )

    val buttonModifier = if (testTag != null) {
        modifier.testTag(testTag)
    } else {
        modifier
    }

    val vibrationEnabled = com.example.LocalVibrationEnabled.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    Card(
        shape = RoundedCornerShape(if (isScientific) 16.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = buttonModifier
            .height(if (isScientific) 52.dp else 72.dp)
            .clickable(
                onClick = {
                    if (vibrationEnabled) {
                        try {
                            // Method 1: Hardware-bound platform keyboard click feedback (bypasses notification restriction)
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        } catch (e: Exception) {}

                        try {
                            // Method 2: System service vibrator with custom tuning
                            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            }

                            val attrs = android.media.AudioAttributes.Builder()
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .build()

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                // Prefer createOneShot as createPredefined(EFFECT_CLICK) is often silent or ignored on several custom Android ROMs
                                vibrator.vibrate(
                                    android.os.VibrationEffect.createOneShot(55, android.os.VibrationEffect.DEFAULT_AMPLITUDE),
                                    attrs
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(55)
                            }
                        } catch (e: Exception) {
                            // Method 3: Compose core fallback if hardware vibrator fails
                            try {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            } catch (e2: Exception) {}
                        }
                    }
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = textStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}
