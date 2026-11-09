package com.geunho.jenaljettae.domain.model

import java.time.LocalDate

// MARK: - Subscription Draft | 구독 시작일이 반복 결제의 기준 날짜
// iOS의 SubscriptionDraft처럼 선택된 이미지 URI만 저장 UseCase로 전달
data class SubscriptionDraft(
    val title: String = "",
    val cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
    val subscriptionStartDate: LocalDate? = null,
    val amount: Int? = null,
    val memo: String = "",
    val imageUri: String? = null
)
