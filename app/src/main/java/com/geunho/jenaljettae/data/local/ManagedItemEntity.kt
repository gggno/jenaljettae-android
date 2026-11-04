package com.geunho.jenaljettae.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// MARK: - Managed Item Entity | Room 데이터베이스에 저장되는 항목 정의
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
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
