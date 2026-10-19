package com.geunho.jenaljettae.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.geunho.jenaljettae.ui.components.ScreenHeader
import com.geunho.jenaljettae.ui.theme.AppTheme

// MARK: - Home Screen

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    var showsAddNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        ScreenHeader(
            title = "홈",
            subtitle = "쿠폰부터 예약까지, 필요한 날짜를 한눈에",
            onAdd = { showsAddNotice = true }
        )
    }

    if (showsAddNotice) {
        AlertDialog(
            onDismissRequest = { showsAddNotice = false },
            title = { Text("등록 화면 준비 중") },
            text = { Text("추가 버튼의 화면 이동은 등록 기능 구현 시 연결할 예정이에요.") },
            confirmButton = {
                TextButton(onClick = { showsAddNotice = false }) {
                    Text("확인", color = colors.primaryForeground)
                }
            },
            containerColor = colors.card,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary
        )
    }
}
