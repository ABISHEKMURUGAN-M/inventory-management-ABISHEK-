package com.example.utils

import android.content.Context
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.model.Employee
import com.example.data.model.ProductionRecord
import com.example.data.model.TransportationRecord
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class FactoryBackupPayload(
    val timestamp: Long,
    val productionRecords: List<ProductionRecord>,
    val transportationRecords: List<TransportationRecord>,
    val employees: List<Employee>
)

object BackupHelper {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(FactoryBackupPayload::class.java)

    suspend fun exportBackup(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val employees = db.employeeDao().getAllEmployeesList() ?: emptyList()
                val production = db.productionRecordDao().getAllRecordsList() ?: emptyList()
                val transportation = db.transportationRecordDao().getAllTransportationRecordsList() ?: emptyList()

                val payload = FactoryBackupPayload(
                    timestamp = System.currentTimeMillis(),
                    productionRecords = production,
                    transportationRecords = transportation,
                    employees = employees
                )
                adapter.toJson(payload)
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun importBackup(context: Context, jsonString: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val payload = adapter.fromJson(jsonString) ?: return@withContext false
                val db = AppDatabase.getDatabase(context)

                // Restore Employees
                for (emp in payload.employees) {
                    try {
                        db.employeeDao().insertEmployee(emp)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                // Restore Production
                for (prod in payload.productionRecords) {
                    try {
                        db.productionRecordDao().insertRecord(prod)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                // Restore Transportation
                for (trans in payload.transportationRecords) {
                    try {
                        db.transportationRecordDao().insertTransportationRecord(trans)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                true
            } catch (e: Throwable) {
                e.printStackTrace()
                false
            }
        }
    }
}
