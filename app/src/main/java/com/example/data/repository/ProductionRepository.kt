package com.example.data.repository

import com.example.data.local.EmployeeDao
import com.example.data.local.MatrixDao
import com.example.data.local.ProductionRecordDao
import com.example.data.local.TransportationRecordDao
import com.example.data.model.Employee
import com.example.data.model.Matrix
import com.example.data.model.ProductionRecord
import com.example.data.model.TransportationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductionRepository(
    private val employeeDao: EmployeeDao,
    private val productionRecordDao: ProductionRecordDao,
    private val matrixDao: MatrixDao,
    private val transportationRecordDao: TransportationRecordDao
) {
    // Flow of all records
    val allRecords: Flow<List<ProductionRecord>> = productionRecordDao.getAllRecords()

    // Flow of all transportation records
    val allTransportationRecords: Flow<List<TransportationRecord>> = transportationRecordDao.getAllTransportationRecords()

    // Flow of all employees for Admin screen
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

    // Flow of all matrices
    val allMatrices: Flow<List<Matrix>> = matrixDao.getAllMatrices()

    /**
     * Seeds the database with default data if empty.
     */
    suspend fun seedDatabaseAsNeeded() {
        val employeeCount = employeeDao.getEmployeeCount()
        if (employeeCount == 0) {
            // Seed base employees
            val defaults = listOf(
                Employee("EMP001", "Abishek (Admin)", "Admin", "Shift A", "Management", "admin"),
                Employee("EMP002", "Alex Vance (Worker)", "Worker", "Shift A", "Radiator Assembly", "worker"),
                Employee("EMP003", "Marcus Cole (Supervisor)", "Supervisor", "Shift B", "Quality Control", "supervisor"),
                Employee("EMP004", "Sarah Conner (Worker)", "Worker", "Shift C", "CAP Processing", "worker")
            )
            for (emp in defaults) {
                employeeDao.insertEmployee(emp)
            }
        }

        val matrices = matrixDao.getAllMatrices().first()
        if (matrices.isEmpty()) {
            val areas = listOf("Production Line", "CAP Burns", "Final Line")
            val sampleMatrixList = mutableListOf<Matrix>()
            
            for (area in areas) {
                for (i in 1..7) {
                    val mId = "MATRIX-$i"
                    sampleMatrixList.add(
                        Matrix(
                            id = "${area}_$mId",
                            matrixId = mId,
                            area = area,
                            status = if (i == 4) "Stopped" else if (i == 6) "Low Output" else "Running",
                            currentShift = "Shift A",
                            todayTarget = 1000 - (i * 50)
                        )
                    )
                }
            }
            matrixDao.insertMatrices(sampleMatrixList)

            // Seed some existing mock production entries for reports/graphs to look highly populated!
            // We'll generate a few mock entries for the last 5 days
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val baseTime = System.currentTimeMillis()
            val models = listOf("SP2i GA", "HR 1.0", "MX-5 Hybrid", "Radiator-X", "Hanon Elite")
            val recordCount = productionRecordDao.getAllRecords().first().size
            if (recordCount == 0) {
                for (d in 0..4) {
                    val dateStr = format.format(Date(baseTime - (d * 24 * 60 * 60 * 1000L)))
                    val shift = if (d % 3 == 0) "Shift A" else if (d % 3 == 1) "Shift B" else "Shift C"
                    val area = areas[d % 3]
                    
                    // Insert some sample historical database entries for visual charts
                    val newEntryId = productionRecordDao.insertRecord(
                        ProductionRecord(
                            date = dateStr,
                            shift = shift,
                            area = area,
                            matrix = "MATRIX-${(d % 7) + 1}",
                            modelName = models[d % models.size],
                            beforeCap = 820 + (d * 50),
                            afterCap = 800 + (d * 40),
                            spareCount = 20 + d,
                            rejectedCount = 5 + (d % 3),
                            remarks = "Daily operational run completed.",
                            employeeId = "EMP002",
                            timestamp = baseTime - (d * 24 * 60 * 60 * 1000L),
                            isSynced = true
                        )
                    )

                    // Seed matching transportation records so stock updates cleanly (Produced -> Transported -> Remaining)
                    if (d < 3) {
                        transportationRecordDao.insertTransportationRecord(
                            TransportationRecord(
                                modelName = models[d % models.size],
                                quantity = 350 - (d * 50),
                                date = dateStr,
                                vehicleDetails = "FORD-CARGO-TRUCK-${100 + d}",
                                destination = "Hanon Logistics Hub Unit ${d + 1}",
                                employeeId = "EMP001",
                                timestamp = baseTime - (d * 24 * 60 * 60 * 1000L) - (1 * 60 * 60 * 1000L),
                                isSynced = true
                            )
                        )
                    }
                }
            }
        }
    }

    // Auth operations
    suspend fun getEmployeeById(empId: String): Employee? {
        return employeeDao.getEmployeeById(empId)
    }

    suspend fun addEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
        try {
            FirebaseSyncHelper.pushEmployee(employee)
        } catch (_: Exception) {}
    }

    suspend fun deleteEmployeeById(empId: String) {
        employeeDao.deleteEmployeeById(empId)
        try {
            FirebaseSyncHelper.deleteEmployee(empId)
        } catch (_: Exception) {}
    }

    // Production operations
    suspend fun addProductionRecord(record: ProductionRecord): Long {
        val entryId = productionRecordDao.insertRecord(record)
        val finalRecord = if (record.id == 0) {
            record.copy(id = entryId.toInt())
        } else {
            record
        }
        
        try {
            productionRecordDao.markSynced(finalRecord.id)
            FirebaseSyncHelper.pushProductionRecord(finalRecord)
        } catch (_: Exception) {}

        // When a production record is uploaded, update the corresponding matrix's current status and running output
        val matrixObj = matrixDao.getMatrixById("${record.area}_${record.matrix}")
        if (matrixObj != null) {
            val netOutput = record.afterCap + record.spareCount
            val status = if (netOutput < (matrixObj.todayTarget / 2)) "Low Output" else "Running"
            matrixDao.updateMatrixStatus(matrixObj.id, status, record.shift)
        }

        return entryId
    }

    fun getRecordsByDate(date: String): Flow<List<ProductionRecord>> {
        return productionRecordDao.getRecordsByDate(date)
    }

    fun getRecordsByDateAndShift(date: String, shift: String): Flow<List<ProductionRecord>> {
        return productionRecordDao.getRecordsByDateAndShift(date, shift)
    }

    suspend fun deleteProductionRecord(id: Int) {
        productionRecordDao.deleteRecordById(id)
        try {
            FirebaseSyncHelper.deleteProductionRecord(id)
        } catch (_: Exception) {}
    }

    // Matrix operations
    fun getMatricesByArea(area: String): Flow<List<Matrix>> {
        return matrixDao.getMatricesByArea(area)
    }

    suspend fun updateMatrixStatus(id: String, status: String, shift: String) {
        matrixDao.updateMatrixStatus(id, status, shift)
    }

    // Sync trigger (called on background or pull refresh to resolve pending offline uploads)
    suspend fun syncOfflineEntries(): Int {
        val unsynced = productionRecordDao.getUnsyncedRecords()
        var syncCount = 0
        for (rec in unsynced) {
            try {
                productionRecordDao.markSynced(rec.id)
                FirebaseSyncHelper.pushProductionRecord(rec)
                syncCount++
            } catch (_: Exception) {}
        }
        return syncCount
    }

    // Transportation/Dispatch operations
    suspend fun addTransportationRecord(record: TransportationRecord): Long {
        val entryId = transportationRecordDao.insertTransportationRecord(record)
        val finalRecord = if (record.id == 0) {
            record.copy(id = entryId.toInt())
        } else {
            record
        }
        try {
            transportationRecordDao.markSynced(finalRecord.id)
            FirebaseSyncHelper.pushTransportationRecord(finalRecord)
        } catch (_: Exception) {}
        return entryId
    }

    suspend fun deleteTransportationRecord(id: Int) {
        transportationRecordDao.deleteTransportationRecordById(id)
        try {
            FirebaseSyncHelper.deleteTransportationRecord(id)
        } catch (_: Exception) {}
    }
}
