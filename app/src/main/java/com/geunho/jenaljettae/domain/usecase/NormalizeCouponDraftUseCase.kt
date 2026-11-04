package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.CouponDraft

// MARK: - Normalize Coupon Draft UseCase | AI 분석 결과의 공백·빈값·중복 안내 정리
class NormalizeCouponDraftUseCase {

    fun execute(draft: CouponDraft): CouponDraft {
        return draft.copy(
            title = draft.title.normalizedOrNull(),
            brand = draft.brand.normalizedOrNull(),
            couponNumber = draft.couponNumber.normalizedOrNull(),
            reviewNotes = draft.reviewNotes
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        )
    }

    // MARK: - Normalize Text | 공백만 있는 문자열은 null로 변환
    private fun String?.normalizedOrNull(): String? {
        return this?.trim()?.takeIf { it.isNotEmpty() }
    }
}
