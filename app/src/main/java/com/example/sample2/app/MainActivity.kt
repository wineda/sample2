package com.example.sample2.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.sample2.ui.ChatScreen
import com.example.sample2.core.theme.ChatGptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            ChatGptTheme {
                ChatScreen()
            }
        }
    }
}
