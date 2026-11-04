package com.geunho.jenaljettae.domain.model

import java.time.LocalDate

// MARK: - Coupon Draft | AI 이미지 분석으로 추출한 쿠폰 정보
data class CouponDraft(
    val title: String? = null,
    val brand: String? = null,
    val expiryDate: LocalDate? = null,
    val couponNumber: String? = null,
    val reviewNotes: List<String> = emptyList()
)
