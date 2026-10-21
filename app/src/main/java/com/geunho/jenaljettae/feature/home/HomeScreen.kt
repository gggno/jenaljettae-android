package com.geunho.jenaljettae.features.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geunho.jenaljettae.core.components.ItemCard
import com.geunho.jenaljettae.core.components.StatusBadgeTone
import com.geunho.jenaljettae.ui.components.ItemTypeFilter
import com.geunho.jenaljettae.ui.components.ScreenHeader
import com.geunho.jenaljettae.ui.components.TypeFilter
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// MARK: - Home Screen

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val today = LocalDate.now()

    var showsSampleDetail by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf(ItemTypeFilter.ALL) }

    var visibleMonthText by rememberSaveable { mutableStateOf(today.withDayOfMonth(1).toString()) }
    var selectedDateText by rememberSaveable { mutableStateOf(today.toString()) }

    val visibleMonth = LocalDate.parse(visibleMonthText)
    val selectedDate = LocalDate.parse(selectedDateText)

    // MARK: - Sample Events

    val sampleEvents = remember(today) {
        listOf(
            HomeSampleEvent(1, today, ItemTypeFilter.COUPON, "아메리카노 교환권", "쿠폰 · 만료일", AppIcon.coupon, "오늘"),
            HomeSampleEvent(2, today, ItemTypeFilter.RESERVATION, "저녁 식사 예약", "예약 · 오후 7:00", AppIcon.reservation, "오늘"),
            HomeSampleEvent(3, today.plusDays(1), ItemTypeFilter.PASS, "헬스장 이용권", "이용권 · 종료일", AppIcon.pass, "내일"),
            HomeSampleEvent(4, today.plusDays(1), ItemTypeFilter.SUBSCRIPTION, "음악 스트리밍 구독", "구독 · 예상 갱신일", AppIcon.subscription, "내일"),
            HomeSampleEvent(5, today, ItemTypeFilter.RESERVATION, "가족 여행 숙소 예약", "예약 · 7박 8일", AppIcon.reservation, "예정", endDate = today.plusDays(7))
        )
    }

    // MARK: - Filtered Events

    val filteredEvents = sampleEvents.filter { event ->
        !selectedDate.isBefore(event.date) &&
                !selectedDate.isAfter(event.endDate ?: event.date) &&
                (selectedType == ItemTypeFilter.ALL || event.type == selectedType)
    }

    val calendarEvents = sampleEvents
        .filter { selectedType == ItemTypeFilter.ALL || it.type == selectedType }
        .map { CalendarEvent(id = it.id, title = it.title, startDate = it.date, endDate = it.endDate ?: it.date) }

    val selectedDateTitle = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    }

    // MARK: - Body

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // MARK: - Screen Header

        ScreenHeader(
            title = "홈",
            onAdd = onAdd
        )

        // MARK: - Type Filter

        TypeFilter(
            selectedType = selectedType,
            onSelect = { selectedType = it }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = AppSpacing.small, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {

            // MARK: - Month Calendar

            MonthCalendar(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                today = today,
                events = calendarEvents,
                onDateSelect = { selectedDateText = it.toString() },
                onMonthChange = { month ->
                    val monthStart = month.withDayOfMonth(1)
                    val day = minOf(selectedDate.dayOfMonth, monthStart.lengthOfMonth())

                    visibleMonthText = monthStart.toString()
                    selectedDateText = monthStart.withDayOfMonth(day).toString()
                },
                onToday = {
                    val currentDate = LocalDate.now()
                    visibleMonthText = currentDate.withDayOfMonth(1).toString()
                    selectedDateText = currentDate.toString()
                },
                onDatePicked = { date ->
                    selectedDateText = date.toString()
                    visibleMonthText = date.withDayOfMonth(1).toString()
                },
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)
            )

            // MARK: - Selected Date Events

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateTitle,
                        style = AppTypography.sectionTitle,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${filteredEvents.size}건",
                        style = AppTypography.body,
                        color = colors.textSecondary
                    )
                }

                if (filteredEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "이 날짜에는 일정이 없어요",
                            style = AppTypography.body,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    filteredEvents.forEach { event ->
                        ItemCard(
                            title = event.title,
                            subtitle = event.subtitle,
                            iconRes = event.iconRes,
                            statusLabel = event.statusLabel,
                            statusTone = if (event.date == today) StatusBadgeTone.ATTENTION else StatusBadgeTone.NEUTRAL,
                            onClick = { showsSampleDetail = true }
                        )
                    }
                }
            }
        }
    }

    // MARK: - Alerts

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

// MARK: - Sample Event

private data class HomeSampleEvent(
    val id: Int,
    val date: LocalDate,
    val type: ItemTypeFilter,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val statusLabel: String,
    val endDate: LocalDate? = null
)
