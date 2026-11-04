package com.geunho.jenaljettae.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geunho.jenaljettae.ui.components.ScreenHeader
import com.geunho.jenaljettae.ui.components.TypeFilter
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import java.time.LocalDate
import com.geunho.jenaljettae.features.home.views.MonthCalendar
import com.geunho.jenaljettae.features.home.views.SelectedDateEventsView

// MARK: - Home Screen | UI를 표시하고 사용자 동작을 ViewModel에 전달
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val today = LocalDate.now()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calendarEvents = remember(uiState) { viewModel.calendarEvents }
    val filteredEvents = remember(uiState) { viewModel.filteredEvents }

    // MARK: - Alert State | 화면에만 필요한 안내창 표시 상태 유지
    var showsSampleDetail by rememberSaveable { mutableStateOf(false) }

    // MARK: - Body | 기존 홈 레이아웃과 화면 이동 유지
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        ScreenHeader(title = "홈", onAdd = onAdd)

        // MARK: - Type Filter | 필터 선택을 ViewModel로 전달
        TypeFilter(selectedType = uiState.selectedType, onSelect = viewModel::selectType)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(top = AppSpacing.small, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
            // MARK: - Month Calendar | 상태는 ViewModel에서 관리하고 달력 UI는 그대로 유지
            MonthCalendar(
                visibleMonth = uiState.visibleMonth,
                selectedDate = uiState.selectedDate,
                today = today,
                events = calendarEvents,
                onDateSelect = viewModel::selectDate,
                onMonthChange = viewModel::changeMonth,
                onToday = viewModel::moveToToday,
                onDatePicked = viewModel::pickDate,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )

            // MARK: - Selected Date Events | ViewModel이 필터링한 일정 목록 표시
            SelectedDateEventsView(
                selectedDate = uiState.selectedDate,
                events = filteredEvents,
                today = today,
                onEventTap = { showsSampleDetail = true },
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )
        }
    }

    // MARK: - Alert | 실제 상세 화면 연결 전 기존 안내창 유지
    if (showsSampleDetail) {
        AlertDialog(
            onDismissRequest = { showsSampleDetail = false },
            title = { Text("일정 상세 화면 준비 중") },
            text = { Text("현재는 UI 확인용 예시 일정이에요.") },
            confirmButton = {
                TextButton(onClick = { showsSampleDetail = false }) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}