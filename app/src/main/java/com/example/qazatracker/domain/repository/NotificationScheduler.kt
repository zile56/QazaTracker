package com.example.qazatracker.domain.repository

import com.example.qazatracker.domain.model.NotificationFrequency

interface NotificationScheduler {
    /** NEVER cancels any pending reminder work; anything else (re)schedules it at that cadence. */
    fun schedule(frequency: NotificationFrequency)
}
