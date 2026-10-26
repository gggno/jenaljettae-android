package com.geunho.jenaljettae.features.home

import androidx.lifecycle.ViewModel
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import com.geunho.jenaljettae.ui.components.ItemTypeFilter
import com.geunho.jenaljettae.ui.theme.AppIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Home UI State | Compose 화면에서 관찰하는 홈 상태
data class HomeUiState(
    val selectedType: ItemTypeFilter,
    val visibleMonth: LocalDate,
    val selectedDate: LocalDate,
    val items: List<ManagedItem>
)

// MARK: - Home ViewModel | 홈 상태·조회·필터링 및 달력 이동 관리
class HomeViewModel(private val repository: ManagedItemRepository) : ViewModel() {
    private val today = LocalDate.now()
    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedType = ItemTypeFilter.ALL,
            visibleMonth = today.withDayOfMonth(1),
            selectedDate = today,
            items = repository.fetchItems()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // MARK: - Calendar Events | 선택 유형에 맞는 데이터를 달력 표시용으로 변환
    val calendarEvents: List<CalendarEvent>
        get() = typeFilteredItems().map { item ->
            CalendarEvent(
                id = item.id,
                title = item.title,
                startDate = item.startDate,
                endDate = item.endDate ?: item.startDate
            )
        }

    // MARK: - Filtered Events | 선택 날짜에 포함된 항목을 카드 표시용으로 변환
    internal val filteredEvents: List<HomeSampleEvent>
        get() {
            val date = _uiState.value.selectedDate
            return typeFilteredItems()
                .filter { item -> !date.isBefore(item.startDate) && !date.isAfter(item.endDate ?: item.startDate) }
                .map { makeHomeEvent(it) }
        }

    // MARK: - Reload | 추후 데이터가 저장·수정되면 Repository에서 다시 조회
    fun reload() {
        _uiState.update { it.copy(items = repository.fetchItems()) }
    }

    // MARK: - Select Type | 필터를 변경하면 달력과 날짜별 목록이 재계산됨
    fun selectType(type: ItemTypeFilter) {
        _uiState.update { it.copy(selectedType = type) }
    }

    // MARK: - Select Date | 달력에서 선택한 날짜 변경
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    // MARK: - Change Month | 선택한 일자를 유지하며 월말 초과를 보정
    fun changeMonth(month: LocalDate) {
        val monthStart = month.withDayOfMonth(1)
        _uiState.update { state ->
            val day = minOf(state.selectedDate.dayOfMonth, monthStart.lengthOfMonth())
            state.copy(visibleMonth = monthStart, selectedDate = monthStart.withDayOfMonth(day))
        }
    }

    // MARK: - Move To Today | 표시 월과 선택 날짜를 오늘로 되돌림
    fun moveToToday() {
        val currentDate = LocalDate.now()
        _uiState.update { it.copy(visibleMonth = currentDate.withDayOfMonth(1), selectedDate = currentDate) }
    }

    // MARK: - Pick Date | 날짜 선택기의 결과를 달력과 선택 날짜에 반영
    fun pickDate(date: LocalDate) {
        _uiState.update { it.copy(visibleMonth = date.withDayOfMonth(1), selectedDate = date) }
    }

    // MARK: - Type Filter | Domain 항목 유형을 화면 필터 유형으로 변환
    private fun typeFilteredItems(): List<ManagedItem> {
        val state = _uiState.value
        return state.items.filter { state.selectedType == ItemTypeFilter.ALL || filterType(it.type) == state.selectedType }
    }

    private fun filterType(type: ManagedItemType): ItemTypeFilter = when (type) {
        ManagedItemType.COUPON -> ItemTypeFilter.COUPON
        ManagedItemType.PASS -> ItemTypeFilter.PASS
        ManagedItemType.SUBSCRIPTION -> ItemTypeFilter.SUBSCRIPTION
        ManagedItemType.SCHEDULE -> ItemTypeFilter.SCHEDULE
    }

    // MARK: - Event Mapping | Domain 데이터를 기존 ItemCard용 모델로 변환
    private fun makeHomeEvent(item: ManagedItem): HomeSampleEvent {
        val type = filterType(item.type)
        val iconRes = when (item.type) {
            ManagedItemType.COUPON -> AppIcon.coupon
            ManagedItemType.PASS -> AppIcon.pass
            ManagedItemType.SUBSCRIPTION -> AppIcon.subscription
            ManagedItemType.SCHEDULE -> AppIcon.schedule
        }
        val currentDate = LocalDate.now()
        val statusLabel = when {
            item.endDate != null -> "예정"
            item.startDate == currentDate -> "오늘"
            item.startDate == currentDate.plusDays(1) -> "내일"
            else -> "예정"
        }
        return HomeSampleEvent(
            id = item.id,
            date = item.startDate,
            type = type,
            title = item.title,
            subtitle = "${type.title} · ${item.detail}",
            iconRes = iconRes,
            statusLabel = statusLabel,
            endDate = item.endDate
        )
    }
}
