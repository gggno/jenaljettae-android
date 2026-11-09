package com.geunho.jenaljettae.features.home.registration

import androidx.compose.runtime.compositionLocalOf
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase
import com.geunho.jenaljettae.domain.usecase.SavePassUseCase
import com.geunho.jenaljettae.domain.usecase.SaveSubscriptionUseCase
import com.geunho.jenaljettae.domain.usecase.SaveScheduleUseCase

// MARK: - Coupon Registration | 쿠폰 저장
val LocalSaveCouponUseCase = compositionLocalOf<SaveCouponUseCase?> { null }
val LocalOnCouponSaved = compositionLocalOf<((ManagedItem) -> Unit)?> { null }

// MARK: - Pass Registration | 이용권 저장
val LocalSavePassUseCase = compositionLocalOf<SavePassUseCase?> { null }
val LocalOnPassSaved = compositionLocalOf<((ManagedItem) -> Unit)?> { null }

// MARK: - Subscription Registration | 정기 구독 저장
val LocalSaveSubscriptionUseCase = compositionLocalOf<SaveSubscriptionUseCase?> { null }
val LocalOnSubscriptionSaved = compositionLocalOf<((ManagedItem) -> Unit)?> { null }

// MARK: - Schedule Registration | 일정 저장
val LocalSaveScheduleUseCase = compositionLocalOf<SaveScheduleUseCase?> { null }
val LocalOnScheduleSaved = compositionLocalOf<((ManagedItem) -> Unit)?> { null }
