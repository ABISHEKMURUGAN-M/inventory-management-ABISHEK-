package com.example.data.local

import androidx.room.*
import com.example.data.model.ProductionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionRecordDao {
    @Query("SELECT * FROM production_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ProductionRecord>>

    @Query("SELECT * FROM production_records ORDER BY timestamp DESC")
    suspend fun getAllRecordsList(): List<ProductionRecord>

    @Query("SELECT * FROM production_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<ProductionRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ProductionRecord): Long

    @Query("UPDATE production_records SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    @Query("DELETE FROM production_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("SELECT * FROM production_records WHERE date = :date ORDER BY timestamp DESC")
    fun getRecordsByDate(date: String): Flow<List<ProductionRecord>>

    @Query("SELECT * FROM production_records WHERE date = :date AND shift = :shift ORDER BY timestamp DESC")
    fun getRecordsByDateAndShift(date: String, shift: String): Flow<List<ProductionRecord>>
}
