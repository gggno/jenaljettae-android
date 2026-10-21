package com.geunho.jenaljettae.features.home

import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// MARK: - Calendar Events

data class CalendarEvent(
    val id: Int,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

private data class CalendarEventSegment(
    val id: Int,
    val title: String,
    val startColumn: Int,
    val endColumn: Int,
    val lane: Int,
    val isMultiDay: Boolean,
    val continuesBefore: Boolean,
    val continuesAfter: Boolean
)

private data class CalendarWeek(
    val dates: List<LocalDate>,
    val segments: List<CalendarEventSegment>,
    val hiddenCounts: List<Int>
)

private data class CalendarEventCandidate(
    val event: CalendarEvent,
    val startColumn: Int,
    val endColumn: Int,
    val isMultiDay: Boolean,
    val continuesBefore: Boolean,
    val continuesAfter: Boolean
)

private val dateHeaderHeight = 18.dp
private val eventBarHeight = 15.dp
private val overflowHeight = 11.dp
private val eventBarSpacing = 1.dp
private val dateCellHeight = dateHeaderHeight + eventBarHeight * 2 + eventBarSpacing + overflowHeight + 4.dp
private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)

// MARK: - Month Calendar

@Composable
fun MonthCalendar(
    visibleMonth: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    events: List<CalendarEvent>,
    onDateSelect: (LocalDate) -> Unit,
    onMonthChange: (LocalDate) -> Unit,
    onToday: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val calendarShape = RoundedCornerShape(18.dp)
    val monthStart = visibleMonth.withDayOfMonth(1)
    val leadingDays = monthStart.dayOfWeek.value % 7
    val totalDays = ((leadingDays + monthStart.lengthOfMonth() + 6) / 7) * 7
    val firstDate = monthStart.minusDays(leadingDays.toLong())
    val weekRows = remember(monthStart, events) {
        (0 until totalDays)
            .map { firstDate.plusDays(it.toLong()) }
            .chunked(7)
            .map { buildCalendarWeek(it, events) }
    }

    var showsDatePicker by remember { mutableStateOf(false) }
    var pickerYear by remember { mutableIntStateOf(monthStart.year) }
    var pickerMonth by remember { mutableIntStateOf(monthStart.monthValue) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(calendarShape)
            .background(colors.card)
            .border(1.dp, colors.border, calendarShape)
            .padding(top = AppSpacing.xxSmall, bottom = AppSpacing.small),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
    ) {
        // MARK: - Month Header

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                pickerYear = monthStart.year
                                pickerMonth = monthStart.monthValue
                                showsDatePicker = true
                            }
                        )
                        .semantics { contentDescription = "날짜 선택" },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${monthStart.year}년 ${monthStart.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                DropdownMenu(
                    expanded = showsDatePicker,
                    onDismissRequest = { showsDatePicker = false },
                    modifier = Modifier.width(290.dp).background(colors.card)
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "날짜 선택",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { showsDatePicker = false }
                                    )
                                    .padding(horizontal = AppSpacing.xSmall),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "취소",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AndroidView(
                                factory = { context ->
                                    NumberPicker(context).apply {
                                        minValue = 1
                                        maxValue = 12
                                        displayedValues = Array(12) { "${it + 1}월" }
                                        value = pickerMonth
                                        wrapSelectorWheel = true
                                        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                        setOnValueChangedListener { _, _, newValue -> pickerMonth = newValue }
                                    }
                                },
                                update = { picker ->
                                    if (picker.value != pickerMonth) picker.value = pickerMonth
                                    picker.setTextColor(colors.textPrimary.toArgb())
                                },
                                modifier = Modifier.weight(1f).height(140.dp)
                            )

                            key(monthStart.year) {
                                AndroidView(
                                    factory = { context ->
                                        NumberPicker(context).apply {
                                            minValue = (monthStart.year - 100).coerceAtLeast(1)
                                            maxValue = (monthStart.year + 100).coerceAtMost(9999)
                                            setFormatter { "${it}년" }
                                            value = pickerYear
                                            wrapSelectorWheel = false
                                            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                            setOnValueChangedListener { _, _, newValue -> pickerYear = newValue }
                                        }
                                    },
                                    update = { picker ->
                                        if (picker.value != pickerYear) picker.value = pickerYear
                                        picker.setTextColor(colors.textPrimary.toArgb())
                                    },
                                    modifier = Modifier.weight(1f).height(140.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary)
                                .clickable(role = Role.Button) {
                                    val targetMonth = LocalDate.of(pickerYear, pickerMonth, 1)
                                    val day = minOf(selectedDate.dayOfMonth, targetMonth.lengthOfMonth())
                                    onDatePicked(targetMonth.withDayOfMonth(day))
                                    showsDatePicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "이동",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimary
                            )
                        }
                    }
                }
            }

            MonthNavigationButton(
                iconRes = R.drawable.ic_jnt_chevron_left,
                contentDescription = "이전 달",
                onClick = { onMonthChange(monthStart.minusMonths(1)) }
            )

            MonthNavigationButton(
                iconRes = R.drawable.ic_jnt_chevron_right,
                contentDescription = "다음 달",
                onClick = { onMonthChange(monthStart.plusMonths(1)) }
            )

            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onToday
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "오늘",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryForeground,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.primaryTint)
                        .padding(horizontal = AppSpacing.xSmall, vertical = AppSpacing.xxSmall)
                )
            }
        }

        // MARK: - Weekday Header

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { weekday ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = weekday,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // MARK: - Week Grid

        val gridBorderColor = colors.border
        Column(
            modifier = Modifier.drawBehind {
                val stroke = 0.5.dp.toPx()
                drawLine(gridBorderColor, Offset(0f, 0f), Offset(size.width, 0f), stroke)
            },
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            weekRows.forEachIndexed { weekIndex, week ->
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(dateCellHeight)) {
                    val dayWidth = maxWidth / 7

                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.dates.forEachIndexed { index, date ->
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val isCurrentMonth = date.year == monthStart.year && date.month == monthStart.month
                            val dayEvents = events.filter { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }
                            val borderColor = colors.border

                            Box(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .height(dateCellHeight)
                                    .drawBehind {
                                        val stroke = 0.5.dp.toPx()
                                        if (index < 6) drawLine(borderColor, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
                                        if (weekIndex < weekRows.lastIndex) drawLine(borderColor, Offset(0f, size.height), Offset(size.width, size.height), stroke)
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        role = Role.Button,
                                        onClick = {
                                            if (isCurrentMonth) onDateSelect(date) else onDatePicked(date)
                                        }
                                    )
                                    .semantics {
                                        contentDescription = "${date.format(dateFormatter)}, " +
                                                (if (isToday) "오늘, " else "") +
                                                (if (isSelected) "선택됨, " else "") +
                                                "일정 ${dayEvents.size}건, ${dayEvents.joinToString { it.title }}"
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 2.dp, top = 1.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.primary else Color.Transparent)
                                        .then(
                                            if (isToday && !isSelected) Modifier.border(1.5.dp, colors.primaryForeground, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colors.onPrimary else if (isCurrentMonth) colors.textPrimary else colors.textSecondary
                                    )
                                }

                                val hiddenCount = week.hiddenCounts[index]
                                if (hiddenCount > 0) {
                                    Text(
                                        text = "+${hiddenCount}개",
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryForeground,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 4.dp, bottom = 2.dp)
                                            .height(overflowHeight)
                                    )
                                }
                            }
                        }
                    }

                    // 연결 막대는 장식용이므로 터치는 아래 날짜 셀이 처리해.
                    week.segments.forEach { segment ->
                        CalendarEventBar(
                            segment = segment,
                            modifier = Modifier
                                .offset(
                                    x = dayWidth * segment.startColumn + 2.dp,
                                    y = dateHeaderHeight + (eventBarHeight + eventBarSpacing) * segment.lane
                                )
                                .width(dayWidth * (segment.endColumn - segment.startColumn + 1) - 4.dp)
                                .height(eventBarHeight)
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Event Bar

@Composable
private fun CalendarEventBar(segment: CalendarEventSegment, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(
        topStart = if (segment.continuesBefore) 0.dp else 4.dp,
        bottomStart = if (segment.continuesBefore) 0.dp else 4.dp,
        topEnd = if (segment.continuesAfter) 0.dp else 4.dp,
        bottomEnd = if (segment.continuesAfter) 0.dp else 4.dp
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (segment.isMultiDay) colors.primary else colors.primaryTint)
            .padding(horizontal = 0.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = segment.title,
            fontSize = 9.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (segment.isMultiDay) colors.onPrimary else colors.primaryForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// MARK: - Week Event Lanes

private fun buildCalendarWeek(dates: List<LocalDate>, events: List<CalendarEvent>): CalendarWeek {
    val firstDate = dates.first()
    val lastDate = dates.last()

    val candidates = events.mapNotNull { event ->
        if (event.startDate.isAfter(event.endDate) || event.startDate.isAfter(lastDate) || event.endDate.isBefore(firstDate)) {
            null
        } else {
            CalendarEventCandidate(
                event = event,
                startColumn = maxOf(0, ChronoUnit.DAYS.between(firstDate, event.startDate).toInt()),
                endColumn = minOf(6, ChronoUnit.DAYS.between(firstDate, event.endDate).toInt()),
                isMultiDay = event.startDate != event.endDate,
                continuesBefore = event.startDate.isBefore(firstDate),
                continuesAfter = event.endDate.isAfter(lastDate)
            )
        }
    }.sortedWith(
        compareByDescending<CalendarEventCandidate> { it.isMultiDay }
            .thenBy { it.event.startDate }
            .thenBy { it.event.id }
    )

    val occupied = Array(2) { BooleanArray(7) }
    val segments = mutableListOf<CalendarEventSegment>()
    val hiddenCounts = MutableList(7) { 0 }

    candidates.forEach { candidate ->
        val columns = candidate.startColumn..candidate.endColumn
        val lane = (0..1).firstOrNull { row -> columns.all { !occupied[row][it] } }

        if (lane != null) {
            columns.forEach { occupied[lane][it] = true }
            segments.add(
                CalendarEventSegment(
                    id = candidate.event.id,
                    title = candidate.event.title,
                    startColumn = candidate.startColumn,
                    endColumn = candidate.endColumn,
                    lane = lane,
                    isMultiDay = candidate.isMultiDay,
                    continuesBefore = candidate.continuesBefore,
                    continuesAfter = candidate.continuesAfter
                )
            )
        } else {
            columns.forEach { hiddenCounts[it]++ }
        }
    }

    return CalendarWeek(dates, segments, hiddenCounts)
}

// MARK: - Month Navigation Button

@Composable
private fun MonthNavigationButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
