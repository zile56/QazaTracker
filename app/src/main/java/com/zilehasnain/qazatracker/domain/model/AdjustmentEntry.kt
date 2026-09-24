package com.zilehasnain.qazatracker.domain.model

import java.time.Instant

data class AdjustmentEntry(
    val prayerType: PrayerType,
    val delta: Int,
    val reason: AdjustmentReason,
    val note: String?,
    val timestamp: Instant
)
