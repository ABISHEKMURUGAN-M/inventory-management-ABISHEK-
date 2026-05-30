package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ProductionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge drawing right behind navigation/status system bars
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0,0,0,0) // let custom headers handle insets
                ) { innerPadding ->
                    // Main layout content container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(IndustrialNavy)
                    ) {
                        MainAppShell()
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: ProductionViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Immersive screen crossfades under 300ms for a top tier industrial premium UI feel
    Crossfade(targetState = currentScreen, label = "mes_screen_transitions") { screen ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IndustrialNavy
        ) {
            when (screen) {
                "splash" -> SplashScreen(onSplashFinished = { viewModel.navigateTo("login") })
                "login" -> LoginScreen(viewModel = viewModel)
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "area" -> AreaSelectionScreen(viewModel = viewModel)
                "matrix" -> MatrixSelectionScreen(viewModel = viewModel)
                "entry" -> ProductionEntryScreen(viewModel = viewModel)
                "live" -> LiveBoardScreen(viewModel = viewModel)
                "reports" -> ReportsScreen(viewModel = viewModel)
                "employee_mgmt" -> EmployeeManagementScreen(viewModel = viewModel)
                "transportation" -> TransportationScreen(viewModel = viewModel)
                "inventory" -> InventoryDashboardScreen(viewModel = viewModel)
                else -> LoginScreen(viewModel = viewModel)
            }
        }
    }
}
