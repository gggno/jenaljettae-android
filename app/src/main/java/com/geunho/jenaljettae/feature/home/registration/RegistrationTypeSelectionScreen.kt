package com.geunho.jenaljettae.features.home.registration

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.core.components.ItemCard
import com.geunho.jenaljettae.core.components.ItemCardMode
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography

// MARK: - Registration Type Selection | 등록 유형 선택 및 이동 이벤트 전달
@Composable
fun RegistrationTypeSelectionScreen(
    onClose: () -> Unit,
    onCouponSelected: () -> Unit
) {
    val colors = AppTheme.colors

    var showsNextStepNotice by rememberSaveable { mutableStateOf(false) }
    var selectedTypeName by rememberSaveable { mutableStateOf("") }

    BackHandler { onClose() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // MARK: - Header | 등록 화면 닫기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.xSmall,
                    vertical = AppSpacing.small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp))
            {
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_close),
                    contentDescription = "닫기",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "유형 선택",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Content | 유형 선택 및 기능 안내
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(
                    top = AppSpacing.large,
                    bottom = AppSpacing.xLarge
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text(
                    text = "무엇을 보관할까요?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = "유형에 맞는 날짜와 상태를 관리해요.",
                    style = AppTypography.body,
                    color = colors.textSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.cardSpacing)) {
                RegistrationTypeCard(
                    title = "쿠폰·기프티콘",
                    subtitle = "상품 교환권, 할인 쿠폰",
                    iconRes = AppIcon.coupon,
                    onClick = onCouponSelected
                )

                RegistrationTypeCard(
                    title = "이용권",
                    subtitle = "기간제 구독권, 기간권",
                    iconRes = AppIcon.pass,
                    onClick = {
                        selectedTypeName = "이용권"
                        showsNextStepNotice = true
                    }
                )

                RegistrationTypeCard(
                    title = "정기 구독",
                    subtitle = "매월·매년 반복되는 갱신",
                    iconRes = AppIcon.subscription,
                    onClick = {
                        selectedTypeName = "정기 구독"
                        showsNextStepNotice = true
                    }
                )

                RegistrationTypeCard(
                    title = "일정",
                    subtitle = "식당, 병원, 미용실, 숙박",
                    iconRes = AppIcon.schedule,
                    onClick = {
                        selectedTypeName = "일정"
                        showsNextStepNotice = true
                    }
                )
            }

            Surface(
                color = colors.primaryTint,
                shape = RoundedCornerShape(AppRadius.card)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_info),
                        contentDescription = null,
                        tint = colors.primaryForeground,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "고정 기간은 이용권, 반복 갱신은 정기 구독으로 구분해요.",
                        style = AppTypography.caption,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }

    // MARK: - Temporary Notice | 아직 미구현된 유형 안내
    if (showsNextStepNotice) {
        AlertDialog(
            onDismissRequest = { showsNextStepNotice = false },
            title = { Text("등록 화면 준비 중") },
            text = {
                Text("$selectedTypeName 등록 화면은 다음 단계에서 연결할 예정이에요.")
            },
            confirmButton = {
                TextButton(onClick = { showsNextStepNotice = false }) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Registration Type Card | 등록 유형 카드
@Composable
private fun RegistrationTypeCard(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    ItemCard(
        title = title,
        subtitle = subtitle,
        iconRes = iconRes,
        statusLabel = "",
        mode = ItemCardMode.TYPE_SELECTION,
        onClick = onClick
    )
}
