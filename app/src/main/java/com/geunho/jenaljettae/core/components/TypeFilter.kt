package com.geunho.jenaljettae.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

// MARK: - Filter Type

enum class ItemTypeFilter(val title: String) {
    ALL("전체"),
    COUPON("쿠폰"),
    PASS("이용권"),
    SUBSCRIPTION("구독"),
    RESERVATION("예약")
}

// MARK: - Type Filter

@Composable
fun TypeFilter(
    selectedType: ItemTypeFilter,
    onSelect: (ItemTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemTypeFilter.entries.forEach { type ->
            val isSelected = selectedType == type

            Box(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .selectable(
                        selected = isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.RadioButton,
                        onClick = { onSelect(type) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.title,
                    style = AppTypography.body,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.onPrimary else colors.textSecondary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) colors.primary else colors.inputSurface)
                        .padding(horizontal = AppSpacing.small, vertical = AppSpacing.xSmall)
                )
            }


        }
    }
}
