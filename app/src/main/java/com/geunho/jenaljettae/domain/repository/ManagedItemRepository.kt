package com.geunho.jenaljettae.domain.repository

import com.geunho.jenaljettae.domain.model.ManagedItem

// MARK: - Managed Item Repository | 항목 조회 기능의 규약 정의
interface ManagedItemRepository {
    fun fetchItems(): List<ManagedItem>
}
