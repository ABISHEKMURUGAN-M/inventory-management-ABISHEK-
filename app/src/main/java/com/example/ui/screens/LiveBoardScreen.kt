package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductionRecord
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LiveBoardScreen(viewModel: ProductionViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val context = LocalContext.current

    // Filters states
    var searchModelQuery by remember { mutableStateOf("") }
    var selectedFilterShift by remember { mutableStateOf("All") }
    var selectedFilterArea by remember { mutableStateOf("All") }

    // Clicked record for detailed popup modal
    var detailedRecordPopup by remember { mutableStateOf<ProductionRecord?>(null) }

    // Filtered lists
    val filteredRecords = remember(allRecords, searchModelQuery, selectedFilterShift, selectedFilterArea) {
        allRecords.filter { rec ->
            val matchModel = rec.modelName.contains(searchModelQuery, ignoreCase = true) || rec.matrix.contains(searchModelQuery, ignoreCase = true)
            val matchShift = selectedFilterShift == "All" || rec.shift == selectedFilterShift
            val matchArea = selectedFilterArea == "All" || rec.area == selectedFilterArea
            matchModel && matchShift && matchArea
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // App top navbar header
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE PRODUCTION MONITOR",
                        color = IndustrialWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Instant automatic refresh active",
                        color = StatusGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return home",
                        tint = IndustrialWhite
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    Toast.makeText(context, "Production data synchronized.", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
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

        // --- FILTER ACTION CONTROLS PANEL ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialSlate)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input row
            OutlinedTextField(
                value = searchModelQuery,
                onValueChange = { searchModelQuery = it },
                placeholder = { Text("Filter by Radiator Model or Matrix...", color = IndustrialGray.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Lookup", tint = IndustrialLightBlue) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndustrialLightBlue,
                    unfocusedBorderColor = IndustrialAccent,
                    focusedTextColor = IndustrialWhite,
                    unfocusedTextColor = IndustrialWhite,
                    focusedContainerColor = IndustrialNavy,
                    unfocusedContainerColor = IndustrialNavy
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            // Horizontal Filter Chips flow rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shift:", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                listOf("All", "Shift A", "Shift B", "Shift C").forEach { s ->
                    val isSelected = selectedFilterShift == s
                    Surface(
                        onClick = { selectedFilterShift = s },
                        color = if (isSelected) IndustrialBlue else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) IndustrialBlue else IndustrialAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = s,
                            color = IndustrialWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stage:", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                listOf("All", "Production Line", "CAP Burns", "Final Line").forEach { a ->
                    val isSelected = selectedFilterArea == a
                    val chipLabel = when (a) {
                        "Production Line" -> "Assembly"
                        "CAP Burns" -> "Furnace"
                        "Final Line" -> "Finishing"
                        else -> "All"
                    }
                    Surface(
                        onClick = { selectedFilterArea = a },
                        color = if (isSelected) IndustrialBlue else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) IndustrialBlue else IndustrialAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = chipLabel,
                            color = IndustrialWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // --- WORK LIST DATA GRID BOARD TABLE ---
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Empty", tint = IndustrialAccent, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No production match logs found.",
                        color = IndustrialWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shift databases have cached results or requires selecting different filtration bounds.",
                        color = IndustrialGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            // Data table header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131D28))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MODEL", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                Text("MATRIX", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("BEFORE", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                Text("YIELD", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                Text("REJECT", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                Text("FLAG", color = IndustrialGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                items(filteredRecords.size) { index ->
                    val rec = filteredRecords[index]
                    RowLogEntryItem(
                        record = rec,
                        isAdminOrSupervisor = currentUser?.role == "Admin" || currentUser?.role == "Supervisor",
                        onClick = { detailedRecordPopup = rec },
                        onDelete = { viewModel.deleteRecord(rec.id) }
                    )
                }
            }
        }
    }

    // Modal detailed remark card dialog
    if (detailedRecordPopup != null) {
        val rec = detailedRecordPopup!!
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(rec.timestamp))
        AlertDialog(
            onDismissRequest = { detailedRecordPopup = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Log Data Sheet", tint = IndustrialLightBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MES Cycle Log Sheet")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailTextPair("Log ID", "#${rec.id}")
                    DetailTextPair("Timestamp", dateString)
                    DetailTextPair("Shift Name", rec.shift)
                    DetailTextPair("Factory Stage", rec.area)
                    DetailTextPair("Matrix ID", rec.matrix)
                    DetailTextPair("Model Assembly", rec.modelName)
                    DetailTextPair("Before CAP Intake", "${rec.beforeCap} pcs")
                    DetailTextPair("After CAP Yield", "${rec.afterCap} pcs")
                    DetailTextPair("Spare Items", "${rec.spareCount} pcs")
                    DetailTextPair("Rejected Scrap", "${rec.rejectedCount} pcs")
                    DetailTextPair("Output (Total)", "${rec.afterCap + rec.spareCount} pcs")
                    DetailTextPair("Author Badge", rec.employeeId)
                    DetailTextPair("Mailed Synced", if (rec.isSynced) "Yes (Cloud Database OK)" else "Queued offline")
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("remarks".uppercase(), color = IndustrialGray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(
                        text = rec.remarks ?: "No custom operational anomalies reported.",
                        color = IndustrialWhite,
                        fontSize = 13.sp,
                        fontStyle = if (rec.remarks == null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { detailedRecordPopup = null }) {
                    Text("DISMISS", color = IndustrialLightBlue)
                }
            },
            containerColor = IndustrialSlate,
            titleContentColor = IndustrialWhite,
            textContentColor = IndustrialWhite
        )
    }
}

@Composable
fun RowLogEntryItem(
    record: ProductionRecord,
    isAdminOrSupervisor: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedDeleteButton by remember { mutableStateOf(false) }

    val totalYield = record.afterCap + record.spareCount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = record.modelName,
                    color = IndustrialWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = record.shift,
                    color = IndustrialGray,
                    fontSize = 10.sp
                )
            }

            // Matrix + Area
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.matrix,
                    color = IndustrialLightBlue,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (record.area) {
                        "Production Line" -> "Assembly"
                        "CAP Burns" -> "Furnace"
                        "Final Line" -> "Finishing"
                        else -> "Line"
                    },
                    color = IndustrialGray,
                    fontSize = 10.sp
                )
            }

            // Before CAP
            Text(
                text = "${record.beforeCap}",
                color = IndustrialWhite,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.End
            )

            // Success After CAP yield
            Text(
                text = "$totalYield",
                color = StatusGreen,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.End
            )

            // Rejected scrap
            Text(
                text = "${record.rejectedCount}",
                color = if (record.rejectedCount > 10) StatusRed else IndustrialWhite,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (record.rejectedCount > 10) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.End
            )

            // Sync Flag Indicator / Delete Trigger
            Row(
                modifier = Modifier.weight(0.8f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdminOrSupervisor) {
                    IconButton(
                        onClick = { expandedDeleteButton = !expandedDeleteButton },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expandedDeleteButton) Icons.Default.Close else Icons.Default.MoreVert,
                            contentDescription = "Edit Action",
                            tint = IndustrialGray.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Just show Sync Icon
                    Icon(
                        imageVector = if (record.isSynced) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Sync details",
                        tint = if (record.isSynced) StatusGreen else IndustrialAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), thickness = 0.5.dp)

        // Expanded delete confirm slider line for admins/supervisors
        AnimatedVisibility(visible = expandedDeleteButton) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusRed.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confirm deleting entry record #${record.id}?",
                    color = IndustrialWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { expandedDeleteButton = false }) {
                        Text("CANCEL", color = IndustrialWhite, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            onDelete()
                            expandedDeleteButton = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("DELETE", color = IndustrialWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailTextPair(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = IndustrialGray, fontSize = 11.sp)
        Text(text = value, color = IndustrialWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
