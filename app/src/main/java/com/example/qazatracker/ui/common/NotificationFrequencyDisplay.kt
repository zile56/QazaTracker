package com.example.qazatracker.ui.common

import com.example.qazatracker.domain.model.NotificationFrequency

fun NotificationFrequency.displayName(): String = when (this) {
    NotificationFrequency.NEVER -> "Never"
    NotificationFrequency.WEEKLY -> "Weekly"
    NotificationFrequency.BIWEEKLY -> "Bi-weekly"
    NotificationFrequency.DAILY -> "Daily"
}
