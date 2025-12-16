package com.example.batabung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.batabung.navigation.BaTabungNavGraph
import com.example.batabung.ui.theme.BaTabungTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point aplikasi BaTabung.
 * Menggunakan Hilt untuk DI dan Jetpack Compose untuk UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaTabungTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BaTabungNavGraph()
                }
            }
        }
    }
}