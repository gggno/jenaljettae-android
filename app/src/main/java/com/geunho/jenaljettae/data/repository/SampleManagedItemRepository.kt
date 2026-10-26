package com.geunho.jenaljettae.data.repository

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import java.time.LocalDate

// MARK: - Sample Repository | Room 도입 전 테스트용 항목 5개 제공
class SampleManagedItemRepository : ManagedItemRepository {
    override fun fetchItems(): List<ManagedItem> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val hotelCheckOut = today.plusDays(7)

        return listOf(
            ManagedItem(1, ManagedItemType.COUPON, "아메리카노 교환권", "만료일", today),
            ManagedItem(2, ManagedItemType.SCHEDULE, "저녁 식사 일정", "오후 7:00", today),
            ManagedItem(3, ManagedItemType.PASS, "헬스장 이용권", "종료일", tomorrow),
            ManagedItem(4, ManagedItemType.SUBSCRIPTION, "음악 스트리밍 구독", "예상 갱신일", tomorrow),
            ManagedItem(5, ManagedItemType.SCHEDULE, "가족 여행 숙소 일정", "7박 8일", today, hotelCheckOut)
        )
    }
}
