package com.zilehasnain.qazatracker.domain.model

sealed interface CompletionProjection {
    data object AlreadyCaughtUp : CompletionProjection
    data object InsufficientData : CompletionProjection
    data class Estimated(val estimate: CompletionEstimate) : CompletionProjection
}
