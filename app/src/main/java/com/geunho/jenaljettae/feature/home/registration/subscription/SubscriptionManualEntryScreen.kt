package com.geunho.jenaljettae.features.home.registration.subscription

import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.domain.model.SubscriptionCycle
import com.geunho.jenaljettae.features.home.registration.LocalOnSubscriptionSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveSubscriptionUseCase
import com.geunho.jenaljettae.features.home.registration.RegistrationImagePreview
import com.geunho.jenaljettae.features.home.registration.RegistrationImageSection
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

// MARK: - Subscription Manual Entry | iOS와 같은 순서로 구독 등록
@Composable
fun SubscriptionManualEntryScreen(
    onBack: () -> Unit,
    registrationViewModel: SubscriptionRegistrationViewModel = viewModel()
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val memoBringIntoViewRequester = remember { BringIntoViewRequester() }
    val saveUseCase = LocalSaveSubscriptionUseCase.current
    val onSaved = LocalOnSubscriptionSaved.current

    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val canSave = uiState.canSave && saveUseCase != null && onSaved != null
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }
    val upcomingDates = remember(uiState.subscriptionStartDate, uiState.cycle) {
        uiState.subscriptionStartDate?.let { uiState.cycle.upcomingBillingDates(it, count = 3) }.orEmpty()
    }

    // MARK: - Date Picker | 이용권과 동일한 3열 NumberPicker 사용
    val today = remember { LocalDate.now() }
    var showsDatePicker by rememberSaveable { mutableStateOf(false) }
    var pickerYear by rememberSaveable { mutableIntStateOf(today.year) }
    var pickerMonth by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var pickerDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }
    var cycleMenuExpanded by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showsMemoEditor by rememberSaveable { mutableStateOf(false) }
    var isMemoFocused by remember { mutableStateOf(false) }

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun openDatePicker() {
        dismissKeyboard()
        if (showsDatePicker) {
            showsDatePicker = false
        } else {
            val date = uiState.subscriptionStartDate ?: LocalDate.now()
            pickerYear = date.year
            pickerMonth = date.monthValue
            pickerDay = date.dayOfMonth
            showsDatePicker = true
        }
    }

    LaunchedEffect(isMemoFocused) {
        if (isMemoFocused) {
            delay(300)
            memoBringIntoViewRequester.bringIntoView()
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
            .statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
        // MARK: - Header | 등록 화면 상단
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xSmall, vertical = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_chevron_left),
                    contentDescription = "뒤로 가기",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "정기 구독 등록",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .pointerInput(focusManager, keyboardController) { detectTapGestures { dismissKeyboard() } }
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.large, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // MARK: - Title | 서비스명 필수
            SubscriptionEntryTextField("서비스명 *", "예: 넷플릭스", uiState.title, registrationViewModel::updateTitle)

            // MARK: - Cycle | 결제 주기 선택
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text("결제 주기 *", style = AppTypography.caption, color = colors.textPrimary)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { dismissKeyboard(); cycleMenuExpanded = true },
                        shape = RoundedCornerShape(AppRadius.input),
                        color = colors.inputSurface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = AppSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(uiState.cycle.title, style = AppTypography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_jnt_chevron_down),
                                contentDescription = "결제 주기 선택",
                                tint = colors.primaryForeground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = cycleMenuExpanded,
                        onDismissRequest = { cycleMenuExpanded = false },
                        modifier = Modifier.background(colors.card)
                    ) {
                        SubscriptionCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.title, color = colors.textPrimary) },
                                onClick = { registrationViewModel.updateCycle(cycle); cycleMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            // MARK: - Start Date | 구독 시작일 선택 (최초 결제 기준)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text("구독 시작일 *", style = AppTypography.caption, color = colors.textPrimary)
                Surface(
                    onClick = ::openDatePicker,
                    shape = RoundedCornerShape(AppRadius.input),
                    color = colors.inputSurface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = AppSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.subscriptionStartDate?.format(dateFormatter) ?: "날짜 선택",
                            color = if (uiState.subscriptionStartDate == null) colors.textSecondary else colors.textPrimary,
                            style = AppTypography.body,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(painterResource(R.drawable.ic_jnt_calendar), null, tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
                    }
                }

                if (showsDatePicker) {
                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(AppRadius.card),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.medium)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                                SubscriptionDateWheel(
                                    value = pickerYear, range = 1900..2100,
                                    onValueChange = { pickerYear = it; pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth()) },
                                    modifier = Modifier.weight(1f)
                                )
                                SubscriptionDateWheel(
                                    value = pickerMonth, range = 1..12,
                                    onValueChange = { pickerMonth = it; pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth()) },
                                    modifier = Modifier.weight(1f)
                                )
                                SubscriptionDateWheel(
                                    value = pickerDay, range = 1..YearMonth.of(pickerYear, pickerMonth).lengthOfMonth(),
                                    onValueChange = { pickerDay = it }, modifier = Modifier.weight(1f)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { showsDatePicker = false }) { Text("취소", color = colors.textSecondary) }
                                TextButton(onClick = {
                                    registrationViewModel.updateStartDate(LocalDate.of(pickerYear, pickerMonth, pickerDay))
                                    showsDatePicker = false
                                }) { Text("적용", color = colors.primaryForeground, fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
                Text("선택한 구독 시작일을 기준으로 ${uiState.cycle.title} 반복 표시해요.", style = AppTypography.caption, color = colors.textSecondary)
            }

            // MARK: - Timeline | 구독 시작일 이후의 미래 예상 결제일 3개
            if (upcomingDates.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("예상 결제 일정", style = AppTypography.body, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(painterResource(R.drawable.ic_jnt_calendar), null, tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        upcomingDates.forEachIndexed { index, date ->
                            val isNext = index == 0
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(if (isNext) 12.dp else 10.dp)
                                            .background(if (isNext) colors.primary else colors.border, CircleShape)
                                    )
                                    if (index != upcomingDates.lastIndex) {
                                        Box(modifier = Modifier.size(width = 2.dp, height = 46.dp).background(colors.border))
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            date.format(dateFormatter), style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium,
                                            color = colors.textPrimary, modifier = Modifier.weight(1f)
                                        )
                                        if (isNext) Text("다음 결제", style = AppTypography.caption, color = colors.primaryForeground)
                                    }
                                    val weekday = date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.KOREAN)
                                    val corrected = date.dayOfMonth < (uiState.subscriptionStartDate?.dayOfMonth ?: date.dayOfMonth)
                                    Text(
                                        text = if (corrected) "$weekday · 월말 보정" else weekday,
                                        style = AppTypography.caption, color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                    Text("실제 결제일은 서비스 정책에 따라 달라질 수 있어요.", style = AppTypography.caption, color = colors.textSecondary)
                }
            }

            // MARK: - Amount | Compose VisualTransformation으로 입력 중 1,000원 표시
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text("결제 금액 (선택)", style = AppTypography.caption, color = colors.textPrimary)
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = registrationViewModel::updateAmount,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 17,000", color = colors.textSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = remember { SubscriptionThousandsSeparatorTransformation() },
                    suffix = { Text("원", style = AppTypography.body, color = colors.textPrimary) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary, textAlign = TextAlign.Start),
                    shape = RoundedCornerShape(AppRadius.input),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.inputSurface,
                        unfocusedContainerColor = colors.inputSurface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        cursorColor = colors.primaryForeground
                    )
                )
                Text(
                    text = if (uiState.isInvalidAmount) "입력 가능한 결제 금액 범위를 초과했어요." else "원화(₩) 기준으로 저장해요.",
                    style = AppTypography.caption,
                    color = if (uiState.isInvalidAmount) MaterialTheme.colorScheme.error else colors.textSecondary
                )
            }

            // MARK: - Image | 기존 공통 이미지 추가·변경·삭제·미리보기 재사용
            RegistrationImageSection(
                itemName = "정기 구독",
                imageUri = uiState.imageUri,
                onImageSelected = registrationViewModel::setImageUri,
                onRemoveImage = registrationViewModel::removeImage,
                onPreviewImage = { previewImageUri = uiState.imageUri },
                onLoadingChanged = registrationViewModel::setPhotoLoading
            )

            // MARK: - Memo | 기존 이용권과 동일한 접기·펼치기
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(colors.inputSurface, RoundedCornerShape(AppRadius.input))
                        .clickable { if (showsMemoEditor) dismissKeyboard(); showsMemoEditor = !showsMemoEditor }
                        .padding(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.memo.isEmpty()) "메모 추가 (선택)" else "메모 확인 및 수정",
                        style = AppTypography.body, color = colors.textPrimary, modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_chevron_down), contentDescription = null,
                        tint = colors.textSecondary, modifier = Modifier.size(20.dp)
                            .graphicsLayer { rotationZ = if (showsMemoEditor) 180f else 0f }
                    )
                }
                if (showsMemoEditor) {
                    OutlinedTextField(
                        value = uiState.memo,
                        onValueChange = registrationViewModel::updateMemo,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                            .bringIntoViewRequester(memoBringIntoViewRequester)
                            .onFocusChanged { isMemoFocused = it.isFocused },
                        placeholder = { Text("메모를 입력해주세요.", color = colors.textSecondary) },
                        shape = RoundedCornerShape(AppRadius.input),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.inputSurface, unfocusedContainerColor = colors.inputSurface,
                            focusedBorderColor = colors.primary, unfocusedBorderColor = colors.border,
                            cursorColor = colors.primaryForeground
                        )
                    )
                }
            }

            Text("서비스명, 결제 주기, 구독 시작일은 필수 입력 항목이에요.", style = AppTypography.caption, color = colors.textSecondary)

            // MARK: - Save | Room 저장 후 홈 화면으로 복귀
            Button(
                onClick = {
                    dismissKeyboard()
                    val useCase = saveUseCase
                    val savedCallback = onSaved
                    if (useCase != null && savedCallback != null) {
                        coroutineScope.launch {
                            val savedItem = registrationViewModel.saveSubscription(using = useCase)
                            if (savedItem != null) savedCallback(savedItem)
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.medium).height(52.dp),
                shape = RoundedCornerShape(AppRadius.card),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary, contentColor = colors.onPrimary,
                    disabledContainerColor = colors.inputSurface, disabledContentColor = colors.textSecondary
                )
            ) {
                Text(if (uiState.isSaving) "저장 중..." else "정기 구독 저장", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    previewImageUri?.let { uri ->
        RegistrationImagePreview(itemName = "정기 구독", imageUri = uri, onDismiss = { previewImageUri = null })
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = registrationViewModel::clearError,
            title = { Text("정기 구독 저장 실패") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = registrationViewModel::clearError) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Entry Text Field | 기존 이용권 입력 스타일 재사용
@Composable
private fun SubscriptionEntryTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        Text(label, style = AppTypography.caption, color = colors.textPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = colors.textSecondary) },
            shape = RoundedCornerShape(AppRadius.input),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.inputSurface, unfocusedContainerColor = colors.inputSurface,
                focusedBorderColor = colors.primary, unfocusedBorderColor = colors.border,
                cursorColor = colors.primaryForeground
            )
        )
    }
}

