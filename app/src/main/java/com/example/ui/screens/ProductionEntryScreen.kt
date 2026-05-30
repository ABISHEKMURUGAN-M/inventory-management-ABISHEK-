package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionEntryScreen(viewModel: ProductionViewModel) {
    val selectedArea by viewModel.selectedArea.collectAsState()
    val selectedMatrix by viewModel.selectedMatrix.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Forms States
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd (EEEE)", Locale.US).format(Date()) }

    var shiftState by remember { mutableStateOf(currentUser?.shift ?: "Shift A") }
    var shiftDropdownExpanded by remember { mutableStateOf(false) }

    val radiatorModels = listOf("SP2i GA", "HR 1.0", "MX-5 Hybrid", "Radiator-X", "Hanon Elite")
    var modelState by remember { mutableStateOf(radiatorModels[0]) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    // Counts States (Numbers)
    var beforeCap by remember { mutableIntStateOf(0) }
    var afterCap by remember { mutableIntStateOf(0) }
    var spareCount by remember { mutableIntStateOf(0) }
    var rejectedCount by remember { mutableIntStateOf(0) }

    var remarksState by remember { mutableStateOf("") }

    // Validation Alert Visibility
    var validationErrorText by remember { mutableStateOf<String?>(null) }

    fun doFormReset() {
        beforeCap = 0
        afterCap = 0
        spareCount = 0
        rejectedCount = 0
        remarksState = ""
        validationErrorText = null
        focusManager.clearFocus()
        Toast.makeText(context, "Form inputs cleared.", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // Navbar header
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PRODUCTION INPUT",
                        color = IndustrialWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${selectedMatrix?.matrixId ?: ""} • ${selectedArea ?: ""}",
                        color = Color(0xFF00B4D8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("matrix") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return Matrix selection",
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
            // Form validation alert block
            AnimatedVisibility(visible = validationErrorText != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StatusRed, RoundedCornerShape(8.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert logo", tint = StatusRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validationErrorText ?: "",
                            color = IndustrialWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // SECTION 1: AUTO READ-ONLY DATA METADATA
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "METALS CONTEXT READOUTS",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RowValueBlock("Reporting Date", todayDateStr, Icons.Default.DateRange, modifier = Modifier.weight(1.3f))
                        RowValueBlock("Factory Stage", selectedArea ?: "N/A", Icons.Default.LocationOn, modifier = Modifier.weight(1.2f))
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        RowValueBlock("Matrix Segment", selectedMatrix?.matrixId ?: "N/A", Icons.Default.Settings, modifier = Modifier.weight(1.3f))
                        RowValueBlock("Active Submitter", currentUser?.employeeId ?: "N/A", Icons.Default.Person, modifier = Modifier.weight(1.2f))
                    }
                }
            }

            // SECTION 2: SHIFT AND MODEL SELECTORS (DROPDOWNS)
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "SHIFT & PRODUCT SELECTION",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Shift selection dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = shiftState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Operational Shift") },
                            trailingIcon = {
                                IconButton(onClick = { shiftDropdownExpanded = !shiftDropdownExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand shift list",
                                        tint = IndustrialLightBlue
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialLightBlue,
                                unfocusedBorderColor = IndustrialAccent,
                                focusedLabelColor = IndustrialLightBlue,
                                unfocusedLabelColor = IndustrialGray,
                                focusedTextColor = IndustrialWhite,
                                unfocusedTextColor = IndustrialWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shiftDropdownExpanded = true }
                                .testTag("shift_selection_dropdown")
                        )
                        DropdownMenu(
                            expanded = shiftDropdownExpanded,
                            onDismissRequest = { shiftDropdownExpanded = false },
                            modifier = Modifier.background(IndustrialSlate)
                        ) {
                            listOf("Shift A", "Shift B", "Shift C").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s, color = IndustrialWhite) },
                                    onClick = {
                                        shiftState = s
                                        shiftDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Model Name Selection Menu dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = modelState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Radiator Model Core") },
                            trailingIcon = {
                                IconButton(onClick = { modelDropdownExpanded = !modelDropdownExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand model list",
                                        tint = IndustrialLightBlue
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialLightBlue,
                                unfocusedBorderColor = IndustrialAccent,
                                focusedLabelColor = IndustrialLightBlue,
                                unfocusedLabelColor = IndustrialGray,
                                focusedTextColor = IndustrialWhite,
                                unfocusedTextColor = IndustrialWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { modelDropdownExpanded = true }
                                .testTag("model_selection_dropdown")
                        )
                        DropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false },
                            modifier = Modifier.background(IndustrialSlate)
                        ) {
                            radiatorModels.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode, color = IndustrialWhite) },
                                    onClick = {
                                        modelState = mode
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: NUMERIC QUANTITIES (Before CAP, After CAP, Spares, Rejected)
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "WORKPIECE NUMERICAL INPUT COUNTS",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Quantity 1: Before CAP
                    CountRapidAdjuster(
                        label = "Before CAP Input Assemblies",
                        count = beforeCap,
                        accentColor = IndustrialLightBlue,
                        onCountChanged = { beforeCap = it.coerceAtLeast(0) }
                    )

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f))

                    // Quantity 2: After CAP
                    CountRapidAdjuster(
                        label = "After CAP Successful Cures",
                        count = afterCap,
                        accentColor = StatusGreen,
                        onCountChanged = { afterCap = it.coerceAtLeast(0) }
                    )

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f))

                    // Quantity 3: Spares
                    CountRapidAdjuster(
                        label = "Spare Auxiliary Count",
                        count = spareCount,
                        accentColor = StatusYellow,
                        onCountChanged = { spareCount = it.coerceAtLeast(0) }
                    )

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f))

                    // Quantity 4: Rejected count
                    CountRapidAdjuster(
                        label = "Defective Discards / Rejected Count",
                        count = rejectedCount,
                        accentColor = StatusRed,
                        onCountChanged = { rejectedCount = it.coerceAtLeast(0) }
                    )
                }
            }

            // SECTION 4: TEXT REMARKS
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF233245))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = remarksState,
                        onValueChange = { remarksState = it },
                        label = { Text("Log Remarks (Optional anomalies or pauses)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndustrialLightBlue,
                            unfocusedBorderColor = IndustrialAccent,
                            focusedTextColor = IndustrialWhite,
                            unfocusedTextColor = IndustrialWhite,
                            focusedLabelColor = IndustrialLightBlue,
                            unfocusedLabelColor = IndustrialGray
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("remarks_input_field")
                    )
                }
            }

            // ACTION BOX: SUBMIT & CLEAR BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clear reset
                OutlinedButton(
                    onClick = { doFormReset() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialWhite),
                    border = BorderStroke(1.5.dp, StatusRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("form_reset_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Trash delete", tint = StatusRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }

                // Submitter Save
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // 1. Validate numbers
                        if (beforeCap <= 0 && afterCap <= 0) {
                            validationErrorText = "Submission Failed: Counts cannot remain empty or zero."
                            return@Button
                        }
                        if (afterCap > beforeCap) {
                            validationErrorText = "Yield Discrepancy: After CAP count ($afterCap) cannot exceed incoming assemblies count ($beforeCap)."
                            return@Button
                        }
                        
                        validationErrorText = null // clear

                        val isSuccess = viewModel.submitProductionRecord(
                            shift = shiftState,
                            modelName = modelState,
                            beforeCap = beforeCap,
                            afterCap = afterCap,
                            spareCount = spareCount,
                            rejectedCount = rejectedCount,
                            remarks = remarksState
                        )

                        if (isSuccess) {
                            Toast.makeText(context, "Log synchronized successfully!", Toast.LENGTH_SHORT).show()
                            // Go straight to live board to view incoming data
                            viewModel.navigateTo("live")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("form_submit_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Upload Cloud")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE ENTRY", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Sub element block value
@Composable
fun RowValueBlock(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = IndustrialLightBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = IndustrialGray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = IndustrialWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// COUNT RAPID ADJUSTER ROW (No keyboards needed!)
@Composable
fun CountRapidAdjuster(
    label: String,
    count: Int,
    accentColor: Color,
    onCountChanged: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = IndustrialWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Numeric Output Display
            Text(
                text = count.toString(),
                color = accentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .width(70.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            // Adjuster button cluster
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Decrement button (-10)
                RapidBtn(symbol = "-10", color = Color.Gray) { onCountChanged(count - 10) }
                // Decrement button (-1)
                RapidBtn(symbol = "-1", color = Color.Gray) { onCountChanged(count - 1) }
                // Increment button (+1)
                RapidBtn(symbol = "+1", color = accentColor) { onCountChanged(count + 1) }
                // Increment button (+10)
                RapidBtn(symbol = "+10", color = accentColor) { onCountChanged(count + 10) }
            }
        }
    }
}

@Composable
fun RapidBtn(symbol: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(height = 36.dp, width = 48.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
