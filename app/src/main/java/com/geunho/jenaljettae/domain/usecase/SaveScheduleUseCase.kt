package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.model.ScheduleDraft
import com.geunho.jenaljettae.domain.repository.CouponImageRepository
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

// MARK: - Save Schedule UseCase | 일정 검증 및 이미지·Room 저장
class SaveScheduleUseCase(
    private val repository: ManagedItemRepository,
    private val imageRepository: CouponImageRepository
) {
    // MARK: - Execute | 필수 입력 검증 및 일정 저장
    suspend fun execute(draft: ScheduleDraft): ManagedItem {
        val title = draft.title.trim()

        require(title.isNotEmpty()) { "일정명을 입력해주세요." }

        val startDate = requireNotNull(draft.startDate) {
            "시작일을 선택해주세요."
        }

        val endDate = requireNotNull(draft.endDate) {
            "종료일을 선택해주세요."
        }

        require(!endDate.isBefore(startDate)) {
            "종료일은 시작일보다 빠를 수 없어요."
        }

        require(draft.timeMinutes == null || draft.timeMinutes in 0..1439) {
            "시작 시간을 확인해주세요."
        }

        // MARK: - Image | 기존 공통 이미지 저장소 재사용
        val savedImageFileName = draft.imageUri?.let {
            imageRepository.saveImage(it)
        }

        // MARK: - Item | 공통 ManagedItem 생성
        val item = ManagedItem(
            type = ManagedItemType.SCHEDULE,
            title = title,
            detail = draft.place.trim(),
            startDate = startDate,
            endDate = endDate,
            memo = draft.memo.trim().ifEmpty { null },
            imageFileName = savedImageFileName,
            scheduleTimeMinutes = draft.timeMinutes
        )

        // MARK: - Save | Room 저장 실패 시 이미지 정리
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
