package com.geunho.jenaljettae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.geunho.jenaljettae.app.MainTabScreen

// MARK: - Main Activity | 앱 실행 및 시스템 화면 설정
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            MainTabScreen()
        }
    }
}
