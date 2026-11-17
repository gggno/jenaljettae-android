package com.geunho.jenaljettae.features.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography

// MARK: - Library Item Row | iOS처럼 간결한 보관 목록 행
@Composable
fun LibraryItemRow(
    item: ManagedItem,
    subtitle: String,
    status: String,
    isArchived: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val icon = when (item.type) {
        ManagedItemType.COUPON -> AppIcon.coupon
        ManagedItemType.PASS -> AppIcon.pass
        ManagedItemType.SUBSCRIPTION -> AppIcon.subscription
        ManagedItemType.SCHEDULE -> AppIcon.schedule
    }

    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(onClick = onClick).padding(vertical = AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(if (isArchived) colors.surface else colors.primaryTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon), contentDescription = null,
                tint = if (isArchived) colors.disabledText else colors.primaryForeground,
                modifier = Modifier.size(21.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
            Text(
                item.title, style = AppTypography.itemTitle,
                color = if (isArchived) colors.textSecondary else colors.textPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, style = AppTypography.caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text(
            status, style = AppTypography.status,
            color = if (isArchived) colors.disabledText else colors.primaryForeground,
            maxLines = 1
        )
        Icon(painterResource(AppIcon.chevronRight), contentDescription = null, tint = colors.disabledText, modifier = Modifier.size(14.dp))
    }
}
