package com.zilehasnain.qazatracker.domain.model

import java.time.Duration

/** Order here drives the segmented control's display order in Settings. */
enum class NotificationFrequency {
    NEVER,
    WEEKLY,
    BIWEEKLY,
    DAILY
}

/**
 * Maps a frequency to the interval a PeriodicWorkRequest should repeat at, or null for
 * NEVER (the signal to cancel any pending reminder work instead of scheduling it).
 */
fun NotificationFrequency.repeatInterval(): Duration? = when (this) {
    NotificationFrequency.NEVER -> null
    NotificationFrequency.DAILY -> Duration.ofDays(1)
    NotificationFrequency.WEEKLY -> Duration.ofDays(7)
    NotificationFrequency.BIWEEKLY -> Duration.ofDays(14)
}
