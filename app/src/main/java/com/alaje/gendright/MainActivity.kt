package com.alaje.gendright

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alaje.gendright.ui.navigation.GendrightNavHost
import com.alaje.gendright.ui.theme.GendRightTheme

class MainActivity : ComponentActivity() {
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


