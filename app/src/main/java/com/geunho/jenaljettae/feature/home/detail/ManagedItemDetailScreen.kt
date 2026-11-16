package com.geunho.jenaljettae.features.home.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.geunho.jenaljettae.data.local.CouponImageStorage
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.model.SubscriptionCycle
import com.geunho.jenaljettae.features.home.registration.RegistrationImagePreview
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

// MARK: - Managed Item Detail Screen | iOS 포인트 카드형 상세 화면
@Composable
fun ManagedItemDetailScreen(
    viewModel: ManagedItemDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showsCopyNotice by remember { mutableStateOf(false) }
    var showsImagePreview by remember { mutableStateOf(false) }

    val imageUri = remember(context, state.item?.imageFileName) {
        CouponImageStorage(context).getImageFile(state.item?.imageFileName)
            ?.takeIf { it.isFile }?.let { Uri.fromFile(it).toString() }
    }

    BackHandler { onBack() }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        // MARK: - Header | iOS 내비게이션 제목과 뒤로 가기
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = AppSpacing.xSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(AppIcon.back), contentDescription = "뒤로 가기", tint = colors.textPrimary, modifier = Modifier.size(24.dp))
            }
            Text("상세 정보", style = AppTypography.navigationTitle, color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(48.dp))
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            state.item == null -> Box(Modifier.fillMaxSize().padding(AppSpacing.xxLarge), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "상세 정보를 불러올 수 없어요.", color = colors.textSecondary, style = AppTypography.body, textAlign = TextAlign.Center)
            }
            else -> {
                val item = state.item!!
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .padding(top = AppSpacing.large, bottom = AppSpacing.xxLarge),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    DetailPointCard(item)
                    DetailAdditionalSection(item, onCopy = { number ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("번호", number))
                        showsCopyNotice = true
                    })
                    if (item.type == ManagedItemType.SUBSCRIPTION) {
                        item.subscriptionCycle?.let { DetailBillingSection(item.startDate, it) }
                    }
                    if (imageUri != null) DetailImageSection(imageUri = imageUri, itemName = item.title, onClick = { showsImagePreview = true })
                    item.memo?.trim()?.takeIf { it.isNotEmpty() }?.let { memo ->
                        DetailSection(title = "메모") {
                            Text(memo, style = AppTypography.body, color = colors.textPrimary, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }

    if (showsCopyNotice) {
        AlertDialog(
            onDismissRequest = { showsCopyNotice = false },
            title = { Text("번호를 복사했어요") },
            confirmButton = { TextButton(onClick = { showsCopyNotice = false }) { Text("확인") } }
        )
    }
    if (showsImagePreview && imageUri != null) {
        RegistrationImagePreview(itemName = state.item?.title ?: "등록된", imageUri = imageUri, onDismiss = { showsImagePreview = false })
    }
}

// MARK: - Point Card | 제목 옆 배지 + 핵심 정보 + 기간 진행 바
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailPointCard(item: ManagedItem) {
    val colors = AppTheme.colors
    val today = LocalDate.now()
    val badge = detailStatus(item, today)
    val subtitle = listOfNotNull(itemTypeTitle(item.type), item.detail.trim().takeIf { it.isNotEmpty() }).joinToString(" · ")
    val shape = RoundedCornerShape(AppRadius.largeCard)

    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(colors.card).border(BorderStroke(1.dp, colors.border), shape).padding(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), verticalAlignment = Alignment.Top) {
            Row(modifier = Modifier.weight(1f).height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(colors.primary, RoundedCornerShape(1.dp)))
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                    // MARK: - Title Badge | iOS ViewThatFits처럼 자리가 부족하면 다음 줄로 이동
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
                        Text(
                            text = item.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            lineHeight = 26.sp,
                            modifier = Modifier.heightIn(min = 28.dp).wrapContentHeight(Alignment.CenterVertically)
                        )
                        Box(
                            modifier = Modifier.height(28.dp).clip(RoundedCornerShape(7.dp))
                                .background(colors.primaryTint).padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryForeground, maxLines = 1)
                        }
                    }
                    Text(subtitle, fontSize = 14.sp, color = colors.textSecondary)
                }
            }
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(AppRadius.input)).background(colors.primaryTint), contentAlignment = Alignment.Center) {
                Icon(painterResource(itemTypeIcon(item.type)), contentDescription = null, modifier = Modifier.size(21.dp), tint = colors.primaryForeground)
            }
        }
        DetailDivider()
        DetailPrimaryMetrics(item, today)
        if (item.type != ManagedItemType.COUPON) {
            detailPeriod(item, today)?.let { timeline ->
                DetailDivider()
                DetailPeriodTimeline(timeline)
            }
        }
    }
}

