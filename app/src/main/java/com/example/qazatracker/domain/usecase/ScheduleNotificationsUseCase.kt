package com.example.qazatracker.domain.usecase

import com.example.qazatracker.domain.model.NotificationFrequency
import com.example.qazatracker.domain.repository.NotificationScheduler
import com.example.qazatracker.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Persists a chosen notification frequency and (re)schedules the reminder work to match it
 * in one step, so the two can never drift apart — e.g. a preference saved as DAILY while the
 * old WEEKLY worker is still running.
 */
class ScheduleNotificationsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(frequency: NotificationFrequency) {
        settingsRepository.setNotificationFrequency(frequency)
        notificationScheduler.schedule(frequency)
    }
}
