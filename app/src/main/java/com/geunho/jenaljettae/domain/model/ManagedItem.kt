package com.geunho.jenaljettae.domain.model

import java.time.LocalDate

// MARK: - Managed Item Type | 앱에서 보관하는 항목 유형 정의
enum class ManagedItemType {
    COUPON,
    PASS,
    SUBSCRIPTION,
    SCHEDULE
}

// MARK: - Managed Item | UI나 저장 방식에 의존하지 않는 공통 데이터 모델
data class ManagedItem(
    val id: Int,
    val type: ManagedItemType,
    val title: String,
    val detail: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
)
