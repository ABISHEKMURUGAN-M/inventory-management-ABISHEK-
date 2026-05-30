package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matrices")
data class Matrix(
    @PrimaryKey val id: String, // area_matrixId (to make it unique across areas)
    val matrixId: String, // "MATRIX-1" ... "MATRIX-7"
    val area: String, // "Production Line", "CAP Burns", "Final Line"
    val status: String, // "Running", "Low Output", "Stopped"
    val currentShift: String,
    val todayTarget: Int = 1000 // Each matrix has a daily target
)
