package com.example.data.local

import androidx.room.*
import com.example.data.model.Matrix
import kotlinx.coroutines.flow.Flow

@Dao
interface MatrixDao {
    @Query("SELECT * FROM matrices ORDER BY matrixId ASC")
    fun getAllMatrices(): Flow<List<Matrix>>

    @Query("SELECT * FROM matrices WHERE area = :area ORDER BY matrixId ASC")
    fun getMatricesByArea(area: String): Flow<List<Matrix>>

    @Query("SELECT * FROM matrices WHERE id = :id LIMIT 1")
    suspend fun getMatrixById(id: String): Matrix?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatrix(matrix: Matrix)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatrices(matrices: List<Matrix>)

    @Query("UPDATE matrices SET status = :status, currentShift = :shift WHERE id = :id")
    suspend fun updateMatrixStatus(id: String, status: String, shift: String)
}
