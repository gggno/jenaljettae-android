package com.geunho.jenaljettae.domain.repository

import com.geunho.jenaljettae.domain.model.CouponDraft

// MARK: - Coupon Analysis Repository | 쿠폰 이미지 AI 분석 규약
interface CouponAnalysisRepository {
    suspend fun analyze(imageData: ByteArray): CouponDraft
}
