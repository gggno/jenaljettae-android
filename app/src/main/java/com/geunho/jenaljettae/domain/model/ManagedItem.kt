package com.geunho.jenaljettae.domain.model

import java.time.LocalDate
import java.util.UUID

// MARK: - Managed Item Type | 앱에서 보관하는 항목 유형 정의
enum class ManagedItemType {
    COUPON,
    PASS,
    SUBSCRIPTION,
    SCHEDULE
}

// MARK: - Managed Item | 저장 기술에 의존하지 않는 공통 데이터 모델
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
    val createdAt: Long = System.currentTimeMillis()
)
