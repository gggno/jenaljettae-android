package com.geunho.jenaljettae.domain.repository

// MARK: - Coupon Image Repository | 쿠폰 이미지 저장·삭제 규약
interface CouponImageRepository {
    suspend fun saveImage(sourceUri: String): String
    suspend fun deleteImage(fileName: String)
}
