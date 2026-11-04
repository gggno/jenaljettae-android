package com.geunho.jenaljettae.data.repository

import com.geunho.jenaljettae.data.local.ManagedItemDao
import com.geunho.jenaljettae.data.local.ManagedItemEntity
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.repository.ManagedItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// MARK: - Room Repository | Room DAO를 이용한 항목 저장·조회·삭제
class RoomManagedItemRepository(
    private val dao: ManagedItemDao
) : ManagedItemRepository {

    // MARK: - Observe Items | DB 변경을 Domain 모델 목록으로 전달
    override fun observeItems(): Flow<List<ManagedItem>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // MARK: - Fetch Items | 현재 저장된 데이터를 한 번 조회
    override suspend fun fetchItems(): List<ManagedItem> {
        return dao.observeAll().first().map { it.toDomain() }
    }

    // MARK: - Save Item | Domain 모델을 Entity로 변환해 저장
    override suspend fun saveItem(item: ManagedItem) {
        dao.upsert(item.toEntity())
    }

    // MARK: - Delete Item | 항목 ID로 삭제
    override suspend fun deleteItem(id: String) {
        dao.deleteById(id)
    }

    // MARK: - Entity Mapping | Room Entity를 Domain 모델로 변환
    private fun ManagedItemEntity.toDomain(): ManagedItem {
        return ManagedItem(
            id = id,
            type = ManagedItemType.valueOf(type),
            title = title,
            detail = detail,
            startDate = LocalDate.ofEpochDay(startDateEpochDay),
            endDate = endDateEpochDay?.let { LocalDate.ofEpochDay(it) },
            couponNumber = couponNumber,
            memo = memo,
            imageFileName = imageFileName,
            createdAt = createdAtEpochMillis
        )
    }

    // MARK: - Domain Mapping | Domain 모델을 Room Entity로 변환
    private fun ManagedItem.toEntity(): ManagedItemEntity {
        return ManagedItemEntity(
            id = id,
            type = type.name,
            title = title,
            detail = detail,
            startDateEpochDay = startDate.toEpochDay(),
            endDateEpochDay = endDate?.toEpochDay(),
            couponNumber = couponNumber,
            memo = memo,
            imageFileName = imageFileName,
            createdAtEpochMillis = createdAt
        )
    }
}
