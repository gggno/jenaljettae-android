package com.geunho.jenaljettae.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography

// MARK: - Coupon Registration Method

@Composable
fun CouponRegistrationMethodScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors

    var showsNextStepNotice by rememberSaveable { mutableStateOf(false) }
    var selectedMethodName by rememberSaveable { mutableStateOf("") }
    var showsManualEntry by rememberSaveable { mutableStateOf(false) }

// MARK: - Manual Entry

    if (showsManualEntry) {
        CouponManualEntryScreen(
            onBack = { showsManualEntry = false }
        )
        return
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // MARK: - Header

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_chevron_left),
                    contentDescription = "뒤로 가기",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "쿠폰 등록 방법",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Content

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.large, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {

            // MARK: - Description

            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                Text(
                    text = "쿠폰·기프티콘",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryForeground
                )

                Text(
                    text = "어떻게 등록할까요?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = "이미지 속 글자를 AI로 읽어 입력을 도와줘요.",
                    style = AppTypography.body,
                    color = colors.textSecondary
                )
            }

            // MARK: - Registration Buttons

            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Button(
                    onClick = {
                        selectedMethodName = "AI로 쿠폰 읽기"
                        showsNextStepNotice = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(AppRadius.card),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(
                        text = "AI로 쿠폰 읽기",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                OutlinedButton(
                    onClick = { showsManualEntry = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(AppRadius.card),
                    border = BorderStroke(1.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.card,
                        contentColor = colors.primaryForeground
                    )
                ) {
                    Text(
                        text = "직접 입력하기",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // MARK: - Sample Coupon

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        colors.primaryTint,
                        RoundedCornerShape(AppRadius.card)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    AppSpacing.xSmall,
                    Alignment.CenterVertically
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_coupon),
                    contentDescription = null,
                    tint = colors.primaryForeground,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "아메리카노 교환권",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )

                Text(
                    text = "COUPON SAMPLE",
                    style = AppTypography.caption,
                    color = colors.textSecondary
                )
            }

            Text(
                text = "사진 한 장을 선택한 뒤 인식한 내용을 확인하고 수정할 수 있어요.",
                style = AppTypography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // MARK: - Temporary Notice

    if (showsNextStepNotice) {
        AlertDialog(
            onDismissRequest = { showsNextStepNotice = false },
            title = { Text("등록 기능 준비 중") },
            text = {
                Text("$selectedMethodName 기능은 다음 단계에서 연결할 예정이에요.")
            },
            confirmButton = {
                TextButton(onClick = { showsNextStepNotice = false }) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}
