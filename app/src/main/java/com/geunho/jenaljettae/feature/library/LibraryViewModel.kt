package com.geunho.jenaljettae.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import java.text.Collator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// MARK: - Library Filters | 보관 상태 및 정렬 기준
enum class LibraryScope(val title: String) {
    ALL("전체 항목"), ACTIVE("진행 중"), HISTORY("지난 기록")
}

enum class LibrarySortOrder(val title: String) {
    NEAREST_DATE("날짜순"), RECENTLY_CREATED("최근 등록순"), OLDEST_CREATED("오래된 등록순"), NAME_ASCENDING("이름순")
}

// MARK: - Library State | 목록·검색·필터·정렬 상태
data class LibraryUiState(
    val items: List<ManagedItem> = emptyList(),
    val selectedScope: LibraryScope = LibraryScope.ALL,
    val selectedType: ManagedItemType? = null,
    val searchText: String = "",
    val sortOrder: LibrarySortOrder = LibrarySortOrder.NEAREST_DATE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// MARK: - Library ViewModel | Room Flow 관찰 및 iOS와 동일한 보관함 규칙
class LibraryViewModel(private val repository: ManagedItemRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)

    init {
        viewModelScope.launch {
            try {
                repository.observeItems().collectLatest { items ->
                    _uiState.value = _uiState.value.copy(items = items, isLoading = false, errorMessage = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.localizedMessage ?: "보관함을 불러오지 못했어요.")
            }
        }
    }

    // MARK: - Selection | 선택 즉시 화면에 반영
    fun selectScope(scope: LibraryScope) { _uiState.value = _uiState.value.copy(selectedScope = scope) }
    fun selectType(type: ManagedItemType?) { _uiState.value = _uiState.value.copy(selectedType = type) }
    fun updateSearch(text: String) { _uiState.value = _uiState.value.copy(searchText = text) }
    fun selectSort(order: LibrarySortOrder) { _uiState.value = _uiState.value.copy(sortOrder = order) }

    // MARK: - Counts | 검색과 관계없이 전체 저장 항목 기준
    fun activeCount(items: List<ManagedItem>, today: LocalDate = LocalDate.now()): Int = items.count { !isArchived(it, today) }
    fun historyCount(items: List<ManagedItem>, today: LocalDate = LocalDate.now()): Int = items.count { isArchived(it, today) }

    // MARK: - Filtered Items | 상태·유형·검색 후 정렬
    fun filteredItems(state: LibraryUiState, today: LocalDate = LocalDate.now()): List<ManagedItem> {
        val keyword = state.searchText.trim()
        val collator = Collator.getInstance(Locale.KOREAN)
        val filtered = state.items.filter { item ->
            val matchesScope = when (state.selectedScope) {
                LibraryScope.ALL -> true
                LibraryScope.ACTIVE -> !isArchived(item, today)
                LibraryScope.HISTORY -> isArchived(item, today)
            }
            val matchesType = state.selectedType == null || item.type == state.selectedType
            val matchesSearch = keyword.isEmpty() || listOf(item.title, item.detail, item.couponNumber.orEmpty(), item.memo.orEmpty())
                .any { it.contains(keyword, ignoreCase = true) }
            matchesScope && matchesType && matchesSearch
        }

        return filtered.sortedWith { left, right ->
            val comparison = when (state.sortOrder) {
                LibrarySortOrder.RECENTLY_CREATED -> right.createdAt.compareTo(left.createdAt)
                LibrarySortOrder.OLDEST_CREATED -> left.createdAt.compareTo(right.createdAt)
                LibrarySortOrder.NAME_ASCENDING -> collator.compare(left.title, right.title)
                LibrarySortOrder.NEAREST_DATE -> {
                    val leftArchived = isArchived(left, today)
                    val rightArchived = isArchived(right, today)
                    when {
                        leftArchived != rightArchived -> if (leftArchived) 1 else -1
                        else -> {
                            val dateCompare = relevantDate(left, today).compareTo(relevantDate(right, today))
                            if (leftArchived) -dateCompare else dateCompare
                        }
                    }
                }
            }
            if (comparison != 0) comparison else {
                val nameComparison = collator.compare(left.title, right.title)
                if (nameComparison != 0) nameComparison else left.id.compareTo(right.id)
            }
        }
    }

    // MARK: - Archived State | 쿠폰·이용권·일정은 종료 다음 날부터 지난 기록
    fun isArchived(item: ManagedItem, today: LocalDate = LocalDate.now()): Boolean = when (item.type) {
        ManagedItemType.COUPON, ManagedItemType.PASS, ManagedItemType.SCHEDULE -> (item.endDate ?: item.startDate).isBefore(today)
        ManagedItemType.SUBSCRIPTION -> false // 현재 모델에는 구독 해지·종료 필드가 없음
    }

    // MARK: - Status | 기존 Android 상세 화면의 상태 규칙 재사용
    fun status(item: ManagedItem, today: LocalDate = LocalDate.now()): String {
        val start = item.startDate
        val end = item.endDate ?: start
        return when (item.type) {
            ManagedItemType.COUPON -> if (end.isBefore(today)) "만료" else dDay(today, end)
            ManagedItemType.PASS, ManagedItemType.SCHEDULE -> when {
                start.isAfter(today) -> dDay(today, start)
                end.isBefore(today) -> "종료"
                else -> "진행중"
            }
            ManagedItemType.SUBSCRIPTION -> when {
                start.isAfter(today) -> dDay(today, start)
                start == today -> "오늘 구독 시작"
                nextBillingDate(item, today) == today -> "오늘 갱신 예정"
                else -> "구독중"
            }
        }
    }

    // MARK: - Subtitle | 유형별 핵심 날짜 표시
    fun subtitle(item: ManagedItem, today: LocalDate = LocalDate.now()): String = when (item.type) {
        ManagedItemType.COUPON -> "쿠폰 · ${shortDate(item.endDate ?: item.startDate)} 만료"
        ManagedItemType.PASS -> "이용권 · ${shortDate(item.endDate ?: item.startDate)} 종료"
        ManagedItemType.SUBSCRIPTION -> when {
            item.startDate.isAfter(today) -> "정기 구독 · ${shortDate(item.startDate)} 시작"
            nextBillingDate(item, today) != null -> "정기 구독 · 다음 결제 ${shortDate(nextBillingDate(item, today)!!)}"
            else -> "정기 구독 · ${shortDate(item.startDate)} 시작"
        }
        ManagedItemType.SCHEDULE -> {
            val time = item.scheduleTimeMinutes?.let { " %02d:%02d".format(it / 60, it % 60) }.orEmpty()
            "일정 · ${shortDate(item.startDate)}$time"
        }
    }

    // MARK: - Type | 필터에 표시할 유형 이름
    fun typeTitle(type: ManagedItemType): String = when (type) {
        ManagedItemType.COUPON -> "쿠폰"
        ManagedItemType.PASS -> "이용권"
        ManagedItemType.SUBSCRIPTION -> "정기 구독"
        ManagedItemType.SCHEDULE -> "일정"
    }

    // MARK: - Relevant Date | 날짜순 정렬 기준
    private fun relevantDate(item: ManagedItem, today: LocalDate): LocalDate = when (item.type) {
        ManagedItemType.COUPON, ManagedItemType.PASS -> item.endDate ?: item.startDate
        ManagedItemType.SCHEDULE -> if (item.startDate.isAfter(today)) item.startDate else item.endDate ?: item.startDate
        ManagedItemType.SUBSCRIPTION -> nextBillingDate(item, today) ?: item.startDate
    }

    // MARK: - Billing | 정기 구독 다음 결제일
    private fun nextBillingDate(item: ManagedItem, today: LocalDate): LocalDate? {
        if (item.type != ManagedItemType.SUBSCRIPTION) return null
        if (!item.startDate.isBefore(today)) return item.startDate
        return item.subscriptionCycle?.upcomingBillingDates(
            startDate = item.startDate,
            count = 1,
            referenceDate = today,
            includeStartDate = false
        )?.firstOrNull()
    }

    private fun shortDate(date: LocalDate): String = date.format(dateFormatter)
    private fun dDay(today: LocalDate, date: LocalDate): String {
        val days = ChronoUnit.DAYS.between(today, date)
        return when {
            days == 0L -> "D-DAY"
            days > 0 -> "D-$days"
            else -> "D+${-days}"
        }
    }
}
