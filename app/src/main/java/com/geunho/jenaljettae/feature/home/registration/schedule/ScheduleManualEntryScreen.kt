package com.geunho.jenaljettae.features.home.registration.schedule

import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.features.home.registration.LocalOnScheduleSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveScheduleUseCase
import com.geunho.jenaljettae.features.home.registration.RegistrationImagePreview
import com.geunho.jenaljettae.features.home.registration.RegistrationImageSection
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

// MARK: - Schedule Manual Entry | 일정 직접 입력 및 저장
@Composable
fun ScheduleManualEntryScreen(
    onBack: () -> Unit,
    registrationViewModel: ScheduleRegistrationViewModel = viewModel()
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // MARK: - Dependencies | 기존 등록 화면과 동일한 CompositionLocal
    val saveScheduleUseCase = LocalSaveScheduleUseCase.current
    val onScheduleSaved = LocalOnScheduleSaved.current

    // MARK: - UI State | ViewModel 상태
    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val canSave = uiState.canSave && saveScheduleUseCase != null && onScheduleSaved != null

    // MARK: - Date State | 날짜 선택
    val today = remember { LocalDate.now() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }

    var activeDateField by rememberSaveable { mutableStateOf<String?>(null) }
    var pickerYear by rememberSaveable { mutableIntStateOf(today.year) }
    var pickerMonth by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var pickerDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }

    // MARK: - Time State | 시간 선택
    var showsTimePicker by rememberSaveable { mutableStateOf(false) }
    var pickerHour by rememberSaveable { mutableIntStateOf(9) }
    var pickerMinute by rememberSaveable { mutableIntStateOf(0) }

    // MARK: - Image & Memo State | 화면 임시 상태
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showsMemoEditor by rememberSaveable { mutableStateOf(false) }

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // MARK: - Open Date Picker | 기존 날짜 또는 오늘로 초기화
    fun openDatePicker(field: String) {
        dismissKeyboard()
        showsTimePicker = false

        if (activeDateField == field) {
            activeDateField = null
            return
        }

        val date = (if (field == "start") uiState.startDate else uiState.endDate)
            ?: uiState.startDate ?: today

        pickerYear = date.year
        pickerMonth = date.monthValue
        pickerDay = date.dayOfMonth
        activeDateField = field
    }

    // MARK: - Open Time Picker | 시간 휠 열기
    fun openTimePicker() {
        dismissKeyboard()
        activeDateField = null

        if (showsTimePicker) {
            showsTimePicker = false
            return
        }

        val minutes = uiState.timeMinutes ?: 540
        pickerHour = minutes / 60
        pickerMinute = minutes % 60
        showsTimePicker = true
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
                text = "일정 등록",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Input Content | 빈 공간 터치 시 키보드 닫기
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
            // MARK: - Schedule Title | 일정명 필수
            ScheduleEntryTextField(
                label = "일정명 *",
                placeholder = "예: 치과 예약, 호텔 숙박",
                value = uiState.title,
                onValueChange = registrationViewModel::updateTitle
            )

            // MARK: - Place | 장소 선택
            ScheduleEntryTextField(
                label = "장소 (선택)",
                placeholder = "장소를 입력해주세요.",
                value = uiState.place,
                onValueChange = registrationViewModel::updatePlace
            )

            // MARK: - Date Range | 시작일·종료일 모두 필수
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text(
                    text = "일정 날짜",
                    style = AppTypography.body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    ScheduleDateField(
                        label = "시작일 *",
                        date = uiState.startDate,
                        formatter = dateFormatter,
                        modifier = Modifier.weight(1f),
                        onClick = { openDatePicker("start") }
                    )

                    ScheduleDateField(
                        label = "종료일 *",
                        date = uiState.endDate,
                        formatter = dateFormatter,
                        modifier = Modifier.weight(1f),
                        onClick = { openDatePicker("end") }
                    )
                }

                // MARK: - Date Wheel | 선택한 날짜 수정
                if (activeDateField != null) {
                    val maxDay = YearMonth.of(pickerYear, pickerMonth).lengthOfMonth()

                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(AppRadius.card)
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                            ) {
                                ScheduleNumberWheel(
                                    value = pickerYear,
                                    range = 1900..2100,
                                    onValueChange = {
                                        pickerYear = it
                                        pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                ScheduleNumberWheel(
                                    value = pickerMonth,
                                    range = 1..12,
                                    onValueChange = {
                                        pickerMonth = it
                                        pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                ScheduleNumberWheel(
                                    value = pickerDay.coerceAtMost(maxDay),
                                    range = 1..maxDay,
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

                                TextButton(
                                    onClick = {
                                        val date = LocalDate.of(pickerYear, pickerMonth, pickerDay.coerceAtMost(maxDay))
                                        if (activeDateField == "start") registrationViewModel.updateStartDate(date)
                                        else registrationViewModel.updateEndDate(date)
                                        activeDateField = null
                                    }
                                ) {
                                    Text(
                                        "적용",
                                        color = colors.primaryForeground,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (uiState.isInvalidDateRange) {
                        "종료일은 시작일보다 빠를 수 없어요."
                    } else {
                        "시작일과 종료일은 필수예요. 하루 일정은 같은 날짜를 선택해주세요."
                    },
                    style = AppTypography.caption,
                    color = if (uiState.isInvalidDateRange) MaterialTheme.colorScheme.error else colors.textSecondary
                )
            }

            // MARK: - Optional Time | 시작 시간 선택
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text(
                    text = "시작 시간 (선택)",
                    style = AppTypography.caption,
                    color = colors.textPrimary
                )

                ScheduleInputButton(
                    text = uiState.timeMinutes?.let { "%02d:%02d".format(it / 60, it % 60) }
                        ?: "시간 추가 (선택)",
                    onClick = ::openTimePicker,
                    placeholder = uiState.timeMinutes == null
                )

                if (showsTimePicker) {
                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(AppRadius.card)
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                                ScheduleNumberWheel(
                                    value = pickerHour,
                                    range = 0..23,
                                    onValueChange = { pickerHour = it },
                                    modifier = Modifier.weight(1f)
                                )

                                ScheduleNumberWheel(
                                    value = pickerMinute,
                                    range = 0..59,
                                    onValueChange = { pickerMinute = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { showsTimePicker = false }) {
                                    Text("취소", color = colors.textSecondary)
                                }

                                TextButton(
                                    onClick = {
                                        registrationViewModel.updateTime(pickerHour * 60 + pickerMinute)
                                        showsTimePicker = false
                                    }
                                ) {
                                    Text("적용", color = colors.primaryForeground)
                                }
                            }
                        }
                    }
                }

                if (uiState.timeMinutes != null) {
                    TextButton(onClick = registrationViewModel::clearTime) {
                        Text(
                            "시간 제거",
                            style = AppTypography.caption,
                            color = colors.primaryForeground
                        )
                    }
                }
            }

            // MARK: - Image | 기존 공통 이미지 UI 사용
            RegistrationImageSection(
                itemName = "일정",
                imageUri = uiState.imageUri,
                onImageSelected = registrationViewModel::setImageUri,
                onRemoveImage = registrationViewModel::removeImage,
                onPreviewImage = { previewImageUri = uiState.imageUri },
                onLoadingChanged = registrationViewModel::setPhotoLoading
            )

            // MARK: - Memo | 접고 펼치는 메모
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
                        modifier = Modifier.size(20.dp)
                            .graphicsLayer { rotationZ = if (showsMemoEditor) 180f else 0f }
                    )
                }

                if (showsMemoEditor) {
                    OutlinedTextField(
                        value = uiState.memo,
                        onValueChange = registrationViewModel::updateMemo,
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        placeholder = {
                            Text("메모를 입력해주세요.", color = colors.textSecondary)
                        },
                        shape = RoundedCornerShape(AppRadius.input),
                        colors = scheduleTextFieldColors()
                    )
                }
            }

            // MARK: - Required Fields | 필수 입력 안내
            Text(
                text = "일정명, 시작일, 종료일은 필수 입력 항목이에요.",
                style = AppTypography.caption,
                color = colors.textSecondary
            )

            // MARK: - Save Button | 일정 저장
            Button(
                onClick = {
                    dismissKeyboard()

                    val useCase = saveScheduleUseCase
                    val callback = onScheduleSaved

                    if (useCase != null && callback != null) {
                        coroutineScope.launch {
                            val savedItem = registrationViewModel.saveSchedule(using = useCase)
                            if (savedItem != null) callback(savedItem)
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
                    .padding(top = AppSpacing.medium)
                    .height(52.dp),
                shape = RoundedCornerShape(AppRadius.card),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.inputSurface,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text(
                    if (uiState.isSaving) "저장 중..." else "일정 저장",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    // MARK: - Image Preview | 전체 화면 이미지
    previewImageUri?.let { uri ->
        RegistrationImagePreview(
            itemName = "일정",
            imageUri = uri,
            onDismiss = { previewImageUri = null }
        )
    }

    // MARK: - Save Error | 저장 오류 안내
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = registrationViewModel::clearError,
            title = { Text("일정 저장 실패") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = registrationViewModel::clearError) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Schedule Entry Text Field | 텍스트 입력 컴포넌트
@Composable
private fun ScheduleEntryTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
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
            colors = scheduleTextFieldColors()
        )
    }
}

// MARK: - Schedule Date Field | 시작일·종료일 공통 UI
@Composable
private fun ScheduleDateField(
    label: String,
    date: LocalDate?,
    formatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
    ) {
        Text(label, style = AppTypography.caption, color = colors.textPrimary)

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(AppRadius.input),
            color = colors.inputSurface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp)
                    .padding(horizontal = AppSpacing.xSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date?.format(formatter) ?: "날짜 선택",
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

// MARK: - Schedule Input Button | 시간 선택 UI
@Composable
private fun ScheduleInputButton(
    text: String,
    onClick: () -> Unit,
    placeholder: Boolean = false
) {
    val colors = AppTheme.colors

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppRadius.input),
        color = colors.inputSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp)
                .padding(horizontal = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = AppTypography.body,
                color = if (placeholder) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = colors.primaryForeground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// MARK: - Number Wheel | 날짜·시간 공통 NumberPicker
@Composable
private fun ScheduleNumberWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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

// MARK: - TextField Colors | 공통 입력창 스타일
@Composable
private fun scheduleTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textPrimary,
    focusedContainerColor = AppTheme.colors.inputSurface,
    unfocusedContainerColor = AppTheme.colors.inputSurface,
    focusedBorderColor = AppTheme.colors.primary,
    unfocusedBorderColor = AppTheme.colors.border,
    cursorColor = AppTheme.colors.primaryForeground
)
