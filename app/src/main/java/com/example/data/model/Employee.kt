package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String, // e.g., "EMP1023"
    val name: String,
    val role: String, // "Worker", "Supervisor", "Admin"
    val shift: String, // "Shift A", "Shift B", "Shift C"
    val department: String,
    val password: String
)
