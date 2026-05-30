package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ProductionRepository
import com.example.viewmodel.ProductionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testAppNameIsCorrect() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HANON SYSTEMS-INVENTORY MANAGEMENT", appName)
  }

  @Test
  fun testDatabaseSeedingAndViewModel() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val db = AppDatabase.getDatabase(app)
    val employeeDao = db.employeeDao()
    val productionRecordDao = db.productionRecordDao()
    val matrixDao = db.matrixDao()
    val transportationRecordDao = db.transportationRecordDao()

    val repository = ProductionRepository(employeeDao, productionRecordDao, matrixDao, transportationRecordDao)
    repository.seedDatabaseAsNeeded()

    // Retrieve and verify default profiles
    val employees = employeeDao.getAllEmployees().first()
    assertTrue(employees.isNotEmpty())
    val admin = employeeDao.getEmployeeById("EMP001")
    assertNotNull(admin)
    assertEquals("admin", admin?.password)
    assertEquals("Admin", admin?.role)

    // Retrieve and verify matrices
    val matrices = matrixDao.getAllMatrices().first()
    assertTrue(matrices.isNotEmpty())
    assertEquals(21, matrices.size) // 3 areas * 7 matrices = 21

    // Instantiate viewmodel
    val viewModel = ProductionViewModel(app)
    assertNotNull(viewModel)
  }

  @Test
  fun testMainActivityLaunch() {
    androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      assertNotNull(scenario)
    }
  }
}

