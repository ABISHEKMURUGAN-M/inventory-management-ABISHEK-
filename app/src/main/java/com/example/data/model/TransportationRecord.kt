package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transportation_records")
data class TransportationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modelName: String,
    val quantity: Int,
    val date: String, // yyyy-MM-dd
    val vehicleDetails: String,
    val destination: String,
    val employeeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
