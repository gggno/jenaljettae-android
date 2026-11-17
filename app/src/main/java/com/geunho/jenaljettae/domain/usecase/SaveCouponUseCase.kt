
package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.CouponImageRepository
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.time.LocalDate

// MARK: - Save Coupon UseCase | 쿠폰 필수값 검증 및 이미지와 함께 저장
class SaveCouponUseCase(
    private val repository: ManagedItemRepository,
    private val imageRepository: CouponImageRepository
) {

    suspend fun execute(
        title: String,
        brand: String,
        expiryDate: LocalDate?,
        couponNumber: String,
        memo: String,
        imageUri: String? = null,
        editingItem: ManagedItem? = null
    ): ManagedItem {
        require(editingItem == null || editingItem.type == ManagedItemType.COUPON)
        val trimmedTitle = title.trim()

        // MARK: - Validation | 상품명과 만료일 필수값 검증
        require(trimmedTitle.isNotEmpty()) { "상품명을 입력해주세요." }
        requireNotNull(expiryDate) { "만료일을 선택해주세요." }

        // MARK: - Save Image | 선택한 이미지가 있으면 내부 저장소에 저장
        val imageChange = prepareItemEditImage(imageUri, editingItem, imageRepository)
        val savedImageFileName = imageChange.finalFileName

        // MARK: - Create Item | 쿠폰 정보와 이미지 파일명으로 모델 생성
        val item = ManagedItem(
            id = editingItem?.id ?: java.util.UUID.randomUUID().toString(),
            createdAt = editingItem?.createdAt ?: System.currentTimeMillis(),
            type = ManagedItemType.COUPON,
            title = trimmedTitle,
            detail = brand.trim(),
            startDate = editingItem?.startDate ?: LocalDate.now(),
            endDate = expiryDate,
            couponNumber = couponNumber.trim().ifEmpty { null },
            memo = memo.trim().ifEmpty { null },
            imageFileName = savedImageFileName
        )

        // MARK: - Save Item | Room 저장 실패 시 생성된 이미지 파일 정리
        try {
            repository.saveItem(item)
        } catch (error: Exception) {
            if (imageChange.newlyCreatedFileName != null) {
                withContext(NonCancellable) {
                    try {
                        imageRepository.deleteImage(imageChange.newlyCreatedFileName)
                    } catch (cleanupError: Exception) {
                        if (cleanupError is CancellationException) throw cleanupError
                        error.addSuppressed(cleanupError)
                    }
                }
            }
            throw error
        }

        imageChange.cleanupReplaced(imageRepository)
        return item
    }
}
