package com.geunho.jenaljettae.data.repository

import com.geunho.jenaljettae.data.local.ManagedItemDao
import com.geunho.jenaljettae.data.local.ManagedItemEntity
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.model.SubscriptionCycle
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// MARK: - Room Repository | 쿠폰·이용권·구독·일정 공통 저장소
class RoomManagedItemRepository(
    private val dao: ManagedItemDao
) : ManagedItemRepository {

    // MARK: - Observe | Room 변경 감지
    override fun observeItems(): Flow<List<ManagedItem>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    // MARK: - Fetch | 전체 항목 조회
    override suspend fun fetchItems(): List<ManagedItem> =
        dao.observeAll().first().map { it.toDomain() }

    // MARK: - Save | 새 항목 저장 및 수정
    override suspend fun saveItem(item: ManagedItem) { dao.upsert(item.toEntity()) }

    // MARK: - Delete | 항목 삭제
    override suspend fun deleteItem(id: String) { dao.deleteById(id) }

    // MARK: - Entity to Domain | Room Entity를 Domain 모델로 변환
    private fun ManagedItemEntity.toDomain(): ManagedItem = ManagedItem(
        id = id,
        type = ManagedItemType.valueOf(type),
        title = title,
        detail = detail,
        startDate = LocalDate.ofEpochDay(startDateEpochDay),
        endDate = endDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        couponNumber = couponNumber,
        memo = memo,
        imageFileName = imageFileName,
        createdAt = createdAtEpochMillis,
        subscriptionCycle = SubscriptionCycle.fromRawValue(subscriptionCycle),
        subscriptionAmount = subscriptionAmount,
        scheduleTimeMinutes = scheduleTimeMinutes
    )

    // MARK: - Domain to Entity | Domain 모델을 Entity로 변환
    private fun ManagedItem.toEntity(): ManagedItemEntity = ManagedItemEntity(
        id = id,
        type = type.name,
        title = title,
        detail = detail,
        startDateEpochDay = startDate.toEpochDay(),
        endDateEpochDay = endDate?.toEpochDay(),
        couponNumber = couponNumber,
        memo = memo,
        imageFileName = imageFileName,
        createdAtEpochMillis = createdAt,
        subscriptionCycle = subscriptionCycle?.rawValue,
        subscriptionAmount = subscriptionAmount,
        scheduleTimeMinutes = scheduleTimeMinutes
    )
}
