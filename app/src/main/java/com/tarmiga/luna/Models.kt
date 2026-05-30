package com.tarmiga.luna

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class PhaseType {
    MENSTRUAL, FOLLICULAR, OVULATORY, LUTEAL;

    fun toDisplayString(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class NotificationType {
    PHASE_WARNING, PHASE_START, PERIOD_LATE, DAILY_REMINDER
}

data class CycleState(
    val cycleStarts: List<String>,
    val avgCycleLength: Int = 28
) {
    fun getLatestStart(): LocalDate? {
        return cycleStarts.mapNotNull { 
            try {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: DateTimeParseException) {
                null
            }
        }.maxOrNull()
    }
}
