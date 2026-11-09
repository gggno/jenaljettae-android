package com.geunho.jenaljettae.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import com.geunho.jenaljettae.features.home.views.CalendarEvent
import com.geunho.jenaljettae.features.home.views.HomeSampleEvent
import com.geunho.jenaljettae.ui.components.ItemTypeFilter
import com.geunho.jenaljettae.ui.theme.AppIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

// MARK: - Home UI State | 홈 필터·달력·선택 날짜
data class HomeUiState(
    val selectedType: ItemTypeFilter,
    val visibleMonth: LocalDate,
    val selectedDate: LocalDate,
    val items: List<ManagedItem>
)

// MARK: - Home ViewModel | 저장 데이터 및 달력 표시 관리
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

    // MARK: - Observe Items | Room 데이터 변경 관찰
    init {
        viewModelScope.launch {
            repository.observeItems().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    // MARK: - Calendar Events | 달력에 표시할 이벤트 생성
    val calendarEvents: List<CalendarEvent>
        get() {
            val month = YearMonth.from(_uiState.value.visibleMonth)

            return typeFilteredItems().flatMap { item ->
                if (item.type == ManagedItemType.SUBSCRIPTION) {
                    val cycle = item.subscriptionCycle ?: return@flatMap emptyList()

                    (-1..1).mapNotNull { offset ->
                        val date = cycle.billingDate(
                            item.startDate,
                            month.plusMonths(offset.toLong())
                        ) ?: return@mapNotNull null

                        CalendarEvent(
                            id = "${item.id}:$date",
                            title = item.title,
                            startDate = date,
                            endDate = date
                        )
                    }
                } else {
                    val (start, end) = displayRange(item)

                    listOf(
                        CalendarEvent(
                            id = item.id,
                            title = item.title,
                            startDate = start,
                            endDate = end
                        )
                    )
                }
            }
        }

    // MARK: - Filtered Events | 선택한 날짜의 항목
    internal val filteredEvents: List<HomeSampleEvent>
        get() {
            val selectedDate = _uiState.value.selectedDate

            return typeFilteredItems().mapNotNull { item ->
                if (item.type == ManagedItemType.SUBSCRIPTION) {
                    val billingDate = item.subscriptionCycle?.billingDate(
                        item.startDate,
                        YearMonth.from(selectedDate)
                    )

                    if (billingDate == selectedDate) {
                        makeHomeEvent(item, billingDate)
                    } else {
                        null
                    }
                } else {
                    val (start, end) = displayRange(item)

                    if (!selectedDate.isBefore(start) && !selectedDate.isAfter(end)) {
                        makeHomeEvent(item, selectedDate)
                    } else {
                        null
                    }
                }
            }
        }

    // MARK: - Reload | 필요할 때 Room 다시 조회
    fun reload() {
        viewModelScope.launch {
            val items = repository.fetchItems()
            _uiState.update { it.copy(items = items) }
        }
    }

    // MARK: - Filter & Date | 홈 날짜·필터 변경
    fun selectType(type: ItemTypeFilter) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

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

    fun moveToToday() {
        val currentDate = LocalDate.now()
        _uiState.update {
            it.copy(
                visibleMonth = currentDate.withDayOfMonth(1),
                selectedDate = currentDate
            )
        }
    }

    fun pickDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                visibleMonth = date.withDayOfMonth(1),
                selectedDate = date
            )
        }
    }

    // MARK: - Type Filtering | 항목 유형 필터링
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

    // MARK: - Display Range | 쿠폰·이용권은 만료일, 일정은 전체 기간
    private fun displayRange(item: ManagedItem): Pair<LocalDate, LocalDate> {
        if (item.type == ManagedItemType.COUPON || item.type == ManagedItemType.PASS) {
            val expiryDate = item.endDate ?: item.startDate
            return expiryDate to expiryDate
        }

        return item.startDate to (item.endDate ?: item.startDate)
    }

    // MARK: - Home Event | 선택일 카드 데이터 구성
    private fun makeHomeEvent(item: ManagedItem, eventDate: LocalDate): HomeSampleEvent {
        val type = filterType(item.type)

        val iconRes = when (item.type) {
            ManagedItemType.COUPON -> AppIcon.coupon
            ManagedItemType.PASS -> AppIcon.pass
            ManagedItemType.SUBSCRIPTION -> AppIcon.subscription
            ManagedItemType.SCHEDULE -> AppIcon.schedule
        }

        val subtitle = when (item.type) {
            ManagedItemType.SUBSCRIPTION -> {
                val cycle = item.subscriptionCycle?.title ?: type.title
                val amount = item.subscriptionAmount?.let {
                    " · ${NumberFormat.getIntegerInstance(Locale.KOREA).format(it)}원"
                } ?: ""

                "${type.title} · $cycle$amount"
            }

            ManagedItemType.SCHEDULE -> {
                val time = item.scheduleTimeMinutes?.let {
                    "%02d:%02d".format(it / 60, it % 60)
                }

                listOfNotNull(
                    type.title,
                    time,
                    item.detail.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
            }

            else -> "${type.title} · ${item.detail}"
        }

        val currentDate = LocalDate.now()

        val statusLabel = when {
            eventDate == currentDate -> "오늘"
            eventDate == currentDate.plusDays(1) -> "내일"
            else -> "예정"
        }

        return HomeSampleEvent(
            id = item.id,
            date = eventDate,
            type = type,
            title = item.title,
            subtitle = subtitle,
            iconRes = iconRes,
            statusLabel = statusLabel,
            endDate = if (
                item.type == ManagedItemType.COUPON ||
                item.type == ManagedItemType.SUBSCRIPTION
            ) null else item.endDate
        )
    }
}
