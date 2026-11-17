package com.geunho.jenaljettae.features.home.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geunho.jenaljettae.data.local.AppDatabase
import com.geunho.jenaljettae.data.local.CouponImageStorage
import com.geunho.jenaljettae.data.repository.LocalCouponImageRepository
import com.geunho.jenaljettae.data.repository.RoomManagedItemRepository
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.model.ManagedItemType
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase
import com.geunho.jenaljettae.domain.usecase.SavePassUseCase
import com.geunho.jenaljettae.domain.usecase.SaveScheduleUseCase
import com.geunho.jenaljettae.domain.usecase.SaveSubscriptionUseCase
import com.geunho.jenaljettae.features.home.registration.LocalOnCouponSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnPassSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnScheduleSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnSubscriptionSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveCouponUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSavePassUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveScheduleUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveSubscriptionUseCase
import com.geunho.jenaljettae.features.home.registration.coupon.CouponManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.pass.PassManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.schedule.ScheduleManualEntryScreen
import com.geunho.jenaljettae.features.home.registration.subscription.SubscriptionManualEntryScreen

// MARK: - Edit Dependencies | 기존 등록 UseCase 그대로 사용
private data class EditDependencies(
    val coupon: SaveCouponUseCase,
    val pass: SavePassUseCase,
    val subscription: SaveSubscriptionUseCase,
    val schedule: SaveScheduleUseCase
)

// MARK: - Managed Item Edit Route | 등록 UI 4종을 수정 화면으로 재사용
@Composable
internal fun ManagedItemEditRoute(item: ManagedItem, onClose: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val dependencies = remember(context) {
        val repository = RoomManagedItemRepository(AppDatabase.getInstance(context).managedItemDao())
        val imageRepository = LocalCouponImageRepository(CouponImageStorage(context))
        EditDependencies(
            coupon = SaveCouponUseCase(repository, imageRepository),
            pass = SavePassUseCase(repository, imageRepository),
            subscription = SaveSubscriptionUseCase(repository, imageRepository),
            schedule = SaveScheduleUseCase(repository, imageRepository)
        )
    }

    // MARK: - Save Callback | Room 관찰로 상세화면 데이터 갱신 후 수정 화면 닫기
    val onSaved: (ManagedItem) -> Unit = { onClose() }
    CompositionLocalProvider(
        LocalSaveCouponUseCase provides dependencies.coupon,
        LocalOnCouponSaved provides onSaved,
        LocalSavePassUseCase provides dependencies.pass,
        LocalOnPassSaved provides onSaved,
        LocalSaveSubscriptionUseCase provides dependencies.subscription,
        LocalOnSubscriptionSaved provides onSaved,
        LocalSaveScheduleUseCase provides dependencies.schedule,
        LocalOnScheduleSaved provides onSaved
    ) {
        when (item.type) {
            ManagedItemType.COUPON -> CouponManualEntryScreen(
                onBack = onClose, editingItem = item,
                registrationViewModel = viewModel(key = "managed-edit-coupon:${item.id}")
            )
            ManagedItemType.PASS -> PassManualEntryScreen(
                onBack = onClose, editingItem = item,
                registrationViewModel = viewModel(key = "managed-edit-pass:${item.id}")
            )
            ManagedItemType.SUBSCRIPTION -> SubscriptionManualEntryScreen(
                onBack = onClose, editingItem = item,
                registrationViewModel = viewModel(key = "managed-edit-subscription:${item.id}")
            )
            ManagedItemType.SCHEDULE -> ScheduleManualEntryScreen(
                onBack = onClose, editingItem = item,
                registrationViewModel = viewModel(key = "managed-edit-schedule:${item.id}")
            )
        }
    }
}
