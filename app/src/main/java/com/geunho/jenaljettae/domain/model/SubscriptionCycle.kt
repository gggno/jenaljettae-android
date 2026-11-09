package com.geunho.jenaljettae.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// MARK: - Subscription Cycle | iOS의 SubscriptionCycle과 같은 5가지 반복 주기
enum class SubscriptionCycle(val rawValue: String, val title: String, val intervalMonths: Long) {
    MONTHLY("monthly", "매월", 1),
    EVERY_TWO_MONTHS("everyTwoMonths", "2개월", 2),
    EVERY_THREE_MONTHS("everyThreeMonths", "3개월", 3),
    EVERY_SIX_MONTHS("everySixMonths", "6개월", 6),
    YEARLY("yearly", "매년", 12);

    companion object {
        fun fromRawValue(value: String?): SubscriptionCycle? = entries.firstOrNull { it.rawValue == value }
    }

    // MARK: - Billing Date | 시작일의 일자를 유지하며 짧은 달은 말일로 보정
    fun billingDate(startDate: LocalDate, targetMonth: YearMonth): LocalDate? {
        val monthOffset = ChronoUnit.MONTHS.between(YearMonth.from(startDate), targetMonth)
        if (monthOffset < 0 || monthOffset % intervalMonths != 0L) return null
        return targetMonth.atDay(minOf(startDate.dayOfMonth, targetMonth.lengthOfMonth()))
    }

    // MARK: - Upcoming Dates | 시작일 포함 여부를 선택해 오늘 이후 일정 조회
    fun upcomingBillingDates(
        startDate: LocalDate,
        count: Int = 3,
        referenceDate: LocalDate = LocalDate.now(),
        includeStartDate: Boolean = false
    ): List<LocalDate> {
        if (count <= 0) return emptyList()

        val anchorMonth = YearMonth.from(startDate)
        val referenceOffset = ChronoUnit.MONTHS.between(anchorMonth, YearMonth.from(referenceDate))
        var occurrenceIndex = maxOf(if (includeStartDate) 0L else 1L, referenceOffset / intervalMonths)
        val dates = mutableListOf<LocalDate>()

        while (dates.size < count) {
            val targetMonth = anchorMonth.plusMonths(occurrenceIndex * intervalMonths)
            val billingDate = billingDate(startDate, targetMonth) ?: break
            if (!billingDate.isBefore(referenceDate)) dates.add(billingDate)
            occurrenceIndex++
        }
        return dates
    }
}
