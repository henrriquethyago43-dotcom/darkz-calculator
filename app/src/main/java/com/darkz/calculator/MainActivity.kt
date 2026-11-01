package com.darkz.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.darkz.calculator.ui.CalculatorScreen
import com.darkz.calculator.ui.theme.DarkzCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkzCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}
