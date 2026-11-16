package com.geunho.jenaljettae.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geunho.jenaljettae.features.home.views.MonthCalendar
import com.geunho.jenaljettae.features.home.views.SelectedDateEventsView
import com.geunho.jenaljettae.ui.components.ScreenHeader
import com.geunho.jenaljettae.ui.components.TypeFilter
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import java.time.LocalDate

// MARK: - Home Screen | UI를 표시하고 사용자 동작을 ViewModel에 전달
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {}
) {
    val colors = AppTheme.colors
    val today = LocalDate.now()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calendarEvents = remember(uiState) { viewModel.calendarEvents }
    val filteredEvents = remember(uiState) { viewModel.filteredEvents }

    // MARK: - Body | 기존 홈 레이아웃을 유지하고 상세화면 이동 연결
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        ScreenHeader(title = "홈", onAdd = onAdd)
        TypeFilter(selectedType = uiState.selectedType, onSelect = viewModel::selectType)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(top = AppSpacing.small, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
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

            // MARK: - Selected Events | 선택한 카드의 실제 ID를 상세화면으로 전달
            SelectedDateEventsView(
                selectedDate = uiState.selectedDate,
                events = filteredEvents,
                today = today,
                onEventTap = onOpenDetail,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )
        }
    }
}
