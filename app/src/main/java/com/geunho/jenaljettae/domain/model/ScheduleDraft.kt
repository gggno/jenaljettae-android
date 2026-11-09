package com.geunho.jenaljettae.domain.model

import java.time.LocalDate

// MARK: - Schedule Draft | 일정 등록 화면의 입력값
data class ScheduleDraft(
    val title: String = "",
    val place: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val timeMinutes: Int? = null,
    val memo: String = "",
    val imageUri: String? = null
)
