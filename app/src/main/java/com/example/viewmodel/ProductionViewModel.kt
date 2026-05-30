package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Employee
import com.example.data.model.Matrix
import com.example.data.model.ProductionRecord
import com.example.data.model.TransportationRecord
import com.example.data.repository.ProductionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ProductionRepository(
        database.employeeDao(),
        database.productionRecordDao(),
        database.matrixDao(),
        database.transportationRecordDao()
    )

    // Auth State
    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Navigation and Selection State
    private val _currentScreen = MutableStateFlow("splash") // splash, login, dashboard, area, matrix, entry, live, reports, employee_mgmt
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedArea = MutableStateFlow<String?>(null)
    val selectedArea: StateFlow<String?> = _selectedArea.asStateFlow()

    private val _selectedMatrix = MutableStateFlow<Matrix?>(null)
    val selectedMatrix: StateFlow<Matrix?> = _selectedMatrix.asStateFlow()

    // Network Sync State (Interactive Toggle)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Notification State Logs
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // Database state mappings
    val allRecords: StateFlow<List<ProductionRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransportationRecords: StateFlow<List<TransportationRecord>> = repository.allTransportationRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatrices: StateFlow<List<Matrix>> = repository.allMatrices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        try {
            com.example.data.repository.FirebaseSyncHelper.initialize(application)
        } catch (_: Exception) {}
        viewModelScope.launch {
            try {
                // Seed base entities if empty at start
                repository.seedDatabaseAsNeeded()
            } catch (e: Throwable) {
                android.util.Log.e("ProductionViewModel", "Failed to seed database: ${e.message}", e)
            }
            // Add some base alert logs
            _notifications.value = listOf(
                "System Initialized Successfully.",
                "Line Active: 21 Matrices running on Shift-A status.",
                "Pre-populated credential profiles (Admin, Supervisor, Worker)."
            )
        }
    }

    // Toggle Online/Offline State manually
    fun toggleNetworkConnection() {
        val current = _isOnline.value
        _isOnline.value = !current
        if (!current) {
            // Reconnected, attempt sync
            viewModelScope.launch {
                val syncCount = repository.syncOfflineEntries()
                if (syncCount > 0) {
                    addLog("Network connection restored. Sync'd $syncCount cached entries to Cloud.")
                }
            }
        } else {
            addLog("App switched to Offline Mode. Submissions will cache in local Room DB.")
        }
    }

    fun addLog(msg: String) {
        val currentLogs = _notifications.value.toMutableList()
        currentLogs.add(0, "[${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}] $msg")
        _notifications.value = currentLogs.take(30) // limit to latest 30 logs
    }

    // Navigation Helpers
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectArea(area: String) {
        _selectedArea.value = area
        navigateTo("matrix")
    }

    fun selectMatrix(matrix: Matrix) {
        _selectedMatrix.value = matrix
        navigateTo("entry")
    }

    fun goBackToAreaSelection() {
        _selectedMatrix.value = null
        navigateTo("area")
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        navigateTo("login")
    }

    // Auth Actions
    fun login(employeeId: String, passwordEntered: String) {
        viewModelScope.launch {
            _loginError.value = null
            val emp = repository.getEmployeeById(employeeId.trim().uppercase())
            if (emp == null) {
                _loginError.value = "Employee ID not found. Ensure ID format is e.g. EMP001"
            } else if (emp.password != passwordEntered) {
                _loginError.value = "Incorrect password. Attempt reset or contact Admin."
            } else {
                _currentUser.value = emp
                addLog("Login Success: ${emp.name} has joined ${emp.shift}")
                navigateTo("dashboard")
            }
        }
    }

    // Actions
    fun submitProductionRecord(
        shift: String,
        modelName: String,
        beforeCap: Int,
        afterCap: Int,
        spareCount: Int,
        rejectedCount: Int,
        remarks: String
    ): Boolean {
        val area = _selectedArea.value ?: return false
        val matrixObj = _selectedMatrix.value ?: return false
        val user = _currentUser.value ?: return false

        viewModelScope.launch {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val currentDateStr = format.format(Date())

            val record = ProductionRecord(
                date = currentDateStr,
                shift = shift,
                area = area,
                matrix = matrixObj.matrixId,
                modelName = modelName,
                beforeCap = beforeCap,
                afterCap = afterCap,
                spareCount = spareCount,
                rejectedCount = rejectedCount,
                remarks = remarks.ifBlank { null },
                employeeId = user.employeeId,
                timestamp = System.currentTimeMillis(),
                isSynced = _isOnline.value
            )

            repository.addProductionRecord(record)
            addLog("Production input saved for ${record.matrix} (${record.modelName})")

            // Real-time notification checks
            val totalOutput = afterCap + spareCount
            if (totalOutput < (matrixObj.todayTarget / 2)) {
                addLog("⚠️ Warning: low production registered in ${record.matrix} (${totalOutput} assemblies)")
            }

            if (rejectedCount > 15) {
                addLog("🔴 Severe Reject Alert: $rejectedCount discards in ${record.matrix}!")
            }
        }
        return true
    }

    fun deleteRecord(recordId: Int) {
        val user = _currentUser.value
        if (user?.role == "Worker") {
            addLog("Access Denied: Workers cannot delete entries.")
            return
        }
        viewModelScope.launch {
            repository.deleteProductionRecord(recordId)
            addLog("Supervisor deleted production entry #$recordId")
        }
    }

    fun createEmployee(id: String, name: String, role: String, shift: String, dept: String, pass: String): Boolean {
        val cleanedId = id.trim().uppercase()
        if (cleanedId.isBlank() || name.isBlank() || pass.isBlank()) {
            _loginError.value = "Registration failed. Please enter all values."
            return false
        }
        _loginError.value = null
        viewModelScope.launch {
            repository.addEmployee(Employee(cleanedId, name, role, shift, dept, pass))
            addLog("Registered status terminal user: $cleanedId ($name)")
        }
        return true
    }

    fun removeEmployee(id: String) {
        val user = _currentUser.value
        if (user?.role != "Admin") return
        viewModelScope.launch {
            repository.deleteEmployeeById(id)
            addLog("Admin removed employee profile: $id")
        }
    }

    // Change running status of an individual Matrix
    fun toggleMatrixPower(matrix: Matrix) {
        viewModelScope.launch {
            val currentStatus = matrix.status
            val newStatus = when (currentStatus) {
                "Running" -> "Stopped"
                "Stopped" -> "Running"
                else -> "Running"
            }
            repository.updateMatrixStatus(matrix.id, newStatus, matrix.currentShift)
            addLog("Matrix Status Change: ${matrix.matrixId} is now $newStatus")
        }
    }

    fun submitTransportationRecord(
        modelName: String,
        quantity: Int,
        date: String,
        vehicleDetails: String,
        destination: String
    ): Boolean {
        val user = _currentUser.value ?: return false
        viewModelScope.launch {
            val record = TransportationRecord(
                modelName = modelName,
                quantity = quantity,
                date = date,
                vehicleDetails = vehicleDetails.ifBlank { "Unassigned" },
                destination = destination.ifBlank { "Warehouse A" },
                employeeId = user.employeeId,
                timestamp = System.currentTimeMillis(),
                isSynced = _isOnline.value
            )
            repository.addTransportationRecord(record)
            addLog("Transported: $quantity pcs of $modelName dispatched via ${record.vehicleDetails}")
        }
        return true
    }

    fun deleteTransportationRecord(recordId: Int) {
        val user = _currentUser.value
        if (user?.role == "Worker") {
            addLog("Access Denied: Workers cannot delete entries.")
            return
        }
        viewModelScope.launch {
            repository.deleteTransportationRecord(recordId)
            addLog("Supervisor deleted transportation entry #$recordId")
        }
    }
}
