package com.geunho.jenaljettae.features.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.time.LocalDate
import kotlinx.coroutines.launch

private const val INITIAL_VISIBLE_COUNT = 10
private const val LOAD_MORE_COUNT = 15

// MARK: - Library Screen | iOS 보관함과 동일한 통계·검색·필터·목록
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val today = LocalDate.now()
    val filteredItems = remember(uiState, today) { viewModel.filteredItems(uiState, today) }
    val activeCount = remember(uiState.items, today) { viewModel.activeCount(uiState.items, today) }
    val historyCount = uiState.items.size - activeCount
    val collapsedTitleThreshold = with(LocalDensity.current) { 56.dp.roundToPx() }
    var visibleItemCount by rememberSaveable { mutableIntStateOf(INITIAL_VISIBLE_COUNT) }
    var showsSortSheet by rememberSaveable { mutableStateOf(false) }

    // MARK: - Reset Visible Count | 검색·유형·정렬이 바뀌면 처음 10개로 복귀
    LaunchedEffect(uiState.selectedScope, uiState.selectedType, uiState.searchText, uiState.sortOrder) {
        visibleItemCount = INITIAL_VISIBLE_COUNT
        if (scrollState.value > 0) scrollState.scrollTo(0)
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus(); keyboardController?.hide() } }
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.small, bottom = AppSpacing.xxLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // MARK: - Header | 스크롤 최상단의 큰 제목
            Text("보관함", style = AppTypography.screenTitle, color = colors.textPrimary, modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xSmall))

            LibrarySummary(
                totalCount = uiState.items.size,
                activeCount = activeCount,
                historyCount = historyCount,
                selectedScope = uiState.selectedScope,
                onSelect = viewModel::selectScope
            )

            LibrarySearchBar(
                value = uiState.searchText,
                onValueChange = viewModel::updateSearch,
                onSearch = { focusManager.clearFocus(); keyboardController?.hide() }
            )

            // MARK: - Type Filter | 다섯 가지 유형을 검색창 아래에 항상 표시
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                LibraryTypeChip("전체", uiState.selectedType == null) { viewModel.selectType(null) }
                listOf(ManagedItemType.COUPON, ManagedItemType.PASS, ManagedItemType.SUBSCRIPTION, ManagedItemType.SCHEDULE).forEach { type ->
                    LibraryTypeChip(viewModel.typeTitle(type), uiState.selectedType == type) { viewModel.selectType(type) }
                }
            }

            // MARK: - List | 정렬 및 목록, 더 보기 / 접기
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("보관 목록", style = AppTypography.sectionTitle, color = colors.textPrimary)
                    Spacer(Modifier.width(AppSpacing.xSmall))
                    // MARK: - Animated Count | 깜박임 없는 숫자 전환(슬라이드만 적용)
                    AnimatedContent(
                        targetState = filteredItems.size,
                        transitionSpec = {
                            val increasing = targetState > initialState
                            slideInVertically(animationSpec = tween(220)) { if (increasing) it else -it }
                                .togetherWith(slideOutVertically(animationSpec = tween(220)) { if (increasing) -it else it })
                        },
                        modifier = Modifier.widthIn(min = 28.dp).clipToBounds(),
                        label = "libraryItemCount"
                    ) { count ->
                        Text(count.toString(), style = AppTypography.number, color = colors.primaryForeground)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(8.dp)).clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showsSortSheet = true
                        }.padding(horizontal = AppSpacing.xSmall),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(AppIcon.sort), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        Text(uiState.sortOrder.title, style = AppTypography.caption, color = colors.textSecondary)
                        Icon(painterResource(AppIcon.chevronDown), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(11.dp))
                    }
                }

                when {
                    uiState.isLoading -> Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                    uiState.errorMessage != null -> LibraryEmptyMessage("보관함을 불러올 수 없어요", uiState.errorMessage.orEmpty())
                    filteredItems.isEmpty() -> LibraryEmptyMessage(
                        if (uiState.items.isEmpty()) "보관한 항목이 없어요" else "조건에 맞는 항목이 없어요",
                        if (uiState.items.isEmpty()) "홈에서 새로운 항목을 등록해 주세요." else "검색어나 항목 유형을 확인해 주세요."
                    )
                    else -> {
                        val visibleItems = filteredItems.take(visibleItemCount)
                        Column {
                            visibleItems.forEachIndexed { index, item ->
                                LibraryItemRow(
                                    item = item,
                                    subtitle = viewModel.subtitle(item, today),
                                    status = viewModel.status(item, today),
                                    isArchived = viewModel.isArchived(item, today),
                                    onClick = { onOpenDetail(item.id) }
                                )
                                if (index < visibleItems.lastIndex) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                                }
                            }

                            if (filteredItems.size > INITIAL_VISIBLE_COUNT) {
                                Spacer(Modifier.height(AppSpacing.small))
                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                                    if (visibleItemCount < filteredItems.size) {
                                        val nextCount = minOf(LOAD_MORE_COUNT, filteredItems.size - visibleItemCount)
                                        LibraryListAction(
                                            text = "${nextCount}개 더 보기",
                                            onClick = { visibleItemCount = minOf(visibleItemCount + LOAD_MORE_COUNT, filteredItems.size) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (visibleItemCount > INITIAL_VISIBLE_COUNT) {
                                        LibraryListAction(
                                            text = "접기",
                                            onClick = {
                                                visibleItemCount = INITIAL_VISIBLE_COUNT
                                                coroutineScope.launch { scrollState.scrollTo(0) }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MARK: - Collapsed Title | iOS처럼 스크롤하면 가운데 작은 제목 표시
        if (scrollState.value > collapsedTitleThreshold) {
            Box(modifier = Modifier.fillMaxWidth().height(52.dp).background(colors.background), contentAlignment = Alignment.Center) {
                Text("보관함", style = AppTypography.navigationTitle, color = colors.textPrimary)
            }
        }
    }

    if (showsSortSheet) {
        LibrarySortSheet(
            selectedOrder = uiState.sortOrder,
            onSelect = { order -> viewModel.selectSort(order); showsSortSheet = false },
            onDismiss = { showsSortSheet = false }
        )
    }
}

// MARK: - Search | 검색어 입력 및 키보드 완료·외부 탭 처리
@Composable
private fun LibrarySearchBar(value: String, onValueChange: (String) -> Unit, onSearch: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(AppRadius.input))
            .background(colors.inputSurface).padding(horizontal = AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
    ) {
        Icon(painterResource(AppIcon.search), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = AppTypography.body.copy(color = colors.textPrimary),
            singleLine = true,
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text("보관한 항목을 검색해 보세요", style = AppTypography.body, color = colors.disabledText, maxLines = 1)
                    innerTextField()
                }
            }
        )
        if (value.isNotEmpty()) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).clickable { onValueChange("") }, contentAlignment = Alignment.Center) {
                Icon(painterResource(AppIcon.close), contentDescription = "검색어 지우기", tint = colors.textSecondary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

// MARK: - Type Chip | 선택된 유형의 배경색 강조
@Composable
private fun LibraryTypeChip(title: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.height(36.dp).clip(CircleShape).background(if (selected) colors.primary else colors.surface)
            .clickable(onClick = onClick).padding(horizontal = AppSpacing.small),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = AppTypography.body.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium), color = if (selected) colors.onPrimary else colors.textSecondary)
    }
}

// MARK: - Empty State | 빈 목록 또는 조회 오류
@Composable
private fun LibraryEmptyMessage(title: String, description: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = AppTypography.itemTitle, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.xSmall))
        Text(description, style = AppTypography.caption, color = colors.textSecondary, textAlign = TextAlign.Center)
    }
}

