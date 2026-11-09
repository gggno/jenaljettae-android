package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.model.PassDraft
import com.geunho.jenaljettae.domain.repository.CouponImageRepository
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

// MARK: - Save Pass UseCase | 이용권 입력값 검증, 이미지·Room 저장
// 기존 CouponImageRepository를 재사용해 이미지 스키마 변경을 피함
class SavePassUseCase(
    private val repository: ManagedItemRepository,
    private val imageRepository: CouponImageRepository
) {
    // MARK: - Execute | 필수값 확인 후 Domain 모델 생성·저장
    suspend fun execute(draft: PassDraft): ManagedItem {
        val title = draft.title.trim()

        require(title.isNotEmpty()) { "이용권 이름을 입력해주세요." }
        val startDate = requireNotNull(draft.startDate) { "시작일을 선택해주세요." }
        val endDate = requireNotNull(draft.endDate) { "종료일을 선택해주세요." }
        require(!endDate.isBefore(startDate)) { "종료일은 시작일보다 빠를 수 없어요." }

        // MARK: - Save Image | Photo Picker URI를 앱 내부 파일로 영구 저장
        val savedImageFileName = draft.imageUri?.let { imageRepository.saveImage(it) }

        // MARK: - Create Item | 기존 ManagedItem을 이용권 유형으로 재사용
        val item = ManagedItem(
            type = ManagedItemType.PASS,
            title = title,
            detail = draft.place.trim(),
            startDate = startDate,
            endDate = endDate,
            couponNumber = draft.passNumber.trim().ifEmpty { null },
            memo = draft.memo.trim().ifEmpty { null },
            imageFileName = savedImageFileName
        )

        // MARK: - Save Item | Room 실패 시 새 이미지 파일도 제거
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
