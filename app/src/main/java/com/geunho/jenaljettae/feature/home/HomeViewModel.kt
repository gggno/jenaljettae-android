package com.geunho.jenaljettae.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import com.geunho.jenaljettae.ui.components.ItemTypeFilter
import com.geunho.jenaljettae.ui.theme.AppIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.geunho.jenaljettae.features.home.views.CalendarEvent
import com.geunho.jenaljettae.features.home.views.HomeSampleEvent

// MARK: - Home UI State | 홈 화면에서 관찰하는 상태 정의
data class HomeUiState(
    val selectedType: ItemTypeFilter,
    val visibleMonth: LocalDate,
    val selectedDate: LocalDate,
    val items: List<ManagedItem>
)

// MARK: - Home ViewModel | 홈 상태·필터·달력 이동 및 저장 데이터 관찰
class HomeViewModel(
    private val repository: ManagedItemRepository
) : ViewModel() {
    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedType = ItemTypeFilter.ALL,
            visibleMonth = today.withDayOfMonth(1),
            selectedDate = today,
            items = emptyList()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // MARK: - Observe Items | Room 데이터 변경 시 홈 목록 갱신
    init {
        viewModelScope.launch {
            repository.observeItems().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    // MARK: - Calendar Events | 항목별 표시 날짜를 달력 이벤트로 변환
    val calendarEvents: List<CalendarEvent>
        get() = typeFilteredItems().map { item ->
            val (startDate, endDate) = displayRange(item)

            CalendarEvent(
                id = item.id,
                title = item.title,
                startDate = startDate,
                endDate = endDate
            )
        }

    // MARK: - Filtered Events | 선택 날짜에 해당하는 일정만 표시
    internal val filteredEvents: List<HomeSampleEvent>
        get() {
            val selectedDate = _uiState.value.selectedDate

            return typeFilteredItems()
                .filter { item ->
                    val (startDate, endDate) = displayRange(item)
                    !selectedDate.isBefore(startDate) && !selectedDate.isAfter(endDate)
                }
                .map { makeHomeEvent(it) }
        }

    // MARK: - Reload | 필요한 경우 Repository에서 데이터를 다시 조회
    fun reload() {
        viewModelScope.launch {
            val items = repository.fetchItems()
            _uiState.update { it.copy(items = items) }
        }
    }

    // MARK: - Select Type | 선택한 유형으로 목록 필터링
    fun selectType(type: ItemTypeFilter) {
        _uiState.update { it.copy(selectedType = type) }
    }

    // MARK: - Select Date | 달력에서 선택한 날짜 변경
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    // MARK: - Change Month | 선택 날짜를 유지하며 표시 월 변경
    fun changeMonth(month: LocalDate) {
        val monthStart = month.withDayOfMonth(1)

        _uiState.update { state ->
            val day = minOf(state.selectedDate.dayOfMonth, monthStart.lengthOfMonth())
            state.copy(
                visibleMonth = monthStart,
                selectedDate = monthStart.withDayOfMonth(day)
            )
        }
    }

    // MARK: - Move To Today | 오늘 날짜와 현재 월로 이동
    fun moveToToday() {
        val currentDate = LocalDate.now()

        _uiState.update {
            it.copy(
                visibleMonth = currentDate.withDayOfMonth(1),
                selectedDate = currentDate
            )
        }
    }

    // MARK: - Pick Date | 선택 날짜와 표시 월을 함께 변경
    fun pickDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                visibleMonth = date.withDayOfMonth(1),
                selectedDate = date
            )
        }
    }

    // MARK: - Type Filter | Domain 항목 유형을 화면 필터로 변환
    private fun typeFilteredItems(): List<ManagedItem> {
        val state = _uiState.value
        return state.items.filter {
            state.selectedType == ItemTypeFilter.ALL || filterType(it.type) == state.selectedType
        }
    }

    private fun filterType(type: ManagedItemType): ItemTypeFilter = when (type) {
        ManagedItemType.COUPON -> ItemTypeFilter.COUPON
        ManagedItemType.PASS -> ItemTypeFilter.PASS
        ManagedItemType.SUBSCRIPTION -> ItemTypeFilter.SUBSCRIPTION
        ManagedItemType.SCHEDULE -> ItemTypeFilter.SCHEDULE
    }

    // MARK: - Display Range | 쿠폰은 만료일, 다른 항목은 시작일~종료일 표시
    private fun displayRange(item: ManagedItem): Pair<LocalDate, LocalDate> {
        if (item.type == ManagedItemType.COUPON) {
            val expiryDate = item.endDate ?: item.startDate
            return expiryDate to expiryDate
        }

        return item.startDate to (item.endDate ?: item.startDate)
    }

    // MARK: - Event Mapping | Domain 항목을 홈 카드 모델로 변환
    private fun makeHomeEvent(item: ManagedItem): HomeSampleEvent {
        val type = filterType(item.type)
        val (displayDate, _) = displayRange(item)

        val iconRes = when (item.type) {
            ManagedItemType.COUPON -> AppIcon.coupon
            ManagedItemType.PASS -> AppIcon.pass
            ManagedItemType.SUBSCRIPTION -> AppIcon.subscription
            ManagedItemType.SCHEDULE -> AppIcon.schedule
        }

        val currentDate = LocalDate.now()
        val statusLabel = when {
            item.type != ManagedItemType.COUPON && item.endDate != null -> "예정"
            displayDate == currentDate -> "오늘"
            displayDate == currentDate.plusDays(1) -> "내일"
            else -> "예정"
        }

        return HomeSampleEvent(
            id = item.id,
            date = displayDate,
            type = type,
            title = item.title,
            subtitle = "${type.title} · ${item.detail}",
            iconRes = iconRes,
            statusLabel = statusLabel,
            endDate = if (item.type == ManagedItemType.COUPON) null else item.endDate
        )
    }
}
