package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransportationRecord
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportationScreen(viewModel: ProductionViewModel) {
    val allTransportationRecords by viewModel.allTransportationRecords.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val radiatorModels = listOf("SP2i GA", "HR 1.0", "MX-5 Hybrid", "Radiator-X", "Hanon Elite")
    
    // Switch tabs: 0 = Form, 1 = Ledger Logs
    var activeTab by remember { mutableIntStateOf(0) }

    // Form inputs states
    var modelState by remember { mutableStateOf(radiatorModels[0]) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    var quantityState by remember { mutableStateOf("100") }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var dateState by remember { mutableStateOf(todayDateStr) }
    var vehicleState by remember { mutableStateOf("") }
    var destinationState by remember { mutableStateOf("") }

    var validationErrorText by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        modelState = radiatorModels[0]
        quantityState = "100"
        dateState = todayDateStr
        vehicleState = ""
        destinationState = ""
        validationErrorText = null
        focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // App top navbar header
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "TRANSPORTATION & LOGISTICS",
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
                        contentDescription = "Return home dashboard",
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

        // TAB SWITCHER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialSlate)
        ) {
            TabButton(
                title = "LOG DISPATCH",
                isActive = activeTab == 0,
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = 0 }
            )
            TabButton(
                title = "TRANSIT LEDGER",
                isActive = activeTab == 1,
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = 1 }
            )
        }

        // CONTENT BY TAB
        if (activeTab == 0) {
            // Form Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Validation error card
                AnimatedVisibility(visible = validationErrorText != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StatusRed, RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning alert", tint = StatusRed)
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
                            text = "OUTBOUND TRANSIT DISPATCH MANIFEST",
                            color = IndustrialGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        // 1. Model Selection (Custom Dropdown)
                        Column {
                            Text(
                                "Select Radiator Model Category",
                                color = IndustrialGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = modelState,
                                    onValueChange = {},
                                    readOnly = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndustrialLightBlue,
                                        unfocusedBorderColor = IndustrialAccent,
                                        focusedTextColor = IndustrialWhite,
                                        unfocusedTextColor = IndustrialWhite
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { modelDropdownExpanded = !modelDropdownExpanded }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown indicator",
                                                tint = IndustrialLightBlue
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { modelDropdownExpanded = true }
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

                        // 2. Transported Quantity
                        Column {
                            Text(
                                "Transport Dispatch Quantity (pcs)",
                                color = IndustrialGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantityState,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { it.isDigit() }) {
                                            quantityState = input
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndustrialLightBlue,
                                        unfocusedBorderColor = IndustrialAccent,
                                        focusedTextColor = IndustrialWhite,
                                        unfocusedTextColor = IndustrialWhite
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("transport_quantity_field")
                                )
                                // Rapid adjust helper buttons
                                RapidAdjustBtn(label = "+50") {
                                    val currentNum = quantityState.toIntOrNull() ?: 0
                                    quantityState = (currentNum + 50).toString()
                                }
                                RapidAdjustBtn(label = "+100") {
                                    val currentNum = quantityState.toIntOrNull() ?: 0
                                    quantityState = (currentNum + 100).toString()
                                }
                            }
                        }

                        // 3. Dispatch Transit Date
                        OutlinedTextField(
                            value = dateState,
                            onValueChange = { dateState = it },
                            label = { Text("Log Date (yyyy-MM-dd)") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = IndustrialLightBlue) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialLightBlue,
                                unfocusedBorderColor = IndustrialAccent,
                                focusedTextColor = IndustrialWhite,
                                unfocusedTextColor = IndustrialWhite,
                                focusedLabelColor = IndustrialLightBlue,
                                unfocusedLabelColor = IndustrialGray
                            ),
                            placeholder = { Text("yyyy-MM-dd") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transport_date_field")
                        )

                        // 4. Vehicle Details
                        OutlinedTextField(
                            value = vehicleState,
                            onValueChange = { vehicleState = it },
                            label = { Text("Vehicle Plate / Heavy Trailer Details") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = IndustrialLightBlue) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialLightBlue,
                                unfocusedBorderColor = IndustrialAccent,
                                focusedTextColor = IndustrialWhite,
                                unfocusedTextColor = IndustrialWhite,
                                focusedLabelColor = IndustrialLightBlue,
                                unfocusedLabelColor = IndustrialGray
                            ),
                            placeholder = { Text("e.g. TRAILER-TRUCK FH-16 / SCANIA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transport_vehicle_field")
                        )

                        // 5. Destination Location
                        OutlinedTextField(
                            value = destinationState,
                            onValueChange = { destinationState = it },
                            label = { Text("Destination Plant / Customer Branch") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndustrialLightBlue) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialLightBlue,
                                unfocusedBorderColor = IndustrialAccent,
                                focusedTextColor = IndustrialWhite,
                                unfocusedTextColor = IndustrialWhite,
                                focusedLabelColor = IndustrialLightBlue,
                                unfocusedLabelColor = IndustrialGray
                            ),
                            placeholder = { Text("e.g. Hanon Plant B Hub, Export Depot") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transport_destination_field")
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { resetForm() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IndustrialWhite),
                        border = BorderStroke(1.5.dp, StatusRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET Form", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val qty = quantityState.toIntOrNull()
                            if (qty == null || qty <= 0) {
                                validationErrorText = "Submission Refused: Dispatch quantity must be a positive number."
                                return@Button
                            }
                            if (vehicleState.isBlank()) {
                                validationErrorText = "Details Missing: Please log Vehicle Plate or Truck model details."
                                return@Button
                            }
                            if (destinationState.isBlank()) {
                                validationErrorText = "Details Missing: Specify Dispatch Destination."
                                return@Button
                            }

                            validationErrorText = null // clear

                            val success = viewModel.submitTransportationRecord(
                                modelName = modelState,
                                quantity = qty,
                                date = dateState,
                                vehicleDetails = vehicleState.trim(),
                                destination = destinationState.trim()
                            )

                            if (success) {
                                Toast.makeText(context, "Log logged into transit database!", Toast.LENGTH_SHORT).show()
                                resetForm()
                                // Move to Ledger tab to view updated records
                                activeTab = 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("transport_submit_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CONFIRM DISPATCH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Ledger Log List Tab
            if (allTransportationRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = IndustrialGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NO COMPLETED SHIPMENTS LOGGED YET",
                            color = IndustrialGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add outbound dispatches in the first tab.",
                            color = IndustrialGray.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allTransportationRecords) { record ->
                        TransportationTicketItem(
                            record = record,
                            canDelete = currentUser?.role != "Worker",
                            onDelete = {
                                viewModel.deleteTransportationRecord(record.id)
                                Toast.makeText(context, "Shipment record purged.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isActive) IndustrialLightBlue else Color.Transparent
    val activeColor = if (isActive) IndustrialLightBlue else IndustrialGray

    Box(
        modifier = modifier
            .clickable { onClick() }
            .border(
                BorderStroke(
                    width = if (isActive) 1.5.dp else 0.dp,
                    color = borderColor
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .background(if (isActive) IndustrialNavy else Color.Transparent)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = activeColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isActive) IndustrialWhite else IndustrialGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun RapidAdjustBtn(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = IndustrialLightBlue.copy(alpha = 0.15f),
        contentColor = IndustrialLightBlue,
        border = BorderStroke(1.dp, IndustrialLightBlue.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Ledger record billing card representing the dispatch manifest / shipping note
@Composable
fun TransportationTicketItem(
    record: TransportationRecord,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF223143)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(IndustrialLightBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Cargo icon", 
                        tint = IndustrialLightBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.modelName,
                            color = IndustrialWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${record.quantity} pcs]",
                            color = StatusGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Transit date: ${record.date}",
                        color = IndustrialGray,
                        fontSize = 11.sp
                    )
                }

                if (canDelete) {
                    IconButton(onClick = { confirmDelete = !confirmDelete }) {
                        Icon(
                            imageVector = if (confirmDelete) Icons.Default.Close else Icons.Default.Delete,
                            contentDescription = "Purge cargo log",
                            tint = if (confirmDelete) IndustrialWhite else StatusRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Transport details rows
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("TRANSPORT VEHICLE", fontSize = 8.sp, color = IndustrialGray, fontWeight = FontWeight.Bold)
                    Text(record.vehicleDetails, fontSize = 11.sp, color = IndustrialWhite, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1.3f)) {
                    Text("DESTINATION LOCATION", fontSize = 8.sp, color = IndustrialGray, fontWeight = FontWeight.Bold)
                    Text(record.destination, fontSize = 11.sp, color = IndustrialWhite, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISPATCHED BY: ${record.employeeId}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = IndustrialGray.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Offline vs synced state badge
                val badgeColor = if (record.isSynced) StatusGreen else StatusYellow
                val badgeTxt = if (record.isSynced) "CLD-SYNCED" else "LOCAL-CACHE"
                Text(
                    text = badgeTxt,
                    color = badgeColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Confirm delete trigger list
            AnimatedVisibility(visible = confirmDelete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(StatusRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Purge shipment from inventory books?", color = IndustrialWhite, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { confirmDelete = false }) {
                            Text("NO", color = IndustrialWhite, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                onDelete()
                                confirmDelete = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("CONFIRM PURGE", color = IndustrialWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
