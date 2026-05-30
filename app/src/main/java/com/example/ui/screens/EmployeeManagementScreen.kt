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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.theme.*
import com.example.viewmodel.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(viewModel: ProductionViewModel) {
    val allEmployees by viewModel.allEmployees.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Toggle registering card
    var showRegistrationForm by remember { mutableStateOf(false) }

    // Forms states
    var newEmpId by remember { mutableStateOf("") }
    var newEmpName by remember { mutableStateOf("") }
    var newEmpDept by remember { mutableStateOf("") }
    var newEmpPass by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf("Worker") }
    var isRoleExpanded by remember { mutableStateOf(false) }

    var selectedShift by remember { mutableStateOf("Shift A") }
    var isShiftExpanded by remember { mutableStateOf(false) }

    fun clearRegistrationForm() {
        newEmpId = ""
        newEmpName = ""
        newEmpDept = ""
        newEmpPass = ""
        selectedRole = "Worker"
        selectedShift = "Shift A"
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
                    text = "PERSONNEL MANAGEMENT",
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
            actions = {
                IconButton(onClick = { showRegistrationForm = !showRegistrationForm }) {
                    Icon(
                        imageVector = if (showRegistrationForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Register New",
                        tint = if (showRegistrationForm) StatusRed else IndustrialLightBlue
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = IndustrialSlate,
                titleContentColor = IndustrialWhite
            )
        )
        HorizontalDivider(color = IndustrialAccent.copy(alpha = 0.4f))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- DROPDOWN CREATE FORM CARD (ANIMATED TOGGLE) ---
            item {
                AnimatedVisibility(visible = showRegistrationForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, IndustrialLightBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "REGISTER NEW PERSONNEL BADGE",
                                color = IndustrialWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )

                            // ID input
                            OutlinedTextField(
                                value = newEmpId,
                                onValueChange = { newEmpId = it },
                                label = { Text("Employee ID Badge (e.g. EMP005)") },
                                leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_register_id_input")
                            )

                            // Name input
                            OutlinedTextField(
                                value = newEmpName,
                                onValueChange = { newEmpName = it },
                                label = { Text("Employee Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_register_name_input")
                            )

                            // Role Selection dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedRole,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Access Authorization Role") },
                                    trailingIcon = {
                                        IconButton(onClick = { isRoleExpanded = !isRoleExpanded }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Expand role list",
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
                                        .clickable { isRoleExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = isRoleExpanded,
                                    onDismissRequest = { isRoleExpanded = false },
                                    modifier = Modifier.background(IndustrialSlate)
                                ) {
                                    listOf("Worker", "Supervisor", "Admin").forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role, color = IndustrialWhite) },
                                            onClick = {
                                                selectedRole = role
                                                isRoleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Active shift dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedShift,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Assigned Operating Shift") },
                                    trailingIcon = {
                                        IconButton(onClick = { isShiftExpanded = !isShiftExpanded }) {
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
                                        focusedTextColor = IndustrialWhite,
                                        unfocusedTextColor = IndustrialWhite,
                                        focusedLabelColor = IndustrialLightBlue,
                                        unfocusedLabelColor = IndustrialGray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isShiftExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = isShiftExpanded,
                                    onDismissRequest = { isShiftExpanded = false },
                                    modifier = Modifier.background(IndustrialSlate)
                                ) {
                                    listOf("Shift A", "Shift B", "Shift C").forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s, color = IndustrialWhite) },
                                            onClick = {
                                                selectedShift = s
                                                isShiftExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Department input
                            OutlinedTextField(
                                value = newEmpDept,
                                onValueChange = { newEmpDept = it },
                                label = { Text("Operating Department / Line Unit") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_register_dept_input")
                            )

                            // Security credential passcode
                            OutlinedTextField(
                                value = newEmpPass,
                                onValueChange = { newEmpPass = it },
                                label = { Text("System Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndustrialLightBlue,
                                    unfocusedBorderColor = IndustrialAccent,
                                    focusedTextColor = IndustrialWhite,
                                    unfocusedTextColor = IndustrialWhite,
                                    focusedLabelColor = IndustrialLightBlue,
                                    unfocusedLabelColor = IndustrialGray
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_register_pass_input")
                            )

                            // Submit register Button
                            Button(
                                onClick = {
                                    if (newEmpId.isBlank() || newEmpName.isBlank() || newEmpPass.isBlank()) {
                                        Toast.makeText(context, "Failed: Provide ID, Name, and Passcode parameters.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val idUpper = newEmpId.trim().uppercase()
                                    if (!idUpper.startsWith("EMP")) {
                                        Toast.makeText(context, "ID format warning: Badges should start with 'EMP'", Toast.LENGTH_SHORT).show()
                                    }

                                    val success = viewModel.createEmployee(
                                        id = idUpper,
                                        name = newEmpName.trim(),
                                        role = selectedRole,
                                        shift = selectedShift,
                                        dept = newEmpDept.trim().ifBlank { "Unassigned" },
                                        pass = newEmpPass
                                    )

                                    if (success) {
                                        Toast.makeText(context, "Badge $idUpper registered successfully!", Toast.LENGTH_SHORT).show()
                                        clearRegistrationForm()
                                        showRegistrationForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_register_submit")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Validate save")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("VALIDATE NEW REGISTER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // --- DECLARED ROSTER HEAD HEADING ---
            item {
                Text(
                    text = "ACTIVE PERSONNEL DEPLOYMENT ROSTER (${allEmployees.size})",
                    color = IndustrialWhite.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // --- MEMBERS LISTING ---
            items(allEmployees) { emp ->
                EmployeeRosterRow(
                    employee = emp,
                    onDelete = {
                        if (emp.employeeId == "EMP001") {
                            Toast.makeText(context, "Access Denied: Master Admin (EMP001) cannot be deleted.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.removeEmployee(emp.employeeId)
                            Toast.makeText(context, "Badge profile deleted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmployeeRosterRow(employee: Employee, onDelete: () -> Unit) {
    var confirmDeleteAlert by remember { mutableStateOf(false) }

    val roleBadgeColor = when (employee.role) {
        "Admin" -> SystemWarning
        "Supervisor" -> StatusYellow
        else -> IndustrialLightBlue
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialSlate),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF223143)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info block
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = roleBadgeColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.name,
                        color = IndustrialWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Badge: ${employee.employeeId} | Dept: ${employee.department}",
                        color = IndustrialGray,
                        fontSize = 11.sp
                    )
                }

                // Delete Button
                IconButton(onClick = { confirmDeleteAlert = !confirmDeleteAlert }) {
                    Icon(
                        imageVector = if (confirmDeleteAlert) Icons.Default.Close else Icons.Default.Delete,
                        contentDescription = "Delete Personnel profile",
                        tint = if (confirmDeleteAlert) IndustrialWhite else StatusRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expand metadata details
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = employee.role.uppercase(),
                    color = roleBadgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(roleBadgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Text(
                    text = employee.shift.uppercase(),
                    color = IndustrialWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "PWD: ${employee.password}",
                    color = IndustrialGray.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            AnimatedVisibility(visible = confirmDeleteAlert) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(StatusRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Confirm deleting profile?",
                        color = IndustrialWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { confirmDeleteAlert = false }) {
                            Text("NO", color = IndustrialWhite, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                onDelete()
                                confirmDeleteAlert = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("CONFIRM DELETION", color = IndustrialWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
