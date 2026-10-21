package com.geunho.jenaljettae.features.home

import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

// MARK: - Coupon Manual Entry

@Composable
fun CouponManualEntryScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val memoFocusRequester = remember { FocusRequester() }

    // MARK: - Input State

    var couponTitle by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var couponNumber by rememberSaveable { mutableStateOf("") }
    var memo by rememberSaveable { mutableStateOf("") }

    var expiryDateText by rememberSaveable { mutableStateOf<String?>(null) }
    var showsDatePicker by rememberSaveable { mutableStateOf(false) }
    var showsMemoEditor by rememberSaveable { mutableStateOf(false) }
    var showsSaveNotice by rememberSaveable { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    var pickerYear by rememberSaveable { mutableIntStateOf(today.year) }
    var pickerMonth by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var pickerDay by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }
    var isMemoFocused by remember { mutableStateOf(false) }

    val canSave = couponTitle.isNotBlank() && expiryDateText != null
    val expiryDate = expiryDateText?.let { LocalDate.parse(it) }
    val formattedDate = expiryDate?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) ?: "날짜 선택"

    // MARK: - Keyboard

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val imeBottom = with(density) { WindowInsets.ime.getBottom(this).toDp() }
    val memoScrollSpace = if (isMemoFocused) maxOf(imeBottom, 320.dp) else 0.dp

    // MARK: - Memo Auto Focus

    LaunchedEffect(showsMemoEditor) {
        if (showsMemoEditor) {
            memoFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // MARK: - Memo Auto Scroll

    LaunchedEffect(isMemoFocused, imeBottom) {
        if (isMemoFocused) {
            delay(300)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    BackHandler { onBack() }

    // MARK: - Body

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
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
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

        // MARK: - Form And Fixed Button

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

            // MARK: - Input Content

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dismissKeyboard()
                    }
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(top = AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {

                // MARK: - Product Name

                CouponEntryTextField(
                    label = "상품명 *",
                    placeholder = "상품명을 입력해주세요.",
                    value = couponTitle,
                    onValueChange = { couponTitle = it }
                )

                // MARK: - Brand

                CouponEntryTextField(
                    label = "브랜드 (선택)",
                    placeholder = "브랜드를 입력해주세요.",
                    value = brand,
                    onValueChange = { brand = it }
                )

                // MARK: - Expiry Date

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

                    // MARK: - Inline Date Picker

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
                                            pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CouponDateWheel(
                                        value = pickerMonth,
                                        range = 1..12,
                                        onValueChange = { month ->
                                            pickerMonth = month
                                            pickerDay = minOf(pickerDay, YearMonth.of(pickerYear, pickerMonth).lengthOfMonth())
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
                                            expiryDateText = LocalDate.of(pickerYear, pickerMonth, pickerDay).toString()
                                            showsDatePicker = false
                                        }
                                    ) {
                                        Text(
                                            text = "적용",
                                            color = colors.primaryForeground,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MARK: - Coupon Number

                CouponEntryTextField(
                    label = "쿠폰 번호 (선택)",
                    placeholder = "쿠폰 번호를 입력해주세요.",
                    value = couponNumber,
                    onValueChange = { couponNumber = it }
                )

                // MARK: - Memo

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
                                if (showsMemoEditor) R.drawable.ic_jnt_chevron_left else R.drawable.ic_jnt_chevron_down
                            ),
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (showsMemoEditor) {
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .focusRequester(memoFocusRequester)
                                .onFocusChanged { isMemoFocused = it.isFocused },
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

                // MARK: - Required Fields Notice

                Text(
                    text = "상품명과 만료일은 필수 입력 항목이에요.",
                    style = AppTypography.caption,
                    color = colors.textSecondary
                )

                // MARK: - Scroll Space

                Spacer(modifier = Modifier.height(80.dp + memoScrollSpace))
            }

            // MARK: - Fixed Save Button

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.background)
            ) {
                Button(
                    onClick = {
                        dismissKeyboard()
                        showsSaveNotice = true
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.small)
                        .height(52.dp),
                    shape = RoundedCornerShape(AppRadius.card),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        disabledContainerColor = colors.inputSurface,
                        disabledContentColor = colors.textSecondary
                    )
                ) {
                    Text("쿠폰 저장", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }

    // MARK: - Save Notice

    if (showsSaveNotice) {
        AlertDialog(
            onDismissRequest = { showsSaveNotice = false },
            title = { Text("저장 기능 준비 중") },
            text = {
                Text("입력 화면 확인용 단계예요. 실제 쿠폰 저장은 다음 단계에서 연결할 예정이에요.")
            },
            confirmButton = {
                TextButton(onClick = { showsSaveNotice = false }) {
                    Text("확인", color = colors.primaryForeground)
                }
            }
        )
    }
}

// MARK: - Coupon Entry Text Field

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

// MARK: - Date Wheel

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

// MARK: - Light / Dark Preview

@Preview(name = "Light", showBackground = true)
@Composable
private fun CouponManualEntryLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        CouponManualEntryScreen(onBack = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun CouponManualEntryDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        CouponManualEntryScreen(onBack = {})
    }
}
