package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Employee
import com.example.data.model.Matrix
import com.example.data.model.ProductionRecord
import com.example.data.model.TransportationRecord

@Database(
    entities = [Employee::class, ProductionRecord::class, Matrix::class, TransportationRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun productionRecordDao(): ProductionRecordDao
    abstract fun matrixDao(): MatrixDao
    abstract fun transportationRecordDao(): TransportationRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hanon_production_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
