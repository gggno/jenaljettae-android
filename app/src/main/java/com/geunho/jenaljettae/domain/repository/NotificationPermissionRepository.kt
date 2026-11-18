package com.geunho.jenaljettae.domain.repository

import com.geunho.jenaljettae.domain.model.NotificationPermissionStatus

// MARK: - Notification Permission Repository | 알림 권한 조회 인터페이스
interface NotificationPermissionRepository {
    fun authorizationStatus(): NotificationPermissionStatus
}
