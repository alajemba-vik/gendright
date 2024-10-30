package com.alaje.gendright

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.alaje.gendright.ui.navigation.GendrightNavHost
import com.alaje.gendright.ui.theme.GendRightTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GendRightTheme {
                GendrightNavHost()
            }
        }
    }
}



