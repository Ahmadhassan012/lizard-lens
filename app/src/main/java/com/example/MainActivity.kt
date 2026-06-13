package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.state.DetectionViewModel
import com.example.ui.LizardLensApp
import com.example.ui.theme.LizardLensTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Setup edge-to-edge layouts natively
    enableEdgeToEdge()
    
    val viewModel = DetectionViewModel(this)

    setContent {
      LizardLensTheme {
        LizardLensApp(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

