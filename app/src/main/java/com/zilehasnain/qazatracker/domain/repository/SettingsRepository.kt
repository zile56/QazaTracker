package com.zilehasnain.qazatracker.domain.repository

import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Emits WEEKLY (the default) until the user picks something else. */
    fun observeNotificationFrequency(): Flow<NotificationFrequency>

    suspend fun setNotificationFrequency(frequency: NotificationFrequency)

    /** False until the first-time tutorial has been dismissed (by finishing or skipping it). */
    fun observeHasSeenTutorial(): Flow<Boolean>

    suspend fun markTutorialSeen()
}
