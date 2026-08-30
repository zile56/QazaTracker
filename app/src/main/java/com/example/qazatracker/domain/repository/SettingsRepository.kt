package com.example.qazatracker.domain.repository

import com.example.qazatracker.domain.model.NotificationFrequency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Emits WEEKLY (the default) until the user picks something else. */
    fun observeNotificationFrequency(): Flow<NotificationFrequency>

    suspend fun setNotificationFrequency(frequency: NotificationFrequency)
}
