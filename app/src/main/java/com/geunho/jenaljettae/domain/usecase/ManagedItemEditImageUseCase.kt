package com.geunho.jenaljettae.domain.usecase

import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.repository.CouponImageRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

// MARK: - Image Change | 수정 시 기존 이미지 재사용·새 파일 생성·이전 파일 정리
internal data class ManagedItemEditImageUseCase(
    val finalFileName: String?,
    val newlyCreatedFileName: String?,
    val originalFileName: String?
) {
    suspend fun rollbackNew(imageRepository: CouponImageRepository) {
        val newFileName = newlyCreatedFileName ?: return
        withContext(NonCancellable) { imageRepository.deleteImage(newFileName) }
    }

    suspend fun cleanupReplaced(imageRepository: CouponImageRepository) {
        val oldFileName = originalFileName ?: return
        if (oldFileName == finalFileName) return
        // DB 저장 이후 이미지 정리 실패가 데이터 저장 성공을 덮어쓰지 않게 처리
        withContext(NonCancellable) { runCatching { imageRepository.deleteImage(oldFileName) } }
    }
}

// MARK: - Prepare Image | 선택 이미지가 기존 내부 파일이면 복사하지 않음
internal suspend fun prepareItemEditImage(
    imageUri: String?,
    editingItem: ManagedItem?,
    imageRepository: CouponImageRepository
): ManagedItemEditImageUseCase {
    val originalName = editingItem?.imageFileName
    val isOriginalImage = originalName != null && imageUri != null &&
        imageUri.startsWith("file://") && imageUri.substringAfterLast('/') == originalName
    val newlyCreated = if (isOriginalImage) null else imageUri?.let { imageRepository.saveImage(it) }
    return ManagedItemEditImageUseCase(
        finalFileName = if (isOriginalImage) originalName else newlyCreated,
        newlyCreatedFileName = newlyCreated,
        originalFileName = originalName
    )
}
