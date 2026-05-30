package com.example.data.local

import androidx.room.*
import com.example.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE employeeId = :empId LIMIT 1")
    suspend fun getEmployeeById(empId: String): Employee?

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY name ASC")
    suspend fun getAllEmployeesList(): List<Employee>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE employeeId = :empId")
    suspend fun deleteEmployeeById(empId: String)

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int
}
