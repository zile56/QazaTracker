package com.example.qazatracker.domain.model

enum class AdjustmentReason {
    MANUAL_CORRECTION,
    // Reserved for the v2 female exemption feature — not exposed in v1 UI,
    // but the value must exist now so applying it later needs no migration.
    EXEMPTION,
    RECALCULATION
}
