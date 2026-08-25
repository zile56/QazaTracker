package com.example.qazatracker.ui.common

import com.example.qazatracker.domain.model.AdjustmentReason

fun AdjustmentReason.displayName(): String = when (this) {
    AdjustmentReason.MANUAL_CORRECTION -> "Manual correction"
    AdjustmentReason.EXEMPTION -> "Exemption"
    AdjustmentReason.RECALCULATION -> "Recalculation"
}
