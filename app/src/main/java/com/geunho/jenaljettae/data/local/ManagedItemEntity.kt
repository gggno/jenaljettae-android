package com.geunho.jenaljettae.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// MARK: - Managed Item Entity | Room 공통 항목 테이블
@Entity(tableName = "managed_items")
data class ManagedItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String,
    val title: String,
    val detail: String = "",
    val startDateEpochDay: Long,
    val endDateEpochDay: Long? = null,
    val couponNumber: String? = null,
    val memo: String? = null,
    val imageFileName: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val subscriptionCycle: String? = null,
    val subscriptionAmount: Int? = null,

    // MARK: - Schedule | 선택 시작 시간
    val scheduleTimeMinutes: Int? = null
)
