package com.geunho.jenaljettae.features.home.registration.schedule

import androidx.lifecycle.ViewModel
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ScheduleDraft
import com.geunho.jenaljettae.domain.usecase.SaveScheduleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// MARK: - Schedule UI State | 일정 입력 및 저장 상태
data class ScheduleRegistrationUiState(
    val title: String = "",
    val place: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val timeMinutes: Int? = null,
    val memo: String = "",
    val imageUri: String? = null,
    val isPhotoLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
) {
    // MARK: - Date Validation | 종료일 유효성
    val isInvalidDateRange: Boolean
        get() = startDate != null && endDate != null && endDate.isBefore(startDate)

    // MARK: - Can Save | 일정명·시작일·종료일 필수
    val canSave: Boolean
        get() = title.isNotBlank() && startDate != null && endDate != null &&
                !isInvalidDateRange && !isPhotoLoading && !isSaving && !hasSaved

    // MARK: - Draft | UseCase에 전달할 데이터 생성
    fun toDraft() = ScheduleDraft(
        title = title,
        place = place,
        startDate = startDate,
        endDate = endDate,
        timeMinutes = timeMinutes,
        memo = memo,
        imageUri = imageUri
    )
}

// MARK: - Schedule Registration ViewModel | 입력 및 저장 상태 관리
class ScheduleRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleRegistrationUiState())
    val uiState: StateFlow<ScheduleRegistrationUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) { _uiState.update { it.copy(title = value) } }
    fun updatePlace(value: String) { _uiState.update { it.copy(place = value) } }
    fun updateStartDate(value: LocalDate) { _uiState.update { it.copy(startDate = value) } }
    fun updateEndDate(value: LocalDate) { _uiState.update { it.copy(endDate = value) } }
    fun updateTime(minutes: Int) { _uiState.update { it.copy(timeMinutes = minutes) } }
    fun clearTime() { _uiState.update { it.copy(timeMinutes = null) } }
    fun updateMemo(value: String) { _uiState.update { it.copy(memo = value) } }
    fun setImageUri(value: String) { _uiState.update { it.copy(imageUri = value) } }
    fun removeImage() { _uiState.update { it.copy(imageUri = null) } }
    fun setPhotoLoading(value: Boolean) { _uiState.update { it.copy(isPhotoLoading = value) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    // MARK: - Save Schedule | UseCase로 저장하고 결과 반환
    suspend fun saveSchedule(using: SaveScheduleUseCase): ManagedItem? {
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
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = error.localizedMessage ?: "일정 저장에 실패했어요."
                )
            }
            null
        }
    }
}