// MARK: - Primary Metrics | 유형별 만료일·시작일·종료일·결제일
@Composable
private fun DetailPrimaryMetrics(item: ManagedItem, today: LocalDate) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        when (item.type) {
            ManagedItemType.COUPON -> DetailMetric("만료일", dateWithWeekday(item.endDate ?: item.startDate))
            ManagedItemType.PASS -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Box(Modifier.weight(1f)) { DetailMetric("시작일", dateText(item.startDate)) }
                    Box(Modifier.weight(1f)) { DetailMetric("종료일", dateText(item.endDate ?: item.startDate)) }
                }
                DetailDuration(item)
            }
            ManagedItemType.SUBSCRIPTION -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Box(Modifier.weight(1f)) { DetailMetric("구독 시작일", dateText(item.startDate)) }
                    nextBillingDate(item, today)?.let { date ->
                        Box(Modifier.weight(1f)) { DetailMetric("다음 결제일", dateText(date)) }
                    }
                }
                item.subscriptionCycle?.let { cycle ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
                        Icon(Icons.Outlined.Repeat, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                        Text("${cycle.title} 반복 결제", style = AppTypography.caption, color = colors.textSecondary)
                    }
                }
            }
            ManagedItemType.SCHEDULE -> {
                val end = item.endDate
                if (end != null && end != item.startDate) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                        Box(Modifier.weight(1f)) { DetailMetric("시작일", dateText(item.startDate)) }
                        Box(Modifier.weight(1f)) { DetailMetric("종료일", dateText(end)) }
                    }
                    DetailDuration(item)
                } else {
                    DetailMetric("일정일", dateWithWeekday(item.startDate))
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(label: String, value: String) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
        Text(label, style = AppTypography.caption, color = colors.textSecondary)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DetailDuration(item: ManagedItem) {
    val colors = AppTheme.colors
    val end = item.endDate ?: item.startDate
    val days = (ChronoUnit.DAYS.between(item.startDate, end) + 1).coerceAtLeast(1)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
        Icon(painterResource(AppIcon.calendar), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        Text("총 ${days}일", style = AppTypography.caption, color = colors.textSecondary)
    }
}

// MARK: - Period Timeline | 진행 상태 + 10dp 바 + 20dp 현재 위치 포인트
@Composable
private fun DetailPeriodTimeline(timeline: DetailPeriod) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(timeline.status, style = AppTypography.caption, color = colors.primaryForeground)
            Spacer(Modifier.weight(1f))
            Text(timeline.detail, style = AppTypography.caption, color = colors.textSecondary)
        }
        DetailProgressTrack(timeline)
    }
}

@Composable
private fun DetailProgressTrack(timeline: DetailPeriod) {
    val colors = AppTheme.colors
    val animatedProgress = remember { Animatable(0f) }
    var showsCheckmark by remember { mutableStateOf(false) }
    val target = timeline.progress.toFloat().coerceIn(0f, 1f)
    val shouldCheck = target >= 1f && (timeline.phase == DetailPhase.LAST_DAY || timeline.phase == DetailPhase.ENDED)

    LaunchedEffect(target, timeline.phase) {
        showsCheckmark = false
        // MARK: - Animation | 이동 종료 후에만 체크 표시, 변경 시 이전 애니메이션 취소
        animatedProgress.animateTo(target, animationSpec = tween(durationMillis = 700, easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)))
        showsCheckmark = shouldCheck
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(20.dp).semantics { contentDescription = "${timeline.status}, ${timeline.detail}" }, contentAlignment = Alignment.CenterStart) {
        val markerSize = 20.dp
        val filledWidth = maxWidth * animatedProgress.value
        val markerOffset = (maxWidth - markerSize) * animatedProgress.value
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(colors.primaryTint))
        if (filledWidth > 0.dp) {
            Box(modifier = Modifier.width(filledWidth).height(10.dp).clip(CircleShape).background(colors.primary))
        }
        Box(
            modifier = Modifier.offset(x = markerOffset).size(markerSize).clip(CircleShape)
                .background(colors.primary).border(3.dp, colors.card, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showsCheckmark,
                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.7f, animationSpec = tween(200))
            ) {
                Icon(painterResource(AppIcon.check), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(10.dp))
            }
        }
    }
}

