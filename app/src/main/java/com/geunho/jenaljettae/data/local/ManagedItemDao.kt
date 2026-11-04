package com.geunho.jenaljettae.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// MARK: - Managed Item DAO | 일정 데이터 저장·조회·수정·삭제
@Dao
interface ManagedItemDao {

    // MARK: - Observe | 데이터 변경 시 최신 목록 전달
    @Query("SELECT * FROM managed_items ORDER BY startDateEpochDay ASC")
    fun observeAll(): Flow<List<ManagedItemEntity>>

    // MARK: - Get | ID로 특정 일정 조회
    @Query("SELECT * FROM managed_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ManagedItemEntity?

    // MARK: - Upsert | 신규 저장 또는 기존 일정 수정
    @Upsert
    suspend fun upsert(item: ManagedItemEntity)

    // MARK: - Delete | ID로 일정 삭제
    @Query("DELETE FROM managed_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
