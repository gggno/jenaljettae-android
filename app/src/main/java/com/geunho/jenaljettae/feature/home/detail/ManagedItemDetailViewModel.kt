package com.geunho.jenaljettae.features.home.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// MARK: - Detail UI State | 상세 조회 상태
data class ManagedItemDetailUiState(
    val item: ManagedItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// MARK: - Managed Item Detail ViewModel | 저장소에서 ID로 항목 조회
class ManagedItemDetailViewModel(
    private val itemId: String,
    private val repository: ManagedItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManagedItemDetailUiState())
    val uiState: StateFlow<ManagedItemDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeItems().collectLatest { items ->
                    val item = items.firstOrNull { it.id == itemId }
                    _uiState.value = ManagedItemDetailUiState(
                        item = item,
                        isLoading = false,
                        errorMessage = if (item == null) "저장된 항목을 찾을 수 없어요." else null
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = ManagedItemDetailUiState(
                    isLoading = false,
                    errorMessage = "상세 정보를 불러오지 못했어요."
                )
            }
        }
    }
}
