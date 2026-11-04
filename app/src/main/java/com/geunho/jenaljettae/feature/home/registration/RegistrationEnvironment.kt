package com.geunho.jenaljettae.features.home.registration

import androidx.compose.runtime.compositionLocalOf
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase

// MARK: - Registration Environment | 등록 화면에 저장 기능과 완료 콜백 제공
val LocalSaveCouponUseCase = compositionLocalOf<SaveCouponUseCase?> { null }
val LocalOnCouponSaved = compositionLocalOf<((ManagedItem) -> Unit)?> { null }
