package com.geunho.jenaljettae.data.repository

import android.net.Uri
import com.geunho.jenaljettae.data.local.CouponImageStorage
import com.geunho.jenaljettae.domain.repository.CouponImageRepository

// MARK: - Local Coupon Image Repository | 앱 내부 저장소를 이용한 이미지 저장 구현
class LocalCouponImageRepository(
    private val imageStorage: CouponImageStorage
) : CouponImageRepository {

    // MARK: - Save Image | 선택한 이미지 URI를 내부 저장소에 저장
    override suspend fun saveImage(sourceUri: String): String {
        return imageStorage.saveImage(Uri.parse(sourceUri))
    }

    // MARK: - Delete Image | 저장된 쿠폰 이미지 파일 삭제
    override suspend fun deleteImage(fileName: String) {
        imageStorage.deleteImage(fileName)
    }
}
