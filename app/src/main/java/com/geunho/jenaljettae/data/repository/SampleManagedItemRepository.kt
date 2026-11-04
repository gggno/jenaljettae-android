package com.geunho.jenaljettae.data.repository

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Sample Repository | Preview 및 테스트용 메모리 저장소
class SampleManagedItemRepository : ManagedItemRepository {
    private val today = LocalDate.now()

    private val items = MutableStateFlow(
        listOf(
            ManagedItem(
                id = "sample-1",
                type = ManagedItemType.COUPON,
                title = "아메리카노 교환권",
                detail = "만료일",
                startDate = today
            ),
            ManagedItem(
                id = "sample-2",
                type = ManagedItemType.SCHEDULE,
                title = "저녁 식사 일정",
                detail = "오후 7:00",
                startDate = today
            ),
            ManagedItem(
                id = "sample-3",
                type = ManagedItemType.PASS,
                title = "헬스장 이용권",
                detail = "종료일",
                startDate = today.plusDays(1)
            ),
            ManagedItem(
                id = "sample-4",
                type = ManagedItemType.SUBSCRIPTION,
                title = "음악 스트리밍 구독",
                detail = "예상 갱신일",
                startDate = today.plusDays(1)
            ),
            ManagedItem(
                id = "sample-5",
                type = ManagedItemType.SCHEDULE,
                title = "가족 여행 숙소 일정",
                detail = "7박 8일",
                startDate = today,
                endDate = today.plusDays(7)
            )
        )
    )

    // MARK: - Observe Items | 메모리 데이터의 변경 사항 전달
    override fun observeItems(): Flow<List<ManagedItem>> = items

    // MARK: - Fetch Items | 현재 샘플 데이터 반환
    override suspend fun fetchItems(): List<ManagedItem> = items.value

    // MARK: - Save Item | 샘플 항목 추가 및 수정
    override suspend fun saveItem(item: ManagedItem) {
        items.update { current ->
            if (current.any { it.id == item.id }) {
                current.map { if (it.id == item.id) item else it }
            } else {
                current + item
            }
        }
    }

    // MARK: - Delete Item | ID에 해당하는 샘플 항목 삭제
    override suspend fun deleteItem(id: String) {
        items.update { current -> current.filterNot { it.id == id } }
    }
}
