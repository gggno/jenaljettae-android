package com.geunho.jenaljettae.features.home.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geunho.jenaljettae.core.components.ItemCard
import com.geunho.jenaljettae.core.components.StatusBadgeTone
import com.geunho.jenaljettae.ui.components.ItemTypeFilter
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// MARK: - Selected Date Events | 선택 날짜의 실제 항목 카드
@Composable
internal fun SelectedDateEventsView(
    selectedDate: LocalDate,
    events: List<HomeSampleEvent>,
    today: LocalDate,
    onEventTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val selectedDateTitle = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(selectedDateTitle, style = AppTypography.sectionTitle, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text("${events.size}건", style = AppTypography.body, color = colors.textSecondary)
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xxLarge),
                contentAlignment = Alignment.Center
            ) {
                Text("이 날짜에는 일정이 없어요", style = AppTypography.body, color = colors.textSecondary)
            }
        } else {
            events.forEach { event ->
                ItemCard(
                    title = event.title,
                    subtitle = event.subtitle,
                    iconRes = event.iconRes,
                    statusLabel = event.statusLabel,
                    statusTone = if (event.date == today) StatusBadgeTone.ATTENTION else StatusBadgeTone.NEUTRAL,
                    onClick = { onEventTap(event.id) }
                )
            }
        }
    }
}

// MARK: - Home Sample Event | UI 카드 정보와 실제 ManagedItem ID
internal data class HomeSampleEvent(
    val id: String,
    val date: LocalDate,
    val type: ItemTypeFilter,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val statusLabel: String,
    val endDate: LocalDate? = null
)