// MARK: - Inline Date Wheel | 3열 연도·월·일 휠
@Composable
private fun SubscriptionDateWheel(value: Int, range: IntRange, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val textColor = AppTheme.colors.textPrimary.toArgb()
    AndroidView(
        modifier = modifier.height(160.dp),
        factory = { context ->
            NumberPicker(context).apply {
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                wrapSelectorWheel = false
                setTextColor(textColor)
            }
        },
        update = { picker ->
            picker.setOnValueChangedListener(null)
            picker.maxValue = range.last
            picker.minValue = range.first
            if (picker.value != value) picker.value = value
            picker.setTextColor(textColor)
            picker.setOnValueChangedListener { _, _, selected -> onValueChange(selected) }
        }
    )
}

// MARK: - Amount Visual Transformation | 입력 중 천 단위 콤마 및 커서 위치 유지
private class SubscriptionThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index > 0 && (digits.length - index) % 3 == 0) append(',')
                append(char)
            }
        }
        val positionMap = IntArray(digits.length + 1)
        var formattedOffset = 0
        digits.forEachIndexed { index, _ ->
            if (index > 0 && (digits.length - index) % 3 == 0) formattedOffset++
            formattedOffset++
            positionMap[index + 1] = formattedOffset
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = positionMap[offset.coerceIn(0, digits.length)]
            override fun transformedToOriginal(offset: Int): Int = formatted.take(offset.coerceIn(0, formatted.length)).count { it in '0'..'9' }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
