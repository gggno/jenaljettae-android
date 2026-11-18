package com.geunho.jenaljettae.data.repository

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.geunho.jenaljettae.domain.model.NotificationPermissionStatus
import com.geunho.jenaljettae.domain.repository.NotificationPermissionRepository

// MARK: - System Notification Permission Repository | 시스템 알림 허용 여부 조회
class SystemNotificationPermissionRepository(context: Context) : NotificationPermissionRepository {
    private val appContext = context.applicationContext

    override fun authorizationStatus(): NotificationPermissionStatus {
        return if (NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            NotificationPermissionStatus.AUTHORIZED
        } else {
            NotificationPermissionStatus.DENIED
        }
    }
}
