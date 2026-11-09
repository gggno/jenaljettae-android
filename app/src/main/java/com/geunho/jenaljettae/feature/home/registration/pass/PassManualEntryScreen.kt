package com.geunho.jenaljettae.features.home.registration.pass

import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.features.home.registration.RegistrationImagePreview
import com.geunho.jenaljettae.features.home.registration.RegistrationImageSection
import com.geunho.jenaljettae.features.home.registration.LocalSavePassUseCase
import com.geunho.jenaljettae.features.home.registration.LocalOnPassSaved
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


// MARK: - Pass Manual Entry | 이용권 직접 입력 및 저장 화면
@Composable
fun PassManualEntryScreen(
    onBack: () -> Unit,
    registrationViewModel: PassRegistrationViewModel = viewModel()
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val memoBringIntoViewRequester = remember { BringIntoViewRequester() }

    // MARK: - Dependencies | 쿠폰과 동일한 CompositionLocal 방식
    val savePassUseCase = LocalSavePassUseCase.current
    val onPassSaved = LocalOnPassSaved.current
    val coroutineScope = rememberCoroutineScope()

    // MARK: - UI State | ViewModel이 이용권 입력·검증·저장을 관리
    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val startDate = uiState.startDate
    val endDate = uiState.endDate
    val isInvalidDateRange = uiState.isInvalidDateRange
    val canSave = uiState.canSave && savePassUseCase != null && onPassSaved != null

    // MARK: - Date Picker State | 시작일·종료일 중 한 날짜만 선택
    val today = remember { LocalDate.now() }
    var activeDateField by rememberSaveable { mutableStateOf<String?>(null) }
    var pickerYear by rememberSaveable { mutableIntStateOf(today.year) }
    var pickerMonth by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var pickerDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }

    // MARK: - Image & Memo State | 화면 표시만을 위한 임시 상태
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showsMemoEditor by rememberSaveable { mutableStateOf(false) }
    var isMemoFocused by remember { mutableStateOf(false) }

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // MARK: - Open Date Picker | 현재 선택한 날짜 또는 오늘로 휠 초기화
    fun openDatePicker(field: String) {
        dismissKeyboard()
        if (activeDateField == field) {
            activeDateField = null
            return
        }
        val date = (if (field == "start") startDate else endDate) ?: LocalDate.now()
        pickerYear = date.year
        pickerMonth = date.monthValue
        pickerDay = date.dayOfMonth
        activeDateField = field
    }

    // MARK: - Memo Scroll | 메모 입력 시 키보드 위로 스크롤
    LaunchedEffect(isMemoFocused) {
        if (isMemoFocused) {
            delay(300)
            memoBringIntoViewRequester.bringIntoView()
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // MARK: - Header | 뒤로 가기 및 제목
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xSmall, vertical = AppSpacing.small),
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
                text = "이용권 등록",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Input Content | 빈 공간 터치로만 키보드 닫기 (스크롤 중 유지)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .pointerInput(focusManager, keyboardController) {
                    detectTapGestures(onTap = { dismissKeyboard() })
                }
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.large, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // MARK: - Pass Name | 이용권 이름 필수 입력
            PassEntryTextField(
                label = "이름 *",
                placeholder = "이용권 이름을 입력해주세요.",
                value = uiState.title,
                onValueChange = registrationViewModel::updateTitle
            )

            // MARK: - Place | 이용처 선택 입력
            PassEntryTextField(
                label = "이용처 (선택)",
                placeholder = "이용처를 입력해주세요.",
                value = uiState.place,
                onValueChange = registrationViewModel::updatePlace
            )

            // MARK: - Pass Dates | 시작일·종료일 필수, 가로 2열
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text("이용 기간", style = AppTypography.body, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)

                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    PassDateField(
                        label = "시작일 *",
                        date = startDate,
                        dateFormatter = dateFormatter,
                        modifier = Modifier.weight(1f),
                        onClick = { openDatePicker("start") }
                    )
                    PassDateField(
                        label = "종료일 *",
                        date = endDate,
                        dateFormatter = dateFormatter,
                        modifier = Modifier.weight(1f),
                        onClick = { openDatePicker("end") }
                    )
                }

                // MARK: - Inline Date Picker | 두 날짜가 동일한 휠 UI를 사용
                if (activeDateField != null) {
                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(AppRadius.card),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.medium)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                            ) {
                                PassDateWheel(
                                    value = pickerYear,
                                    range = 1900..2100,
                                    onValueChange = { year ->
                                        pickerYear = year
                                        pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PassDateWheel(
                                    value = pickerMonth,
                                    range = 1..12,
                                    onValueChange = { month ->
                                        pickerMonth = month
                                        pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PassDateWheel(
                                    value = pickerDay,
                                    range = 1..YearMonth.of(pickerYear, pickerMonth).lengthOfMonth(),
                                    onValueChange = { pickerDay = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { activeDateField = null }) {
                                    Text("취소", color = colors.textSecondary)
                                }
                                TextButton(onClick = {
                                    val chosen = LocalDate.of(pickerYear, pickerMonth, pickerDay)
                                    if (activeDateField == "start") registrationViewModel.updateStartDate(chosen)
                                    else registrationViewModel.updateEndDate(chosen)
                                    activeDateField = null
                                }) {
                                    Text("적용", color = colors.primaryForeground, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (isInvalidDateRange) "종료일은 시작일보다 빠를 수 없어요." else "시작일과 종료일은 필수 입력 항목이에요.",
                    style = AppTypography.caption,
                    color = if (isInvalidDateRange) MaterialTheme.colorScheme.error else colors.textSecondary
                )
            }

            // MARK: - Pass Number | 이용권 번호 선택 입력
            PassEntryTextField(
                label = "이용권 번호 (선택)",
                placeholder = "이용권 번호를 입력해주세요.",
                value = uiState.passNumber,
                onValueChange = registrationViewModel::updatePassNumber
            )

            // MARK: - Image | 쿠폰과 동일한 공통 이미지 컴포넌트
            RegistrationImageSection(
                itemName = "이용권",
                imageUri = uiState.imageUri,
                onImageSelected = registrationViewModel::setImageUri,
                onRemoveImage = registrationViewModel::removeImage,
                onPreviewImage = { previewImageUri = uiState.imageUri },
                onLoadingChanged = registrationViewModel::setPhotoLoading
            )

            // MARK: - Memo | 메모 펼치기 및 입력
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.inputSurface, RoundedCornerShape(AppRadius.input))
                        .clickable {
                            if (showsMemoEditor) dismissKeyboard()
                            showsMemoEditor = !showsMemoEditor
                        }
                        .padding(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.memo.isEmpty()) "메모 추가 (선택)" else "메모 확인 및 수정",
                        style = AppTypography.body,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_chevron_down),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = if (showsMemoEditor) 180f else 0f }
                    )
                }

                if (showsMemoEditor) {
                    OutlinedTextField(
                        value = uiState.memo,
                        onValueChange = registrationViewModel::updateMemo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .bringIntoViewRequester(memoBringIntoViewRequester)
                            .onFocusChanged { isMemoFocused = it.isFocused },
                        placeholder = { Text("메모를 입력해주세요.", color = colors.textSecondary) },
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                }
            }

            // MARK: - Required Fields | 입력 필수값 안내
            Text("이름, 시작일, 종료일은 필수 입력 항목이에요.", style = AppTypography.caption, color = colors.textSecondary)

            // MARK: - Save Button | 이용권·이미지 영구 저장
            Button(
                onClick = {
                    dismissKeyboard()
                    val useCase = savePassUseCase
                    val savedCallback = onPassSaved
                    if (useCase != null && savedCallback != null) {
                        coroutineScope.launch {
                            val savedItem = registrationViewModel.savePass(using = useCase)
                            if (savedItem != null) savedCallback(savedItem)
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.medium).height(52.dp),
                shape = RoundedCornerShape(AppRadius.card),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.inputSurface,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text(if (uiState.isSaving) "저장 중..." else "이용권 저장", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    // MARK: - Image Preview | 쿠폰과 동일한 전체 화면 확대 UI
    previewImageUri?.let { uri ->
        RegistrationImagePreview(itemName = "이용권", imageUri = uri, onDismiss = { previewImageUri = null })
    }

    // MARK: - Save Error | 저장 실패 시 입력값 유지하고 안내
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = registrationViewModel::clearError,
            title = { Text("이용권 저장 실패") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = registrationViewModel::clearError) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Pass Entry Field | 쿠폰 입력창과 동일한 스타일
@Composable
private fun PassEntryTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        Text(label, style = AppTypography.caption, color = colors.textPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = colors.textSecondary) },
            textStyle = MaterialTheme.typography.bodyLarge,
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
    }
}

// MARK: - Pass Date Field | 시작일·종료일 공통 셀
@Composable
private fun PassDateField(
    label: String,
    date: LocalDate?,
    dateFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        Text(label, style = AppTypography.caption, color = colors.textPrimary)
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(AppRadius.input),
            color = colors.inputSurface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = AppSpacing.xSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date?.format(dateFormatter) ?: "날짜 선택",
                    style = AppTypography.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (date == null) colors.textSecondary else colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_calendar),
                    contentDescription = null,
                    tint = colors.primaryForeground,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// MARK: - Pass Date Wheel | 쿠폰에서 사용하는 NumberPicker와 동일한 방식
@Composable
private fun PassDateWheel(value: Int, range: IntRange, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val textColor = colors.textPrimary.toArgb()
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
            picker.setOnValueChangedListener { _, _, newValue -> onValueChange(newValue) }
        }
    )
}

// MARK: - Preview | NavHost 연결 전 UI 미리보기
@Preview(name = "Light", showBackground = true)
@Composable
private fun PassManualEntryPreview() {
    JENALJETTAETheme { PassManualEntryScreen(onBack = {}) }
}
