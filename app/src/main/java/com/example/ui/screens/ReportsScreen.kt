package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.ReportExporter
import com.example.viewmodel.ProductionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ProductionViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Filters states
    var selectedReportShift by remember { mutableStateOf("All") }
    var selectedReportArea by remember { mutableStateOf("All") }
    var selectedReportMatrix by remember { mutableStateOf("All") }
    var selectedReportModel by remember { mutableStateOf("All") }
    var selectedDateRange by remember { mutableStateOf("All") } // All, Today, Last 7 Days, Last 30 Days
    var selectedMonthFilter by remember { mutableStateOf("All") } // All, Current Month

    // Dropdown toggle states
    var isShiftDropdownExpanded by remember { mutableStateOf(false) }
    var isAreaDropdownExpanded by remember { mutableStateOf(false) }
    var isMatrixDropdownExpanded by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var isDateDropdownExpanded by remember { mutableStateOf(false) }
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }

    // Backup & Restore state
    var showBackupDialog by remember { mutableStateOf(false) }
    var importTextValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Filtered data entries
    val matchedRecords = remember(allRecords, selectedReportShift, selectedReportArea, selectedReportMatrix, selectedReportModel, selectedDateRange, selectedMonthFilter) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        allRecords.filter { rec ->
            val matchShift = selectedReportShift == "All" || rec.shift == selectedReportShift
            val matchArea = selectedReportArea == "All" || rec.area == selectedReportArea
            val matchMatrix = selectedReportMatrix == "All" || rec.matrix == selectedReportMatrix
            val matchModel = selectedReportModel == "All" || rec.modelName == selectedReportModel
            
            val matchDateRange = when (selectedDateRange) {
                "Today" -> rec.date == todayStr
                "Last 7 Days" -> rec.timestamp >= (now - (7 * oneDayMs))
                "Last 30 Days" -> rec.timestamp >= (now - (30 * oneDayMs))
                else -> true
            }

            val matchMonth = when (selectedMonthFilter) {
                "Current Month" -> rec.date.startsWith(currentMonthPrefix)
                else -> true
            }

            matchShift && matchArea && matchMatrix && matchModel && matchDateRange && matchMonth
        }
    }

    // Tabulate metrics
    val totalBefore = matchedRecords.sumOf { it.beforeCap }
    val totalAfter = matchedRecords.sumOf { it.afterCap }
    val totalSpare = matchedRecords.sumOf { it.spareCount }
    val totalRejected = matchedRecords.sumOf { it.rejectedCount }
    
    val grandTotalOutput = totalAfter + totalSpare
    val rejectPercentage = if (totalBefore > 0) {
        ((totalRejected.toFloat() / totalBefore.toFloat()) * 100f).coerceIn(0f, 100f)
    } else 0f

    val areaSummaryTitle = if (selectedReportArea == "All") "Overall Factory" else selectedReportArea
    
    // Dynamic description heading detailing all active filters
    val reportHeadingText = remember(selectedReportShift, selectedReportArea, selectedReportMatrix, selectedReportModel, selectedDateRange, selectedMonthFilter) {
        val parts = mutableListOf<String>()
        parts.add(areaSummaryTitle)
        if (selectedReportShift != "All") parts.add("Shift $selectedReportShift")
        if (selectedReportMatrix != "All") parts.add(selectedReportMatrix)
        if (selectedReportModel != "All") parts.add(selectedReportModel)
        if (selectedDateRange != "All") parts.add(selectedDateRange)
        if (selectedMonthFilter != "All") parts.add("Month: $selectedMonthFilter")
        parts.joinToString(" • ")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // App Nav Bar Header
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "REPORTS & SHEETS EXPORT",
                    color = IndustrialWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return dashboard",
                        tint = IndustrialWhite
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = IndustrialSlate,
                titleContentColor = IndustrialWhite
            )
        )
        HorizontalDivider(color = IndustrialAccent.copy(alpha = 0.4f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Generate daily or shift-level reports, and export to CSV (Excel compatible) or HTML templates instantly:",
                color = IndustrialWhite.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // --- FILTER CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "REPORT FILTRATION ENVELOPE",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // ROW 1: SHIFT & AREA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Shift Filter dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedReportShift,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Shift", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isShiftDropdownExpanded = !isShiftDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand shifts filter list",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isShiftDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isShiftDropdownExpanded,
                                onDismissRequest = { isShiftDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                listOf("All", "Shift A", "Shift B", "Shift C").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, color = IndustrialWhite) },
                                        onClick = {
                                            selectedReportShift = s
                                            isShiftDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Area Filter dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedReportArea,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Area Stage", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isAreaDropdownExpanded = !isAreaDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand area filter list",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAreaDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isAreaDropdownExpanded,
                                onDismissRequest = { isAreaDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                listOf("All", "Production Line", "CAP Burns", "Final Line").forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a, color = IndustrialWhite) },
                                        onClick = {
                                            selectedReportArea = a
                                            isAreaDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ROW 2: MATRIX & MODEL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Matrix filter dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedReportMatrix,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Matrix", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isMatrixDropdownExpanded = !isMatrixDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand matrix filter list",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isMatrixDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isMatrixDropdownExpanded,
                                onDismissRequest = { isMatrixDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                val matrixOptions = listOf("All") + (1..7).map { "MATRIX-$it" }
                                matrixOptions.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m, color = IndustrialWhite) },
                                        onClick = {
                                            selectedReportMatrix = m
                                            isMatrixDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Model filter dropdown (Dynamically generated from actual records)
                        Box(modifier = Modifier.weight(1f)) {
                            val activeModels = remember(allRecords) {
                                listOf("All") + allRecords.map { it.modelName }.distinct().filter { it.isNotBlank() }
                            }
                            OutlinedTextField(
                                value = selectedReportModel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Product Model", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isModelDropdownExpanded = !isModelDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand model filter list",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isModelDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isModelDropdownExpanded,
                                onDismissRequest = { isModelDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                activeModels.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m, color = IndustrialWhite) },
                                        onClick = {
                                            selectedReportModel = m
                                            isModelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ROW 3: DATE RANGE & MONTH FILTER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Range Filter dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedDateRange,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Time Span", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isDateDropdownExpanded = !isDateDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand date filters",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDateDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isDateDropdownExpanded,
                                onDismissRequest = { isDateDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                listOf("All", "Today", "Last 7 Days", "Last 30 Days").forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range, color = IndustrialWhite) },
                                        onClick = {
                                            selectedDateRange = range
                                            isDateDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Month Filter dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedMonthFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Month Range", fontSize = 11.sp) },
                                trailingIcon = {
                                    IconButton(onClick = { isMonthDropdownExpanded = !isMonthDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand month filters",
                                            tint = IndustrialLightBlue
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isMonthDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isMonthDropdownExpanded,
                                onDismissRequest = { isMonthDropdownExpanded = false },
                                modifier = Modifier.background(IndustrialSlate)
                            ) {
                                listOf("All", "Current Month").forEach { mFilt ->
                                    DropdownMenuItem(
                                        text = { Text(mFilt, color = IndustrialWhite) },
                                        onClick = {
                                            selectedMonthFilter = mFilt
                                            isMonthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- STATS SUMMARY SUMMARY BLOCK ---
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "METRIC ANALYTICS: SUMMARY",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = reportHeadingText,
                        color = IndustrialLightBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    RowTextReportPair(label = "Input Assemblies", value = "$totalBefore pcs")
                    RowTextReportPair(label = "Successful CAP Cures", value = "$totalAfter pcs")
                    RowTextReportPair(label = "Auxiliary Spares Yield", value = "$totalSpare pcs")
                    RowTextReportPair(label = "Defect Discards (Scrap)", value = "$totalRejected pcs", highlightColor = StatusRed)
                    RowTextReportPair(label = "Scrap Defect Rate", value = String.format(Locale.US, "%.2f%%", rejectPercentage), highlightColor = if (rejectPercentage > 10) StatusRed else StatusYellow)

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                    RowTextReportPair(
                        label = "Net Qualified Output",
                        value = "$grandTotalOutput pcs",
                        highlightColor = StatusGreen,
                        isLarge = true
                    )
                }
            }

            // --- ACTION BUTTON EXPORTS DECK ---
            Text(
                text = "EXPORT SHEETS PACK",
                color = IndustrialWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Export Button A: CSV
                Button(
                    onClick = {
                        if (matchedRecords.isEmpty()) {
                            Toast.makeText(context, "No matched records to export.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val csvContent = ReportExporter.generateCSV(matchedRecords)
                        val fileName = "Hanon_Production_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
                        ReportExporter.shareReportFile(context, fileName, csvContent, isHtml = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("export_csv_btn")
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "CSV Grid")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Export Button B: HTML layout (Natively shares/prints beautiful sheets)
                Button(
                    onClick = {
                        if (matchedRecords.isEmpty()) {
                            Toast.makeText(context, "No matched records to print.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val htmlContent = ReportExporter.generateHTMLReport(
                            title = "Hanon Systems - $reportHeadingText",
                            records = matchedRecords
                        )
                        val fileName = "Hanon_Report_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.html"
                        ReportExporter.shareReportFile(context, fileName, htmlContent, isHtml = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("export_html_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "PDF / HTML Print")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT PDF/HTML", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Role disclaimer tag
            Text(
                text = "Note: Share access matches your local enterprise configuration. Reports are audited automatically with operator badge signature.",
                color = IndustrialGray.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            // --- ENTERPRISE BACKUP & DATA PROTECTION SYSTEM ---
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StatusYellow.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Security Snapshots", tint = StatusYellow)
                        Text(
                            text = "MES INDUSTRIAL DATA SAFEGUARD",
                            color = StatusYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Take complete encrypted snapshots of the local operator terminal state database, or restore state during physical terminal replacement.",
                        color = IndustrialWhite.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export Backup Button
                        Button(
                            onClick = {
                                scope.launch {
                                    val backupString = com.example.utils.BackupHelper.exportBackup(context)
                                    if (backupString != null) {
                                        val fileName = "Hanon_Database_Backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.json"
                                        ReportExporter.shareReportFile(context, fileName, backupString, isHtml = false)
                                    } else {
                                        Toast.makeText(context, "Error compiling system payload.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSlate),
                            border = BorderStroke(1.dp, IndustrialLightBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export Backup", tint = IndustrialLightBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXPORT SNAPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialLightBlue)
                        }

                        // Import Backup Button
                        Button(
                            onClick = {
                                importTextValue = ""
                                showBackupDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSlate),
                            border = BorderStroke(1.dp, StatusYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Import Backup", tint = StatusYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESTORE SNAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusYellow)
                        }
                    }
                }
            }

            if (showBackupDialog) {
                AlertDialog(
                    onDismissRequest = { showBackupDialog = false },
                    title = {
                        Text(
                            text = "RECOVER SYSTEM STATE",
                            color = IndustrialWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Paste your previously exported JSON database backup string below to restore all operator logins, production lines, and logs.",
                                color = IndustrialGray,
                                fontSize = 12.sp
                            )
                            OutlinedTextField(
                                value = importTextValue,
                                onValueChange = { importTextValue = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                label = { Text("JSON Payload") },
                                placeholder = { Text("Paste JSON snapshot here...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedContainerColor = IndustrialSlate,
                                    unfocusedContainerColor = IndustrialSlate
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (importTextValue.isBlank()) {
                                    Toast.makeText(context, "Please paste database backup snapshot first.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    val success = com.example.utils.BackupHelper.importBackup(context, importTextValue.trim())
                                    if (success) {
                                        Toast.makeText(context, "Database payload restored. Restarting terminal state...", Toast.LENGTH_LONG).show()
                                        showBackupDialog = false
                                        // Force UI metrics reload by navigating out to refresh triggers
                                        viewModel.navigateTo("dashboard")
                                    } else {
                                        Toast.makeText(context, "Failed parsing JSON payload. Invalid backup signature.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                        ) {
                            Text("RESTORE STATE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("ABORT", color = StatusRed)
                        }
                    },
                    containerColor = IndustrialSlate,
                    textContentColor = IndustrialWhite,
                    titleContentColor = IndustrialWhite
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun RowTextReportPair(
    label: String,
    value: String,
    highlightColor: Color = IndustrialWhite,
    isLarge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = IndustrialGray,
            fontSize = if (isLarge) 13.sp else 12.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = highlightColor,
            fontSize = if (isLarge) 18.sp else 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