// MARK: - List Action | 터치 영역 56dp 및 배경 강조
@Composable
private fun LibraryListAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(AppRadius.input))
            .background(colors.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = AppTypography.button, color = colors.textSecondary)
    }
}

// MARK: - Sort Bottom Sheet | 정렬 4가지·중앙 제목·닫기 버튼
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrarySortSheet(
    selectedOrder: LibrarySortOrder,
    onSelect: (LibrarySortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = AppRadius.largeCard, topEnd = AppRadius.largeCard)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.xSmall, bottom = 15.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 36.dp, height = 5.dp).clip(CircleShape).background(colors.disabledText.copy(alpha = 0.5f)))
            }
            Spacer(Modifier.height(AppSpacing.xSmall))
            Text("정렬", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = AppTypography.navigationTitle.copy(fontSize = 16.sp), color = colors.textPrimary)
            Spacer(Modifier.height(AppSpacing.xSmall))

            LibrarySortOrder.entries.forEach { order ->
                val selected = selectedOrder == order
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onSelect(order) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(order.title, style = AppTypography.body.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal), color = if (selected) colors.primaryForeground else colors.textPrimary)
                    Box(
                        modifier = Modifier.size(20.dp).border(2.dp, if (selected) colors.primaryForeground else colors.disabledText, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Box(Modifier.size(10.dp).background(colors.primaryForeground, CircleShape))
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.xSmall))
            Box(
                modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(AppRadius.input))
                    .background(colors.surface).border(1.dp, colors.border, RoundedCornerShape(AppRadius.input))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("닫기", style = AppTypography.button, color = colors.textPrimary)
            }
        }
    }
}
