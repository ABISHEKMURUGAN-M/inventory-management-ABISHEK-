package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDashboardScreen(viewModel: ProductionViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val allTransportation by viewModel.allTransportationRecords.collectAsState()

    val radiatorModels = listOf("SP2i GA", "HR 1.0", "MX-5 Hybrid", "Radiator-X", "Hanon Elite")
    
    // Search + Filter states
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyLowStock by remember { mutableStateOf(false) }
    var stockThreshold by remember { mutableFloatStateOf(150f) }

    // Computations
    val modelLedgers = remember(allRecords, allTransportation, searchQuery, showOnlyLowStock, stockThreshold) {
        radiatorModels.map { model ->
            val produced = allRecords.filter { it.modelName == model }.sumOf { it.afterCap + it.spareCount }
            val transported = allTransportation.filter { it.modelName == model }.sumOf { it.quantity }
            val remaining = (produced - transported).coerceAtLeast(0)
            val isLowStock = remaining < stockThreshold.toInt()

            ModelStockLedger(
                modelName = model,
                produced = produced,
                transported = transported,
                remaining = remaining,
                isLowStock = isLowStock
            )
        }.filter { ledger ->
            val matchSearch = ledger.modelName.contains(searchQuery, ignoreCase = true)
            val matchLowStock = !showOnlyLowStock || ledger.isLowStock
            matchSearch && matchLowStock
        }
    }

    val totalProducedSum = remember(allRecords) {
        allRecords.sumOf { it.afterCap + it.spareCount }
    }
    val totalTransportedSum = remember(allTransportation) {
        allTransportation.sumOf { it.quantity }
    }
    val totalRemainingSum = (totalProducedSum - totalTransportedSum).coerceAtLeast(0)
    val lowStockCount = radiatorModels.count { model ->
        val prod = allRecords.filter { it.modelName == model }.sumOf { it.afterCap + it.spareCount }
        val trans = allTransportation.filter { it.modelName == model }.sumOf { it.quantity }
        (prod - trans).coerceAtLeast(0) < stockThreshold.toInt()
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
                    text = "REALTIME INVENTORY DASHBOARD",
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
                        contentDescription = "Return home",
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

        // OVERVIEW SCOREBOARD DECK
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialSlate)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card A: Total Inventory Balance
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialNavy),
                border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("WARHOUSE STOCK", color = IndustrialGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$totalRemainingSum pcs", color = IndustrialWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total remaining balance", color = IndustrialGray, fontSize = 9.sp)
                }
            }

            // Card B: Logistics Dispatch Outbound
            Card(
                colors = CardDefaults.cardColors(containerColor = IndustrialNavy),
                border = BorderStroke(1.dp, IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DISPATCHED", color = IndustrialLightBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$totalTransportedSum pcs", color = IndustrialWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Offloaded from floor", color = IndustrialGray, fontSize = 9.sp)
                }
            }

            // Card C: Low Stock Warning triggers
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStockCount > 0) StatusRed.copy(alpha = 0.15f) else IndustrialNavy
                ),
                border = BorderStroke(1.dp, if (lowStockCount > 0) StatusRed else IndustrialAccent.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("LOW LEVEL ALARMS", color = if (lowStockCount > 0) StatusRed else StatusYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$lowStockCount warnings", color = if (lowStockCount > 0) StatusRed else StatusGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Critical factory lines", color = IndustrialGray, fontSize = 9.sp)
                }
            }
        }

        // TOOLBAR & FILTERS SEARCH PANEL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialSlate)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search radiator models...", color = IndustrialGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IndustrialWhite,
                        unfocusedTextColor = IndustrialWhite,
                        focusedContainerColor = IndustrialNavy,
                        unfocusedContainerColor = IndustrialNavy,
                        focusedBorderColor = IndustrialAccent,
                        unfocusedBorderColor = IndustrialAccent.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                )

                // Filter Low Stock switch
                FilterChip(
                    selected = showOnlyLowStock,
                    onClick = { showOnlyLowStock = !showOnlyLowStock },
                    label = { Text("Low Stock Only", color = IndustrialWhite, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusRed.copy(alpha = 0.3f),
                        containerColor = IndustrialNavy
                    ),
                    modifier = Modifier.weight(0.7f)
                )
            }

            // Slider to tweak the Dynamic Stock Threshold value in real time
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alarm Threshold: ${stockThreshold.toInt()} units",
                    color = IndustrialGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = stockThreshold,
                    onValueChange = { stockThreshold = it },
                    valueRange = 50f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = IndustrialLightBlue,
                        activeTrackColor = IndustrialLightBlue,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
            }
        }

        // STOCK LEDGER ITEMS CARDS LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modelLedgers) { ledger ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (ledger.isLowStock) StatusRed.copy(alpha = 0.7f) else IndustrialAccent.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ledger_item_${ledger.modelName}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (ledger.isLowStock) StatusRed else StatusGreen,
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ledger.modelName,
                                    color = IndustrialWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (ledger.isLowStock) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, StatusRed),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = "🔴 LOW STOCK WARNING",
                                        color = StatusRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "STOCK SECURE",
                                    color = StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quantitative matrix sheets breakdown bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Produced", color = IndustrialGray, fontSize = 10.sp)
                                Text("${ledger.produced} units", color = IndustrialWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dispatched", color = IndustrialLightBlue, fontSize = 10.sp)
                                Text("${ledger.transported} units", color = IndustrialWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Remaining", color = if (ledger.isLowStock) StatusRed else StatusGreen, fontSize = 10.sp)
                                Text("${ledger.remaining} units", color = IndustrialWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Progress proportion loader bar
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.DarkGray.copy(alpha = 0.5f))
                        ) {
                            val ratio = if (ledger.produced > 0) {
                                (ledger.remaining.toFloat() / ledger.produced.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .background(if (ledger.isLowStock) StatusRed else StatusGreen)
                            )
                        }
                    }
                }
            }

            if (modelLedgers.isEmpty()) {
                item {
                    Text(
                        text = "No matching stock balances detected for the filter criteria.",
                        color = IndustrialGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(30.dp)
                    )
                }
            }
        }
    }
}

data class ModelStockLedger(
    val modelName: String,
    val produced: Int,
    val transported: Int,
    val remaining: Int,
    val isLowStock: Boolean
)
