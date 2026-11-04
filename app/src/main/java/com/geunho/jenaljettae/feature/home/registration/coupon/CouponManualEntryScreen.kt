package com.geunho.jenaljettae.features.home.registration.coupon

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.domain.model.CouponDraft
import com.geunho.jenaljettae.features.home.registration.LocalOnCouponSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveCouponUseCase
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

// MARK: - Coupon Manual Entry | AI 분석 결과 또는 직접 입력으로 쿠폰 등록
@Composable
fun CouponManualEntryScreen(
    onBack: () -> Unit,
    initialDraft: CouponDraft? = null,
    initialImageUri: Uri? = null,
    registrationViewModel: CouponRegistrationViewModel = viewModel()
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val memoBringIntoViewRequester = remember { BringIntoViewRequester() }

    // MARK: - Dependencies | CompositionLocal에서 저장 기능과 완료 콜백 가져오기
    val saveCouponUseCase = LocalSaveCouponUseCase.current
    val onCouponSaved = LocalOnCouponSaved.current

    // MARK: - UI State | ViewModel 상태 관찰 및 화면 전용 상태 관리
    val uiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
    val sessionId = rememberSaveable { UUID.randomUUID().toString() }

    var showsDatePicker by rememberSaveable { mutableStateOf(false) }
    var showsMemoEditor by rememberSaveable { mutableStateOf(false) }
    var isMemoFocused by remember { mutableStateOf(false) }

    // MARK: - Image Preview State | 전체 화면으로 확인할 이미지 URI
    var previewImageUri by remember { mutableStateOf<String?>(null) }

    // MARK: - AI Image Change | AI가 분석한 원본 이미지의 변경 여부 확인
    var hasChangedAnalyzedImage by rememberSaveable { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    var pickerYear by rememberSaveable { mutableIntStateOf(today.year) }
    var pickerMonth by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var pickerDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }

    val expiryDate = uiState.expiryDate
    val formattedDate = expiryDate?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) ?: "날짜 선택"
    val canSave = uiState.canSave && saveCouponUseCase != null && onCouponSaved != null

    // MARK: - Expired Coupon | 오늘보다 이전 날짜이면 만료 경고 표시
    val isExpiredCoupon = expiryDate?.isBefore(LocalDate.now()) == true

    // MARK: - AI Review Notes | Gemini 분석이 전달한 확인 필요 항목
    val reviewNotes = initialDraft?.reviewNotes.orEmpty()

    // MARK: - Registration Session | 상태 초기화 후 AI 분석 결과와 원본 이미지 적용
    LaunchedEffect(sessionId) {
        registrationViewModel.startSession(sessionId)

        initialDraft?.let { draft ->
            registrationViewModel.updateCouponTitle(draft.title.orEmpty())
            registrationViewModel.updateBrand(draft.brand.orEmpty())
            registrationViewModel.updateCouponNumber(draft.couponNumber.orEmpty())
            draft.expiryDate?.let { registrationViewModel.updateExpiryDate(it) }
        }

        initialImageUri?.let { registrationViewModel.setImageUri(it.toString()) }
    }

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // MARK: - Memo Auto Scroll | 메모 입력 시 키보드 위로 스크롤
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
        // MARK: - Header | 뒤로 가기 및 화면 제목
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.xSmall,
                    vertical = AppSpacing.small),
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
                text = "쿠폰 등록",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Input Content | 이미지와 입력 항목, 저장 버튼을 함께 스크롤
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .pointerInput(focusManager, keyboardController) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                }
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.large, bottom = AppSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // MARK: - Coupon Image | 이미지 선택·변경·삭제 및 전체 화면 미리보기
            CouponImageSection(
                imageUri = uiState.imageUri,
                onImageSelected = { uri ->
                    registrationViewModel.setImageUri(uri)
                    if (initialDraft != null) hasChangedAnalyzedImage = true
                },
                onRemoveImage = {
                    registrationViewModel.removeImage()
                    if (initialDraft != null) hasChangedAnalyzedImage = true
                },
                onPreviewImage = { previewImageUri = uiState.imageUri },
                onLoadingChanged = registrationViewModel::setPhotoLoading
            )

            // MARK: - AI Review Notice | AI 분석 결과의 확인 필요 항목과 이미지 교체 안내
            if (initialDraft != null && uiState.imageUri != null && (hasChangedAnalyzedImage || reviewNotes.isNotEmpty())) {
                CouponWarningCard(
                    title = if (hasChangedAnalyzedImage) "이미지를 확인해주세요." else "확인 필요한 항목",
                    description = if (hasChangedAnalyzedImage) null else "AI가 정확하게 인식했는지 확인해주세요.",
                    notes = if (hasChangedAnalyzedImage) {
                        listOf("이미지와 입력된 쿠폰 정보가 일치하는지 확인해주세요.")
                    } else {
                        reviewNotes
                    }
                )
            }

            // MARK: - Product Name | 상품명 필수 입력
            CouponEntryTextField(
                label = "상품명 *",
                placeholder = "상품명을 입력해주세요.",
                value = uiState.couponTitle,
                onValueChange = registrationViewModel::updateCouponTitle
            )

            // MARK: - Brand | 브랜드 선택 입력
            CouponEntryTextField(
                label = "브랜드 (선택)",
                placeholder = "브랜드를 입력해주세요.",
                value = uiState.brand,
                onValueChange = registrationViewModel::updateBrand
            )

            // MARK: - Expiry Date | 만료일 필수 선택
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Text(
                    text = "만료일 *",
                    style = AppTypography.caption,
                    color = colors.textPrimary
                )

                Surface(
                    onClick = {
                        dismissKeyboard()

                        val date = expiryDate ?: LocalDate.now()
                        pickerYear = date.year
                        pickerMonth = date.monthValue
                        pickerDay = date.dayOfMonth
                        showsDatePicker = !showsDatePicker
                    },
                    shape = RoundedCornerShape(AppRadius.input),
                    color = colors.inputSurface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = AppSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            style = AppTypography.body,
                            color = if (expiryDate == null) colors.textSecondary else colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(R.drawable.ic_jnt_calendar),
                            contentDescription = null,
                            tint = colors.primaryForeground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // MARK: - Inline Date Picker | 휠로 날짜 선택 및 적용
                if (showsDatePicker) {
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
                                CouponDateWheel(
                                    value = pickerYear,
                                    range = 1900..2100,
                                    onValueChange = { year ->
                                        pickerYear = year
                                        pickerDay = minOf(
                                            pickerDay,
                                            YearMonth.of(pickerYear, pickerMonth).lengthOfMonth()
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                CouponDateWheel(
                                    value = pickerMonth,
                                    range = 1..12,
                                    onValueChange = { month ->
                                        pickerMonth = month
                                        pickerDay = minOf(
                                            pickerDay,
                                            YearMonth.of(pickerYear, pickerMonth).lengthOfMonth()
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                CouponDateWheel(
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
                                TextButton(onClick = { showsDatePicker = false }) {
                                    Text("취소", color = colors.textSecondary)
                                }

                                TextButton(
                                    onClick = {
                                        registrationViewModel.updateExpiryDate(
                                            LocalDate.of(pickerYear, pickerMonth, pickerDay)
                                        )
                                        showsDatePicker = false
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

                // MARK: - Expiry Warning | 만료된 쿠폰도 기록용 저장 허용
                if (isExpiredCoupon) {
                    CouponWarningCard(
                        title = "이미 만료된 쿠폰이에요.",
                        description = "유효기간을 확인해주세요. 만료된 쿠폰도 기록용으로 등록할 수 있어요."
                    )
                }
            }

            // MARK: - Coupon Number | 쿠폰 번호 선택 입력
            CouponEntryTextField(
                label = "쿠폰 번호 (선택)",
                placeholder = "쿠폰 번호를 입력해주세요.",
                value = uiState.couponNumber,
                onValueChange = registrationViewModel::updateCouponNumber
            )

            // MARK: - Memo | 메모 입력 영역 펼치기
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
                        text = "메모 추가 (선택)",
                        style = AppTypography.body,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        painter = painterResource(
                            if (showsMemoEditor) R.drawable.ic_jnt_chevron_left
                            else R.drawable.ic_jnt_chevron_down
                        ),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
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
                            .onFocusChanged { state ->
                                isMemoFocused = state.isFocused
                            },
                        placeholder = {
                            Text("메모를 입력해주세요.", color = colors.textSecondary)
                        },
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

            // MARK: - Required Fields Notice | 필수 입력 항목 안내
            Text(
                text = "상품명과 만료일은 필수 입력 항목이에요.",
                style = AppTypography.caption,
                color = colors.textSecondary
            )

            // MARK: - Save Button | 스크롤 영역 최하단에서 쿠폰과 이미지 저장
            Button(
                onClick = {
                    dismissKeyboard()

                    val useCase = saveCouponUseCase
                    val savedCallback = onCouponSaved

                    if (useCase != null && savedCallback != null) {
                        coroutineScope.launch {
                            val savedItem = registrationViewModel.saveCoupon(using = useCase)
                            if (savedItem != null) savedCallback(savedItem)
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = if (uiState.isSaving) "저장 중..." else "쿠폰 저장",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    // MARK: - Image Preview | 이미지 터치 시 전체 화면 미리보기 표시
    previewImageUri?.let { uri ->
        CouponImagePreview(
            imageUri = uri,
            onDismiss = { previewImageUri = null }
        )
    }

    // MARK: - Save Error | 저장 실패 시 입력값을 유지하고 오류 안내
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = registrationViewModel::clearError,
            title = { Text("쿠폰 저장 실패") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = registrationViewModel::clearError) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Coupon Warning Card | AI 검토 및 만료 안내 공통 UI
@Composable
private fun CouponWarningCard(
    title: String,
    description: String? = null,
    notes: List<String> = emptyList()
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.inputSurface, RoundedCornerShape(AppRadius.card))
            .padding(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
        ) {
            // MARK: - Warning Icon | 추가 리소스 없이 주황색 경고 아이콘 구성
            Box(
                modifier = Modifier.size(20.dp).background(Color(0xFFFF9500), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color.Black, fontWeight = FontWeight.Bold, style = AppTypography.caption)
            }

            Text(
                text = title,
                style = AppTypography.body,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }

        if (description != null) {
            Text(description, style = AppTypography.caption, color = colors.textSecondary)
        }

        notes.forEach { note ->
            Text("• $note", style = AppTypography.caption, color = colors.textSecondary)
        }
    }
}

// MARK: - Coupon Entry Text Field | 쿠폰 정보 공통 입력 UI
@Composable
private fun CouponEntryTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val colors = AppTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        Text(
            text = label,
            style = AppTypography.caption,
            color = colors.textPrimary
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(placeholder, color = colors.textSecondary)
            },
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

// MARK: - Coupon Date Wheel | Android NumberPicker로 날짜 선택
@Composable
private fun CouponDateWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
