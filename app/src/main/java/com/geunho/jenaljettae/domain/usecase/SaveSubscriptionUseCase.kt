package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.model.SubscriptionDraft
import com.geunho.jenaljettae.domain.repository.CouponImageRepository
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

// MARK: - Save Subscription UseCase | 입력 검증 → 이미지 영구 저장 → Room 저장
class SaveSubscriptionUseCase(
    private val repository: ManagedItemRepository,
    private val imageRepository: CouponImageRepository
) {
    suspend fun execute(draft: SubscriptionDraft): ManagedItem {
        val title = draft.title.trim()
        require(title.isNotEmpty()) { "구독 서비스명을 입력해주세요." }
        val startDate = requireNotNull(draft.subscriptionStartDate) { "구독 시작일을 선택해주세요." }
        require(draft.amount == null || draft.amount >= 0) { "결제 금액을 확인해주세요." }

        // MARK: - Save Image | 쿠폰·이용권의 기존 영구 이미지 저장소 재사용
        val savedImageFileName = draft.imageUri?.let { imageRepository.saveImage(it) }
        val item = ManagedItem(
            type = ManagedItemType.SUBSCRIPTION,
            title = title,
            startDate = startDate,
            endDate = null,
            memo = draft.memo.trim().ifEmpty { null },
            imageFileName = savedImageFileName,
            subscriptionCycle = draft.cycle,
            subscriptionAmount = draft.amount
        )

        try {
            repository.saveItem(item)
        } catch (error: Exception) {
            if (savedImageFileName != null) {
                withContext(NonCancellable) {
                    try {
                        imageRepository.deleteImage(savedImageFileName)
                    } catch (cleanupError: Exception) {
                        if (cleanupError is CancellationException) throw cleanupError
                        error.addSuppressed(cleanupError)
                    }
                }
            }
            throw error
        }
        return item
    }
}
