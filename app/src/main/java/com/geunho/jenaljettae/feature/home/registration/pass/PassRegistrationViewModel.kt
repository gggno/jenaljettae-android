package com.geunho.jenaljettae.features.home.registration.pass

import androidx.lifecycle.ViewModel
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.PassDraft
import com.geunho.jenaljettae.domain.usecase.SavePassUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Pass Registration UI State | 이용권 입력값과 저장 상태
data class PassRegistrationUiState(
    val title: String = "",
    val place: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val passNumber: String = "",
    val memo: String = "",
    val imageUri: String? = null,
    val isPhotoLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isInvalidDateRange: Boolean
        get() = startDate != null && endDate != null && endDate.isBefore(startDate)

    val canSave: Boolean
        get() = title.isNotBlank() && startDate != null && endDate != null &&
                !isInvalidDateRange && !isPhotoLoading && !isSaving && !hasSaved

    fun toDraft() = PassDraft(title, place, startDate, endDate, passNumber, memo, imageUri)
}

// MARK: - Pass Registration ViewModel | 입력값·저장 상태 관리
class PassRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PassRegistrationUiState())
    val uiState: StateFlow<PassRegistrationUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) { _uiState.update { it.copy(title = value) } }
    fun updatePlace(value: String) { _uiState.update { it.copy(place = value) } }
    fun updateStartDate(value: LocalDate) { _uiState.update { it.copy(startDate = value) } }
    fun updateEndDate(value: LocalDate) { _uiState.update { it.copy(endDate = value) } }
    fun updatePassNumber(value: String) { _uiState.update { it.copy(passNumber = value) } }
    fun updateMemo(value: String) { _uiState.update { it.copy(memo = value) } }
    fun setImageUri(value: String) { _uiState.update { it.copy(imageUri = value) } }
    fun removeImage() { _uiState.update { it.copy(imageUri = null) } }
    fun setPhotoLoading(value: Boolean) { _uiState.update { it.copy(isPhotoLoading = value) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    // MARK: - Save Pass | UI가 코루틴에서 호출하고 완료 항목을 반환
    suspend fun savePass(using: SavePassUseCase): ManagedItem? {
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
            _uiState.update { it.copy(isSaving = false, errorMessage = error.localizedMessage ?: "이용권 저장에 실패했어요.") }
            null
        }
    }
}
