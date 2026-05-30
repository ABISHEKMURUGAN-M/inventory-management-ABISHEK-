package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.Employee
import com.example.data.model.ProductionRecord
import com.example.data.model.TransportationRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseSyncHelper {
    private const val TAG = "FirebaseSyncHelper"
    private var isInitialized = false
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:447003517336:android:6de3fd57724974c93ccf35")
                    .setApiKey("AIzaSyD_dummyKeyValueForFirebaseStudioBuildApp")
                    .setDatabaseUrl("https://hanon-mes-rtdb-default-rtdb.firebaseio.com")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            database = FirebaseDatabase.getInstance()
            auth = FirebaseAuth.getInstance()
            isInitialized = true
            Log.d(TAG, "Firebase initialized successfully with programmatic options.")
            
            // Start real-time remote syncing listeners
            startRealtimeListeners(context)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Firebase SDK dynamically: ${e.message}")
            isInitialized = false
        }
    }

    // Pushes production record to Realtime DB
    fun pushProductionRecord(record: ProductionRecord) {
        if (!isInitialized) return
        try {
            val ref = database?.getReference("production_records")?.child(record.id.toString()) ?: return
            val recordMap = mapOf(
                "id" to record.id,
                "date" to record.date,
                "shift" to record.shift,
                "area" to record.area,
                "matrix" to record.matrix,
                "modelName" to record.modelName,
                "beforeCap" to record.beforeCap,
                "afterCap" to record.afterCap,
                "spareCount" to record.spareCount,
                "rejectedCount" to record.rejectedCount,
                "remarks" to (record.remarks ?: ""),
                "employeeId" to record.employeeId,
                "timestamp" to record.timestamp
            )
            ref.setValue(recordMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push production record to Firebase: ${e.message}")
        }
    }

    // Pushes transportation record to Realtime DB
    fun pushTransportationRecord(record: TransportationRecord) {
        if (!isInitialized) return
        try {
            val ref = database?.getReference("transportation_records")?.child(record.id.toString()) ?: return
            val recordMap = mapOf(
                "id" to record.id,
                "modelName" to record.modelName,
                "quantity" to record.quantity,
                "date" to record.date,
                "vehicleDetails" to record.vehicleDetails,
                "destination" to record.destination,
                "employeeId" to record.employeeId,
                "timestamp" to record.timestamp
            )
            ref.setValue(recordMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push transportation record to Firebase: ${e.message}")
        }
    }

    // Pushes employee profile to Realtime DB
    fun pushEmployee(employee: Employee) {
        if (!isInitialized) return
        try {
            val ref = database?.getReference("employees")?.child(employee.employeeId) ?: return
            val empMap = mapOf(
                "employeeId" to employee.employeeId,
                "name" to employee.name,
                "role" to employee.role,
                "shift" to employee.shift,
                "department" to employee.department,
                "password" to employee.password
            )
            ref.setValue(empMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push employee to Firebase: ${e.message}")
        }
    }

    // Deletes production record in Realtime DB
    fun deleteProductionRecord(id: Int) {
        if (!isInitialized) return
        try {
            database?.getReference("production_records")?.child(id.toString())?.removeValue()
        } catch (_: Exception) {}
    }

    // Deletes transportation record in Realtime DB
    fun deleteTransportationRecord(id: Int) {
        if (!isInitialized) return
        try {
            database?.getReference("transportation_records")?.child(id.toString())?.removeValue()
        } catch (_: Exception) {}
    }

    // Deletes employee record in Realtime DB
    fun deleteEmployee(id: String) {
        if (!isInitialized) return
        try {
            database?.getReference("employees")?.child(id)?.removeValue()
        } catch (_: Exception) {}
    }

    // Listens to remote changes and saves to local Room DB
    private fun startRealtimeListeners(context: Context) {
        val localDb = AppDatabase.getDatabase(context)
        
        // Listeners for Production Records
        try {
            database?.getReference("production_records")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    scope.launch {
                        for (child in snapshot.children) {
                            try {
                                val id = child.child("id").getValue(Int::class.java) ?: continue
                                val date = child.child("date").getValue(String::class.java) ?: ""
                                val shift = child.child("shift").getValue(String::class.java) ?: ""
                                val area = child.child("area").getValue(String::class.java) ?: ""
                                val matrix = child.child("matrix").getValue(String::class.java) ?: ""
                                val modelName = child.child("modelName").getValue(String::class.java) ?: ""
                                val beforeCap = child.child("beforeCap").getValue(Int::class.java) ?: 0
                                val afterCap = child.child("afterCap").getValue(Int::class.java) ?: 0
                                val spareCount = child.child("spareCount").getValue(Int::class.java) ?: 0
                                val rejectedCount = child.child("rejectedCount").getValue(Int::class.java) ?: 0
                                val remarks = child.child("remarks").getValue(String::class.java)
                                val employeeId = child.child("employeeId").getValue(String::class.java) ?: ""
                                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                                val localRecord = ProductionRecord(
                                    id = id,
                                    date = date,
                                    shift = shift,
                                    area = area,
                                    matrix = matrix,
                                    modelName = modelName,
                                    beforeCap = beforeCap,
                                    afterCap = afterCap,
                                    spareCount = spareCount,
                                    rejectedCount = rejectedCount,
                                    remarks = if (remarks.isNullOrBlank()) null else remarks,
                                    employeeId = employeeId,
                                    timestamp = timestamp,
                                    isSynced = true
                                )
                                localDb.productionRecordDao().insertRecord(localRecord)
                            } catch (e: Throwable) {
                                Log.e(TAG, "Error parsing remote production record: ${e.message}")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Production listener cancelled: ${error.message}")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start Realtime Production Listener: ${e.message}")
        }

        // Listeners for Transportation Records
        try {
            database?.getReference("transportation_records")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    scope.launch {
                        for (child in snapshot.children) {
                            try {
                                val id = child.child("id").getValue(Int::class.java) ?: continue
                                val modelName = child.child("modelName").getValue(String::class.java) ?: ""
                                val quantity = child.child("quantity").getValue(Int::class.java) ?: 0
                                val date = child.child("date").getValue(String::class.java) ?: ""
                                val vehicleDetails = child.child("vehicleDetails").getValue(String::class.java) ?: ""
                                val destination = child.child("destination").getValue(String::class.java) ?: ""
                                val employeeId = child.child("employeeId").getValue(String::class.java) ?: ""
                                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                                val transRecord = TransportationRecord(
                                    id = id,
                                    modelName = modelName,
                                    quantity = quantity,
                                    date = date,
                                    vehicleDetails = vehicleDetails,
                                    destination = destination,
                                    employeeId = employeeId,
                                    timestamp = timestamp,
                                    isSynced = true
                                )
                                localDb.transportationRecordDao().insertTransportationRecord(transRecord)
                            } catch (e: Throwable) {
                                Log.e(TAG, "Error parsing remote transportation record: ${e.message}")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Transportation listener cancelled: ${error.message}")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start Realtime Transportation Listener: ${e.message}")
        }

        // Listeners for Employees
        try {
            database?.getReference("employees")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    scope.launch {
                        for (child in snapshot.children) {
                            try {
                                val employeeId = child.child("employeeId").getValue(String::class.java) ?: continue
                                val name = child.child("name").getValue(String::class.java) ?: ""
                                val role = child.child("role").getValue(String::class.java) ?: "Worker"
                                val shift = child.child("shift").getValue(String::class.java) ?: "Shift A"
                                val department = child.child("department").getValue(String::class.java) ?: ""
                                val password = child.child("password").getValue(String::class.java) ?: ""

                                val employee = Employee(
                                    employeeId = employeeId,
                                    name = name,
                                    role = role,
                                    shift = shift,
                                    department = department,
                                    password = password
                                )
                                localDb.employeeDao().insertEmployee(employee)
                            } catch (e: Throwable) {
                                Log.e(TAG, "Error parsing remote employee: ${e.message}")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Employees listener cancelled: ${error.message}")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start Realtime Employees Listener: ${e.message}")
        }
    }
}
