package com.geunho.jenaljettae.features.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geunho.jenaljettae.ui.theme.AppTheme

// MARK: - Library Summary | 전체·진행 중·지난 기록 선택 카드
@Composable
fun LibrarySummary(
    totalCount: Int,
    activeCount: Int,
    historyCount: Int,
    selectedScope: LibraryScope,
    onSelect: (LibraryScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val cards = listOf(
        Triple(LibraryScope.ALL, totalCount, "전체 항목"),
        Triple(LibraryScope.ACTIVE, activeCount, "진행 중"),
        Triple(LibraryScope.HISTORY, historyCount, "지난 기록")
    )

    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.primaryTint).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        cards.forEach { (scope, count, title) ->
            LibrarySummaryCard(
                scope = scope,
                count = count,
                title = title,
                selected = selectedScope == scope,
                onClick = { onSelect(scope) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// MARK: - Summary Card | 투명색 대신 불투명 배경색 사이를 전환해 깜박임 방지
@Composable
private fun LibrarySummaryCard(
    scope: LibraryScope,
    count: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(17.dp)
    val interactionSource = remember(scope) { MutableInteractionSource() }
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "libraryCardSelection_${scope.name}"
    )

    // MARK: - Selection Colors | 동일 진행률로 배경·테두리·텍스트 변화
    val backgroundColor = lerp(colors.primaryTint, colors.card, progress)
    val borderColor = colors.primaryForeground.copy(alpha = 0.35f * progress)
    val textColor = lerp(colors.textSecondary, colors.primaryForeground, progress)

    Column(
        modifier = modifier.height(82.dp).clip(shape).background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(count.toString(), fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}
