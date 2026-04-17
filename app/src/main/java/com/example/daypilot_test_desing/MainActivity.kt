package com.example.daypilot_test_desing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.daypilot_test_desing.navigation.DayPilotNavGraph
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DayPilotTestDesing)
        enableEdgeToEdge()
        setContent {
            DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
                DayPilotNavGraph()
            }
        }
    }
}