// MARK: - Optional Information | 저장된 선택 항목만 표시
@Composable
private fun DetailAdditionalSection(item: ManagedItem, onCopy: (String) -> Unit) {
    when (item.type) {
        ManagedItemType.COUPON -> item.couponNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { number ->
            DetailSection("쿠폰 번호", compact = true) { DetailCopyNumber(number, onCopy) }
        }
        ManagedItemType.PASS -> item.couponNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { number ->
            DetailSection("이용권 정보", icon = { Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null) }) {
                DetailCopyNumber(number, onCopy, label = "이용권 번호")
            }
        }
        ManagedItemType.SUBSCRIPTION -> item.subscriptionAmount?.let { amount ->
            DetailSection("결제 정보", icon = { Icon(Icons.Outlined.CreditCard, contentDescription = null) }) {
                DetailInformationRow("결제 금액", "${String.format(Locale.KOREA, "%,d", amount)}원")
            }
        }
        ManagedItemType.SCHEDULE -> item.scheduleTimeMinutes?.let { minutes ->
            DetailSection("일정 정보", icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) }) {
                DetailInformationRow("시작 시간", "%02d:%02d".format(minutes / 60, minutes % 60))
            }
        }
    }
}

// MARK: - Section | 타이틀과 표면 배경의 선택 정보 카드
@Composable
private fun DetailSection(
    title: String,
    compact: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
            icon?.let { Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) { it() } }
            Text(title, style = AppTypography.sectionTitle.copy(fontSize = 15.sp), color = colors.textPrimary)
        }
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppRadius.card)).background(colors.surface)
                .padding(horizontal = AppSpacing.medium, vertical = if (compact) AppSpacing.xSmall else AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) { content() }
    }
}

@Composable
private fun DetailInformationRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = AppTypography.body, color = colors.textSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, style = AppTypography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, textAlign = TextAlign.End)
    }
}

@Composable
private fun DetailCopyNumber(number: String, onCopy: (String) -> Unit, label: String? = null) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
            if (label != null) Text(label, style = AppTypography.caption, color = colors.textSecondary)
            Text(number, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.input)).background(colors.primaryTint).clickable { onCopy(number) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "${label ?: "쿠폰 번호"} 복사", tint = colors.primaryForeground, modifier = Modifier.size(18.dp))
        }
    }
}

// MARK: - Billing Timeline | SubscriptionManualEntryScreen과 같은 세로 점·연결선 UI
@Composable
private fun DetailBillingSection(startDate: LocalDate, cycle: SubscriptionCycle) {
    val colors = AppTheme.colors
    val dates = remember(startDate, cycle) {
        cycle.upcomingBillingDates(startDate = startDate, count = 3, referenceDate = LocalDate.now(), includeStartDate = false)
    }
    if (dates.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("예상 결제 일정", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.EventRepeat, contentDescription = null, tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
        }
        Column {
            dates.forEachIndexed { index, date ->
                val isNext = index == 0
                val isLast = index == dates.lastIndex
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.width(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(if (isNext) 12.dp else 10.dp).background(if (isNext) colors.primary else colors.border, CircleShape))
                        }
                        if (!isLast) Box(modifier = Modifier.width(2.dp).height(46.dp).background(colors.border))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(dateText(date), style = AppTypography.body.copy(fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium), color = colors.textPrimary)
                            Spacer(Modifier.weight(1f))
                            if (isNext) Text("다음 결제", style = AppTypography.caption, color = colors.primaryForeground)
                        }
                        val weekDay = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.KOREAN))
                        val isMonthAdjusted = date.dayOfMonth < startDate.dayOfMonth
                        Text(if (isMonthAdjusted) "$weekDay · 월말 보정" else weekDay, style = AppTypography.caption, color = colors.textSecondary)
                    }
                }
            }
        }
        Text("실제 결제일은 서비스 정책에 따라 달라질 수 있어요.", style = AppTypography.caption, color = colors.textSecondary)
    }
}

// MARK: - Image | 저장된 원본을 표시하고 등록 화면 프리뷰 재사용
@Composable
private fun DetailImageSection(imageUri: String, itemName: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppRadius.largeCard)
    Box(modifier = Modifier.fillMaxWidth().height(224.dp).clip(shape).background(colors.inputSurface).clickable(onClick = onClick)) {
        SubcomposeAsyncImage(
            model = Uri.parse(imageUri),
            contentDescription = "등록된 ${itemName} 이미지 크게 보기",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) } },
            error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("이미지를 표시할 수 없어요.", color = colors.textSecondary) } }
        )
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.small).size(36.dp).clip(CircleShape).background(colors.card),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.OpenInFull, contentDescription = "이미지 확대", tint = colors.textPrimary, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun DetailDivider() {
    val colors = AppTheme.colors
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
}

// MARK: - Status Rules | iOS countdown(for:)와 동일한 유형별 표시
private fun detailStatus(item: ManagedItem, today: LocalDate): String {
    val start = item.startDate
    val end = item.endDate ?: start
    return when (item.type) {
        ManagedItemType.COUPON -> if (end.isBefore(today)) "만료" else detailDayCount(today, end)
        ManagedItemType.PASS, ManagedItemType.SCHEDULE -> when {
            start.isAfter(today) -> detailDayCount(today, start)
            end.isBefore(today) -> "종료"
            else -> "진행중"
        }
        ManagedItemType.SUBSCRIPTION -> when {
            start.isAfter(today) -> detailDayCount(today, start)
            start == today -> "오늘 구독 시작"
            nextBillingDate(item, today) == today -> "오늘 갱신 예정"
            else -> "구독중"
        }
    }
}

