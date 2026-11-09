package com.geunho.jenaljettae.domain.model

import java.time.LocalDate
import java.util.UUID

// MARK: - Managed Item Type | 앱에서 보관하는 항목 유형 정의
enum class ManagedItemType {
    COUPON, PASS, SUBSCRIPTION, SCHEDULE
}

// MARK: - Managed Item | 저장 방식에 의존하지 않는 공통 Domain 모델
data class ManagedItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ManagedItemType,
    val title: String,
    val detail: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val couponNumber: String? = null,
    val memo: String? = null,
    val imageFileName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val subscriptionCycle: SubscriptionCycle? = null,
    val subscriptionAmount: Int? = null,

    // MARK: - Schedule | 자정부터 지난 분. 미지정이면 null
    val scheduleTimeMinutes: Int? = null
)
