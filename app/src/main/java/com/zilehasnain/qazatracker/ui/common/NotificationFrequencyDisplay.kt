package com.zilehasnain.qazatracker.ui.common

import com.zilehasnain.qazatracker.domain.model.NotificationFrequency

fun NotificationFrequency.displayName(): String = when (this) {
    NotificationFrequency.NEVER -> "Never"
    NotificationFrequency.WEEKLY -> "Weekly"
    NotificationFrequency.BIWEEKLY -> "Bi-weekly"
    NotificationFrequency.DAILY -> "Daily"
}
