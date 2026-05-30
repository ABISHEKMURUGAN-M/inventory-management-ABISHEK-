package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_records")
data class ProductionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val shift: String, // "Shift A", "Shift B", "Shift C"
    val area: String, // "Production Line", "CAP Burns", "Final Line"
    val matrix: String, // "MATRIX-1" to "MATRIX-7"
    val modelName: String,
    val beforeCap: Int,
    val afterCap: Int,
    val spareCount: Int,
    val rejectedCount: Int,
    val remarks: String?,
    val employeeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
