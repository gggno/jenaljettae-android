package com.geunho.jenaljettae.domain.model

import java.time.LocalDate

// MARK: - Pass Draft | 이용권 등록 화면에서 입력한 데이터
// Swift의 PassDraft와 동일하게 필수값 선택 전 상태를 표현
// 이미지 자체가 아니라 선택된 URI를 전달하고 저장 시 내부 파일로 복사
data class PassDraft(
    val title: String = "",
    val place: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val passNumber: String = "",
    val memo: String = "",
    val imageUri: String? = null
)
