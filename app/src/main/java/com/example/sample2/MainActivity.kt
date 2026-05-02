package com.example.sample2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.sample2.ui.ChatRoute
import com.example.sample2.ui.theme.ChatGptTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            ChatGptTheme() {
                ChatRoute()
            }
        }
    }
}