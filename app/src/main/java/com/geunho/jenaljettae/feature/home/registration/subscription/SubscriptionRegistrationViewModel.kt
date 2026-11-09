package com.geunho.jenaljettae.features.home.registration.subscription

import androidx.lifecycle.ViewModel
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.SubscriptionCycle
import com.geunho.jenaljettae.domain.model.SubscriptionDraft
import com.geunho.jenaljettae.domain.usecase.SaveSubscriptionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Subscription UI State | iOS의 @Published 입력값에 해당
// amountText에는 콤마 없는 숫자 문자열만 보관
data class SubscriptionRegistrationUiState(
    val title: String = "",
    val cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
    val subscriptionStartDate: LocalDate? = null,
    val amountText: String = "",
    val memo: String = "",
    val imageUri: String? = null,
    val isPhotoLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isInvalidAmount: Boolean get() = amountText.isNotEmpty() && amountText.toIntOrNull() == null
    val canSave: Boolean get() = title.isNotBlank() && subscriptionStartDate != null &&
            !isInvalidAmount && !isPhotoLoading && !isSaving && !hasSaved

    fun toDraft(): SubscriptionDraft = SubscriptionDraft(
        title = title,
        cycle = cycle,
        subscriptionStartDate = subscriptionStartDate,
        amount = amountText.takeIf { it.isNotEmpty() }?.toIntOrNull(),
        memo = memo,
        imageUri = imageUri
    )
}

// MARK: - Subscription ViewModel | 사용자 입력·검증·저장 상태 관리
class SubscriptionRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionRegistrationUiState())
    val uiState: StateFlow<SubscriptionRegistrationUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) { _uiState.update { it.copy(title = value) } }
    fun updateCycle(value: SubscriptionCycle) { _uiState.update { it.copy(cycle = value) } }
    fun updateStartDate(value: LocalDate) { _uiState.update { it.copy(subscriptionStartDate = value) } }
    fun updateAmount(value: String) { _uiState.update { it.copy(amountText = value.filter { digit -> digit in '0'..'9' }) } }
    fun updateMemo(value: String) { _uiState.update { it.copy(memo = value) } }
    fun setImageUri(value: String) { _uiState.update { it.copy(imageUri = value) } }
    fun removeImage() { _uiState.update { it.copy(imageUri = null) } }
    fun setPhotoLoading(value: Boolean) { _uiState.update { it.copy(isPhotoLoading = value) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    // MARK: - Save | Room 저장 완료 시 동일 항목 반환
    suspend fun saveSubscription(using: SaveSubscriptionUseCase): ManagedItem? {
        val state = _uiState.value
        if (!state.canSave) return null

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        return try {
            val savedItem = using.execute(state.toDraft())
            _uiState.update { it.copy(isSaving = false, hasSaved = true) }
            savedItem
        } catch (error: CancellationException) {
            _uiState.update { it.copy(isSaving = false) }
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(isSaving = false, errorMessage = error.localizedMessage ?: "정기 구독 저장에 실패했어요.") }
            null
        }
    }
}
