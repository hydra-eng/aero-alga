package com.aeroalga.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aeroalga.app.ui.navigation.AppNavigationHost
import com.aeroalga.app.ui.theme.AeroAlgaTheme
import com.aeroalga.app.ui.theme.BioBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AeroAlgaApplication
        val repository = app.repository

        setContent {
            AeroAlgaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BioBackground
                ) {
                    AppNavigationHost(repository = repository)
                }
            }
        }
    }
}