private fun detailDayCount(today: LocalDate, target: LocalDate): String {
    val difference = ChronoUnit.DAYS.between(today, target)
    return when {
        difference == 0L -> "D-DAY"
        difference > 0L -> "D-$difference"
        else -> "D+${-difference}"
    }
}

private fun nextBillingDate(item: ManagedItem, today: LocalDate): LocalDate? {
    if (item.type != ManagedItemType.SUBSCRIPTION) return null
    if (!item.startDate.isBefore(today)) return item.startDate
    return item.subscriptionCycle?.upcomingBillingDates(item.startDate, count = 1, referenceDate = today, includeStartDate = false)?.firstOrNull()
}

private enum class DetailPhase { UPCOMING, ONGOING, LAST_DAY, ENDED }
private data class DetailPeriod(val status: String, val detail: String, val progress: Double, val phase: DetailPhase)

// MARK: - Period Calculation | 쿠폰은 제외, 나머지 유형은 날짜에 따라 계산
private fun detailPeriod(item: ManagedItem, today: LocalDate): DetailPeriod? {
    if (item.type == ManagedItemType.COUPON) return null
    var from = item.startDate
    val to: LocalDate
    val startName: String
    val endName: String

    when (item.type) {
        ManagedItemType.COUPON -> return null
        ManagedItemType.PASS -> {
            to = item.endDate ?: return null
            startName = "이용"
            endName = "이용"
        }
        ManagedItemType.SCHEDULE -> {
            to = item.endDate?.takeIf { it != item.startDate } ?: return null
            startName = "일정"
            endName = "일정"
        }
        ManagedItemType.SUBSCRIPTION -> {
            val cycle = item.subscriptionCycle ?: return null
            startName = "구독"
            endName = "결제"
            if (today.isBefore(from)) {
                return DetailPeriod("구독 시작 전", "D-${ChronoUnit.DAYS.between(today, from)}", 0.0, DetailPhase.UPCOMING)
            }
            to = cycle.upcomingBillingDates(item.startDate, count = 1, referenceDate = today, includeStartDate = false).firstOrNull() ?: return null
            val previousMonth = YearMonth.from(to).minusMonths(cycle.intervalMonths)
            val previousDate = cycle.billingDate(item.startDate, previousMonth) ?: return null
            from = maxOf(item.startDate, previousDate)
        }
    }

    if (to.isBefore(from)) return null
    val remaining = ChronoUnit.DAYS.between(today, to).coerceAtLeast(0)
    if (today.isBefore(from)) {
        return DetailPeriod("$startName 시작 전", "D-${ChronoUnit.DAYS.between(today, from)}", 0.0, DetailPhase.UPCOMING)
    }
    if (today.isAfter(to)) return DetailPeriod("$endName 종료", "100%", 1.0, DetailPhase.ENDED)
    if (today == to) return DetailPeriod(if (item.type == ManagedItemType.SUBSCRIPTION) "오늘 결제 예정" else "오늘 종료", "100%", 1.0, DetailPhase.LAST_DAY)

    val total = ChronoUnit.DAYS.between(from, to)
    val elapsed = ChronoUnit.DAYS.between(from, today)
    val progress = if (total > 0) (elapsed.toDouble() / total.toDouble()).coerceIn(0.0, 1.0) else 0.0
    val percent = (progress * 100.0).roundToInt()
    val suffix = if (item.type == ManagedItemType.SUBSCRIPTION) "결제까지 ${remaining}일" else "${remaining}일 남음"
    return DetailPeriod("${percent}% 진행", suffix, progress, DetailPhase.ONGOING)
}

private fun itemTypeTitle(type: ManagedItemType) = when (type) {
    ManagedItemType.COUPON -> "쿠폰"
    ManagedItemType.PASS -> "이용권"
    ManagedItemType.SUBSCRIPTION -> "구독"
    ManagedItemType.SCHEDULE -> "일정"
}

private fun itemTypeIcon(type: ManagedItemType) = when (type) {
    ManagedItemType.COUPON -> AppIcon.coupon
    ManagedItemType.PASS -> AppIcon.pass
    ManagedItemType.SUBSCRIPTION -> AppIcon.subscription
    ManagedItemType.SCHEDULE -> AppIcon.schedule
}

private val detailDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
private val detailDateWeekdayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (EEE)", Locale.KOREAN)
private fun dateText(date: LocalDate) = date.format(detailDateFormatter)
private fun dateWithWeekday(date: LocalDate) = date.format(detailDateWeekdayFormatter)
