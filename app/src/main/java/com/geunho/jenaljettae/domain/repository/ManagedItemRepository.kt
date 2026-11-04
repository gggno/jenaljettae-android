package com.geunho.jenaljettae.domain.repository

import com.geunho.jenaljettae.domain.model.ManagedItem
import kotlinx.coroutines.flow.Flow

// MARK: - Managed Item Repository | 저장 방식과 무관한 데이터 접근 규약
interface ManagedItemRepository {
    fun observeItems(): Flow<List<ManagedItem>>     // 데이터가 변경될 때마다 최신 목록 전달
    suspend fun fetchItems(): List<ManagedItem>     // 현재 저장된 목록을 한 번 조회
    suspend fun saveItem(item: ManagedItem)         // 신규 저장 또는 기존 항목 수정
    suspend fun deleteItem(id: String)              // ID로 항목 삭제
}
