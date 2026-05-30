package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Matrix
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixSelectionScreen(viewModel: ProductionViewModel) {
    val selectedArea by viewModel.selectedArea.collectAsState()
    val allMatrices by viewModel.allMatrices.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter matrices for current selected Area
    val filteredMatrices = remember(selectedArea, allMatrices) {
        allMatrices.filter { it.area == selectedArea }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy)
    ) {
        // Toggle Header Navbar
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MATRIX SELECTION GRID",
                        color = IndustrialWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedArea ?: "Stage Area Selection",
                        color = Color(0xFF00B4D8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("area") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return back",
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
                .padding(16.dp)
        ) {
            Text(
                text = "Select an active radiator manufacturing matrix to update assembly count:",
                color = IndustrialWhite.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid Layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMatrices) { matrix ->
                    // Calculate individual yields for this specific Matrix in this Area
                    val matrixRecords = allRecords.filter { it.area == matrix.area && it.matrix == matrix.matrixId }
                    val matrixOutput = matrixRecords.sumOf { it.afterCap + it.spareCount }

                    val progressPercent = if (matrix.todayTarget > 0) {
                        ((matrixOutput.toFloat() / matrix.todayTarget.toFloat())).coerceIn(0f, 1f)
                    } else 0f

                    MatrixGridItem(
                        matrix = matrix,
                        currentOutput = matrixOutput,
                        progress = progressPercent,
                        isAdminOrSupervisor = currentUser?.role == "Admin" || currentUser?.role == "Supervisor",
                        onSelect = { viewModel.selectMatrix(matrix) },
                        onPowerToggle = { viewModel.toggleMatrixPower(matrix) }
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixGridItem(
    matrix: Matrix,
    currentOutput: Int,
    progress: Float,
    isAdminOrSupervisor: Boolean,
    onSelect: () -> Unit,
    onPowerToggle: () -> Unit
) {
    val statusColor = when (matrix.status) {
        "Running" -> StatusGreen
        "Low Output" -> StatusYellow
        "Stopped" -> StatusRed
        else -> IndustrialAccent
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (matrix.status == "Stopped") StatusRed.copy(alpha = 0.6f) else IndustrialAccent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("matrix_grid_item_${matrix.matrixId}")
            .clickable(enabled = matrix.status != "Stopped") { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Status Indicator Light + ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = matrix.matrixId,
                        color = IndustrialWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Power Toggle for Admins/Supervisors (Stops/Runs Machinery)
                if (isAdminOrSupervisor) {
                    IconButton(
                        onClick = { onPowerToggle() },
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (matrix.status == "Stopped") StatusRed.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Override Power",
                            tint = if (matrix.status == "Stopped") StatusRed else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Yield metrics
            Text(
                text = "Today Yield:",
                color = IndustrialGray,
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$currentOutput pcs",
                    color = IndustrialWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/ ${matrix.todayTarget}",
                    color = IndustrialGray.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Status Tag label
            Text(
                text = matrix.status.uppercase(),
                color = statusColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Little Progress Meter Bar
            LinearProgressIndicator(
                progress = { progress },
                color = if (matrix.status == "Low Output") StatusYellow else StatusGreen,
                trackColor = Color.DarkGray.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // If stopped, overlay message
            if (matrix.status == "Stopped") {
                Text(
                    text = "HALTED by supervisor",
                    color = StatusRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Tap to open input form",
                    color = IndustrialGray.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }
    }
}
