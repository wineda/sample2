package com.example.sample2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.sample2.ui.ChatScreen
import com.example.sample2.ui.theme.ChatGptTheme

val ChatBackground = Color(0xFFF5F5F5)
val BubbleColor = Color.White
val TextColor = Color(0xFF222222)
val TimeColor = Color(0xFF888888)

val DarkColorScheme = darkColorScheme(

    primary = Color(0xFF1F1F1F),
    secondary = Color(0xFF2A2A2A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),

    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            ChatGptTheme() {
                ChatScreen()
            }
        }
    }
}