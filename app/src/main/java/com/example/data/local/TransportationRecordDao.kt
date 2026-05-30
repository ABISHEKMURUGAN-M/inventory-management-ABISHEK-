package com.example.data.local

import androidx.room.*
import com.example.data.model.TransportationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportationRecordDao {
    @Query("SELECT * FROM transportation_records ORDER BY timestamp DESC")
    fun getAllTransportationRecords(): Flow<List<TransportationRecord>>

    @Query("SELECT * FROM transportation_records ORDER BY timestamp DESC")
    suspend fun getAllTransportationRecordsList(): List<TransportationRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransportationRecord(record: TransportationRecord): Long

    @Query("DELETE FROM transportation_records WHERE id = :id")
    suspend fun deleteTransportationRecordById(id: Int)

    @Query("UPDATE transportation_records SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
