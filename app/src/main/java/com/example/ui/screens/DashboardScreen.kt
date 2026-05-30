package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Matrix
import com.example.data.model.ProductionRecord
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: ProductionViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allTransportation by viewModel.allTransportationRecords.collectAsState()
    val allMatrices by viewModel.allMatrices.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Calculate analytics
    val totalProduction = allRecords.sumOf { it.afterCap + it.spareCount }
    val activeMatrices = allMatrices.filter { it.status == "Running" }.size
    val totalTarget = allMatrices.sumOf { it.todayTarget }
    val completedPercentage = if (totalTarget > 0) {
        ((totalProduction.toFloat() / totalTarget.toFloat()) * 100f).coerceIn(0f, 100f)
    } else 0f

    val lowProductionCount = allMatrices.filter { it.status == "Low Output" || it.status == "Stopped" }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // --- 1. SYSTEM TOP HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(IndustrialSlate)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Thermal Gear Icon",
                tint = Color(0xFF00B4D8),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HANON TERMINAL",
                    color = IndustrialWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = currentUser?.name ?: "Operator Profile",
                    color = IndustrialLightBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Interactive Cloud Sync Signal Toggle (Golden Touch For MES Systems)
            Surface(
                color = if (isOnline) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isOnline) StatusGreen else StatusRed),
                modifier = Modifier
                    .clickable { viewModel.toggleNetworkConnection() }
                    .padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isOnline) StatusGreen else StatusRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "CLOUD OK" else "OFFLINE",
                        color = IndustrialWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Sign out action
            IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .background(IndustrialAccent.copy(alpha = 0.3f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logoff Unit",
                    tint = IndustrialWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(color = IndustrialAccent.copy(alpha = 0.5f))

        // --- 2. MAIN SCROLLABLE DASHBOARD VIEW ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role display banner
            Surface(
                color = IndustrialSlate.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Seal",
                        tint = StatusGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Access Role: ${currentUser?.role ?: "Guest"} | Department: ${currentUser?.department ?: "Plant"}",
                        color = IndustrialWhite.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // --- Grid of High Contrast Stats Cards ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total Yield
                StatsCard(
                    title = "TODAY OUTPUT",
                    value = "$totalProduction pcs",
                    desc = "Accumulated yield",
                    icon = Icons.Default.Settings,
                    accentColor = IndustrialLightBlue,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Run Progress %
                StatsCard(
                    title = "TARGET PROGRESS",
                    value = String.format(Locale.US, "%.1f%%", completedPercentage),
                    desc = "Goal: $totalTarget pcs",
                    icon = Icons.Default.CheckCircle,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Running Matrices
                StatsCard(
                    title = "ACTIVE RUNS",
                    value = "$activeMatrices / 21",
                    desc = "Matrices online",
                    icon = Icons.Default.Check,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )

                // Card 4: Stopped / Defect Alerts
                StatsCard(
                    title = "STAGE WARNINGS",
                    value = "$lowProductionCount alerts",
                    desc = "Stopped/Low output",
                    icon = Icons.Default.Warning,
                    accentColor = if (lowProductionCount > 0) StatusYellow else IndustrialAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- 3. THE LIVE DECK ACTION HUB (Fast Navigation Buttons) ---
            Text(
                text = "FACTORY DECK OPERATIONS",
                color = IndustrialWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First Row: Line Selection & Live Board
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeckButton(
                        title = "Line Selection",
                        subtitle = "Select Stage & Matrix",
                        icon = Icons.Default.List,
                        color = IndustrialBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("area")
                    }

                    DeckButton(
                        title = "Live Board",
                        subtitle = "All Factory Entries",
                        icon = Icons.Default.Info,
                        color = StatusGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("live")
                    }
                }

                // Second Row: Operator Reports & Cargo Dispatch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeckButton(
                        title = "Operator Reports",
                        subtitle = "Export Sheets",
                        icon = Icons.Default.Edit,
                        color = StatusYellow,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("reports")
                    }

                    DeckButton(
                        title = "Cargo Dispatch",
                        subtitle = "Log outbound status",
                        icon = Icons.Default.Share,
                        color = IndustrialLightBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("transportation")
                    }
                }

                // Third Row: Inventory Deck & Personnel Mgmt (Admin Exclusive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeckButton(
                        title = "Inventory Deck",
                        subtitle = "Dynamic Ledger",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF00B4D8),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("inventory")
                    }

                    if (currentUser?.role == "Admin") {
                        DeckButton(
                            title = "Personnel Mgmt",
                            subtitle = "Admin Auth Desk",
                            icon = Icons.Default.Person,
                            color = SystemWarning,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo("employee_mgmt")
                        }
                    } else {
                        // Empty spacer to balance the layout perfectly
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- 4. THE STOCKPILE INVENTORY LEDGER ---
            Text(
                text = "REAL-TIME MODEL STOCKPILE LEDGER",
                color = IndustrialWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("stockpile_ledger_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Warehouse Inventory Balance Sheets",
                        color = IndustrialWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Remaining Balance = Sum(Produced Output) - Sum(Outbound Dispatched)",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    val radiatorModelsList = listOf("SP2i GA", "HR 1.0", "MX-5 Hybrid", "Radiator-X", "Hanon Elite")
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        radiatorModelsList.forEach { model ->
                            val modelProduced = allRecords.filter { it.modelName == model }.sumOf { it.afterCap + it.spareCount }
                            val modelTransported = allTransportation.filter { it.modelName == model }.sumOf { it.quantity }
                            val modelRemaining = (modelProduced - modelTransported).coerceAtLeast(0)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (modelRemaining < 100) StatusYellow else StatusGreen,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = model,
                                            color = IndustrialWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Display computations
                                    Text(
                                        text = "Prd: $modelProduced | Dsp: $modelTransported | Bal: $modelRemaining pcs",
                                        color = if (modelRemaining < 100) StatusYellow else IndustrialLightBlue,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Custom mini-bar showing the remaining stockpile percentage
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.5f))
                                ) {
                                    val ratio = if (modelProduced > 0) {
                                        (modelRemaining.toFloat() / modelProduced.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(ratio)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        IndustrialLightBlue,
                                                        if (modelRemaining < 100) StatusYellow else StatusGreen
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. GRAPH DECK (Custom Dynamic Fluid Canvas Graphs) ---
            Text(
                text = "REAL-TIME MATRIX METRIC ANGLE",
                color = IndustrialWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thermal Matrix Workload Output Profile",
                        color = IndustrialWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Comparative yields registered for Matrix-1 through Matrix-7 based on entries",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Draw our magnificent custom Bar Chart via Canvas
                    MatrixWorkloadBarChart(allRecords = allRecords)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Area Shift Efficiency Gradient",
                        color = IndustrialWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Production Line, CAP Burns, and Final Line comparative logs",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Draw our majestic Shift Line Graph with gradient shading
                    AreaEGradLineChart(allRecords = allRecords)
                }
            }

            // --- 5. LOG CONSOLE TERMINAL FEED (Lively Real-Time MES Logs!) ---
            Text(
                text = "LIVE SYSTEM EVENT STREAM",
                color = IndustrialWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, Color(0xFF1B2F3D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(6.dp)
                                    .background(StatusGreen, CircleShape)
                            )
                            Text(
                                text = "MES_CONSOLE_SHELL_LOGS",
                                color = Color(0xFF39FF14), // Terminal Green
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "STATUS: LISTENING",
                            color = Color.LightGray,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Shell event feed list
                    val scrollState = rememberScrollState()
                    LaunchedEffect(notifications.size) {
                        scrollState.animateScrollTo(0) // dynamic scroll to top for new logs
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        for (log in notifications) {
                            Text(
                                text = log,
                                color = if (log.contains("Warning") || log.contains("⚠️")) StatusYellow else if (log.contains("Severe") || log.contains("🔴")) StatusRed else Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- SUB COMPOSE HELPER: STATS CARD ---
@Composable
fun StatsCard(
    title: String,
    value: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = IndustrialGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = IndustrialWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = IndustrialGray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// --- SUB COMPOSE HELPER: DECK CARD ACTIONS ---
@Composable
fun DeckButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = modifier
            .height(76.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = IndustrialWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = IndustrialGray, fontSize = 10.sp)
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = IndustrialAccent, modifier = Modifier.size(16.dp))
        }
    }
}

// --- CANVAS SUB COMPOSE: MATRIX BAR CHART ---
@Composable
fun MatrixWorkloadBarChart(allRecords: List<ProductionRecord>, modifier: Modifier = Modifier) {
    // Collect output aggregated for MATRIX-1 to MATRIX-7
    val barValues = remember(allRecords) {
        val outputs = FloatArray(7) { 0f }
        for (i in 1..7) {
            val label = "MATRIX-$i"
            val total = allRecords.filter { it.matrix == label }.sumOf { it.afterCap + it.spareCount }
            outputs[i - 1] = total.toFloat()
        }
        outputs
    }

    val maxVal = remember(barValues) {
        val max = barValues.maxOrNull() ?: 100f
        if (max == 0f) 100f else max * 1.15f // margin buffer
    }

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = 7
        val bottomSpacing = 24f
        val leftSpacing = 64f
        val chartWidth = width - leftSpacing
        val chartHeight = height - bottomSpacing
        val spacingBetweenBars = 16f
        val singleBarSpace = (chartWidth / barCount)
        val barWidth = singleBarSpace - spacingBetweenBars

        // 1. Draw grid guidelines
        val gridLinesCount = 4
        for (g in 0..gridLinesCount) {
            val gridY = chartHeight - (g * (chartHeight / gridLinesCount))
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(leftSpacing, gridY),
                end = Offset(width, gridY),
                strokeWidth = 2f
            )
        }

        // 2. Plot Bars
        for (i in 0 until barCount) {
            val value = barValues[i]
            val pctHeight = value / maxVal
            val barDrawHeight = chartHeight * pctHeight
            val barX = leftSpacing + (i * singleBarSpace) + (spacingBetweenBars / 2f)
            val barY = chartHeight - barDrawHeight

            // Gradient brush for radiator metal look
            val brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00B4D8),
                    Color(0xFF0077B6)
                )
            )

            // Draw Bar
            drawRect(
                brush = brush,
                topLeft = Offset(barX, barY),
                size = Size(barWidth, barDrawHeight)
            )

            // Highlight bar outline
            drawRect(
                color = IndustrialWhite.copy(alpha = 0.2f),
                topLeft = Offset(barX, barY),
                size = Size(barWidth, barDrawHeight),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Base line
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(leftSpacing, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 3f
        )
    }

    // Bar Labels Label bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(18.dp))
        for (m in 1..7) {
            Text(
                text = "M$m",
                color = IndustrialGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- CANVAS SUB COMPOSE: AREA STAGE GRADIENT LINE CHART ---
@Composable
fun AreaEGradLineChart(allRecords: List<ProductionRecord>, modifier: Modifier = Modifier) {
    // Dynamic output counts for current Areas
    val areaYVal = remember(allRecords) {
        val prodLine = allRecords.filter { it.area == "Production Line" }.sumOf { it.afterCap + it.spareCount }.toFloat()
        val capBurns = allRecords.filter { it.area == "CAP Burns" }.sumOf { it.afterCap + it.spareCount }.toFloat()
        val finalLine = allRecords.filter { it.area == "Final Line" }.sumOf { it.afterCap + it.spareCount }.toFloat()
        floatArrayOf(prodLine, capBurns, finalLine)
    }

    val maxVal = remember(areaYVal) {
        val max = areaYVal.maxOrNull() ?: 100f
        if (max == 0f) 100f else max * 1.2f
    }

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val bottomSpacing = 24f
        val leftSpacing = 64f
        val chartWidth = width - leftSpacing
        val chartHeight = height - bottomSpacing

        // 1. Grid Lines
        for (g in 0..3) {
            val gridY = chartHeight - (g * (chartHeight / 3f))
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(leftSpacing, gridY),
                end = Offset(width, gridY),
                strokeWidth = 2f
            )
        }

        // Points
        val ptCount = 3
        val distanceX = chartWidth / (ptCount - 1)
        val points = mutableListOf<Offset>()

        for (i in 0 until ptCount) {
            val valIndex = areaYVal[i]
            val py = chartHeight - (chartHeight * (valIndex / maxVal))
            val px = leftSpacing + (i * distanceX)
            points.add(Offset(px, py))
        }

        // Draw area gradient path (shadow) below the curve line
        if (points.size >= 2) {
            val bgPath = Path().apply {
                moveTo(leftSpacing, chartHeight)
                for (p in points) {
                    lineTo(p.x, p.y)
                }
                lineTo(width, chartHeight)
                close()
            }
            drawPath(
                path = bgPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StatusGreen.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )

            // Draw connecting gradient lines
            val linePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = linePath,
                color = StatusGreen,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Circles over knots
            for (p in points) {
                drawCircle(
                    color = IndustrialWhite,
                    radius = 8f,
                    center = p
                )
                drawCircle(
                    color = StatusGreen,
                    radius = 4f,
                    center = p
                )
            }
        }
    }

    // Horizontal Area Head labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Prod Line", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Left)
        Text("CAP Processing", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text("Final Check", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
    }
}
