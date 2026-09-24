package com.zilehasnain.qazatracker.domain.model

import java.time.LocalDate

sealed interface CompletionProjection {
    data object AlreadyCaughtUp : CompletionProjection
    data object InsufficientData : CompletionProjection
    data class Estimated(val projectedDate: LocalDate, val daysRemaining: Long) : CompletionProjection
}
