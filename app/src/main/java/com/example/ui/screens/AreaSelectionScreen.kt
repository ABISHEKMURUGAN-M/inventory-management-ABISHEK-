package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelectionScreen(viewModel: ProductionViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val allMatrices by viewModel.allMatrices.collectAsState()

    // Calculate metrics per Area
    val prodRecords = allRecords.filter { it.area == "Production Line" }
    val prodOutput = prodRecords.sumOf { it.afterCap + it.spareCount }
    val prodActive = allMatrices.filter { it.area == "Production Line" && it.status == "Running" }.size

    val capRecords = allRecords.filter { it.area == "CAP Burns" }
    val capOutput = capRecords.sumOf { it.afterCap + it.spareCount }
    val capActive = allMatrices.filter { it.area == "CAP Burns" && it.status == "Running" }.size

    val finalRecords = allRecords.filter { it.area == "Final Line" }
    val finalOutput = finalRecords.sumOf { it.afterCap + it.spareCount }
    val finalActive = allMatrices.filter { it.area == "Final Line" && it.status == "Running" }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // Top Navbar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "AREA STAGE CHANNELS",
                    color = IndustrialWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back back",
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select your current factory stage area to report production cycles:",
                color = IndustrialWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Stage 1: Production Line
            AreaStageSelectionItem(
                name = "Production Line",
                description = "Initial radiator core assembly, tube inserting, and header plate clamping configurations.",
                icon = Icons.Default.Settings,
                iconColor = IndustrialLightBlue,
                activeCount = prodActive,
                outputCount = prodOutput,
                onClick = { viewModel.selectArea("Production Line") }
            )

            // Stage 2: CAP Burns
            AreaStageSelectionItem(
                name = "CAP Burns",
                description = "Control-atmosphere brazing furnace (CAB) thermal sintering and furnace curing stage processes.",
                icon = Icons.Default.Build,
                iconColor = SystemWarning,
                activeCount = capActive,
                outputCount = capOutput,
                onClick = { viewModel.selectArea("CAP Burns") }
            )

            // Stage 3: Final Line
            AreaStageSelectionItem(
                name = "Final Line",
                description = "Post-furnace checking, leak testing, visual inspection, fan shadow mounting, and packaging.",
                icon = Icons.Default.CheckCircle,
                iconColor = StatusGreen,
                activeCount = finalActive,
                outputCount = finalOutput,
                onClick = { viewModel.selectArea("Final Line") }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AreaStageSelectionItem(
    name: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    activeCount: Int,
    outputCount: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, IndustrialAccent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = IndustrialWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward arrow",
                    tint = IndustrialAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                color = IndustrialGray.copy(alpha = 0.8f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(StatusGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$activeCount / 7 Matrices Active",
                        color = IndustrialGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Yield Today: ",
                        color = IndustrialGray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$outputCount pcs",
                        color = IndustrialWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
