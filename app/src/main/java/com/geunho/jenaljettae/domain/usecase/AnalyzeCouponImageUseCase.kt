package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.CouponDraft
import com.geunho.jenaljettae.domain.repository.CouponAnalysisRepository

// MARK: - Analyze Coupon Image UseCase | AI 분석 요청 및 추출 정보 정규화
class AnalyzeCouponImageUseCase(
    private val repository: CouponAnalysisRepository,
    private val normalizeCouponDraftUseCase: NormalizeCouponDraftUseCase = NormalizeCouponDraftUseCase()
) {

    // MARK: - Execute Analysis | 이미지 분석 후 정리된 쿠폰 정보 반환
    suspend fun execute(imageData: ByteArray): CouponDraft {
        require(imageData.isNotEmpty()) { "분석할 이미지가 비어 있어요." }

        val draft = repository.analyze(imageData)
        return normalizeCouponDraftUseCase.execute(draft)
    }
}
