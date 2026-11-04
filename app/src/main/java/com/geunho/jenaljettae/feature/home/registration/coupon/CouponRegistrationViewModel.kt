package com.geunho.jenaljettae.features.home.registration.coupon

import androidx.lifecycle.ViewModel
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Coupon Registration UI State | 쿠폰 입력값·이미지·저장 상태 정의
data class CouponRegistrationUiState(
    val couponTitle: String = "",
    val brand: String = "",
    val expiryDate: LocalDate? = null,
    val couponNumber: String = "",
    val memo: String = "",
    val imageUri: String? = null,
    val isLoadingPhoto: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = couponTitle.isNotBlank() &&
                expiryDate != null &&
                !isLoadingPhoto &&
                !isSaving &&
                !hasSaved
}

// MARK: - Coupon Registration ViewModel | 입력 상태 및 쿠폰 이미지 저장 관리
class CouponRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CouponRegistrationUiState())
    val uiState: StateFlow<CouponRegistrationUiState> = _uiState.asStateFlow()

    private var registrationSessionId: String? = null

    // MARK: - Registration Session | 새 등록을 시작할 때 이전 입력값 초기화
    fun startSession(sessionId: String) {
        if (registrationSessionId == sessionId) return
        registrationSessionId = sessionId
        _uiState.value = CouponRegistrationUiState()
    }

    // MARK: - Input Updates | 사용자 입력값 변경
    fun updateCouponTitle(value: String) = _uiState.update { it.copy(couponTitle = value) }
    fun updateBrand(value: String) = _uiState.update { it.copy(brand = value) }
    fun updateExpiryDate(value: LocalDate?) = _uiState.update { it.copy(expiryDate = value) }
    fun updateCouponNumber(value: String) = _uiState.update { it.copy(couponNumber = value) }
    fun updateMemo(value: String) = _uiState.update { it.copy(memo = value) }

    // MARK: - Coupon Image | 이미지 추가·변경·삭제 및 로딩 상태 관리
    fun setImageUri(uri: String) = _uiState.update { it.copy(imageUri = uri) }
    fun removeImage() = _uiState.update { it.copy(imageUri = null) }
    fun setPhotoLoading(isLoading: Boolean) = _uiState.update { it.copy(isLoadingPhoto = isLoading) }

    // MARK: - Clear Error | 오류 안내를 닫을 때 메시지 초기화
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    // MARK: - Save Coupon | 쿠폰 정보와 선택한 이미지를 UseCase에 전달
    suspend fun saveCoupon(using: SaveCouponUseCase): ManagedItem? {
        val state = _uiState.value
        if (!state.canSave) return null

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        return try {
            val savedItem = using.execute(
                title = state.couponTitle,
                brand = state.brand,
                expiryDate = state.expiryDate,
                couponNumber = state.couponNumber,
                memo = state.memo,
                imageUri = state.imageUri
            )

            _uiState.update { it.copy(hasSaved = true) }
            savedItem

        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(errorMessage = error.localizedMessage ?: "쿠폰 저장에 실패했어요.")
            }
            null
        } finally {
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
