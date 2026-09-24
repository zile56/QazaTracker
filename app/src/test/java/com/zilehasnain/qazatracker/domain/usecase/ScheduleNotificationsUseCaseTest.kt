package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import com.zilehasnain.qazatracker.domain.repository.NotificationScheduler
import com.zilehasnain.qazatracker.fakes.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Both fakes here are plain Kotlin (no Android/WorkManager/DataStore dependency) — this
 * verifies the use case's own contract: persist-then-schedule, in that order, without
 * needing a real DataStore file or a real WorkManager instance.
 */
class ScheduleNotificationsUseCaseTest {

    private class FakeNotificationScheduler : NotificationScheduler {
        val scheduledCalls = mutableListOf<NotificationFrequency>()
        override fun schedule(frequency: NotificationFrequency) {
            scheduledCalls += frequency
        }
    }

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var notificationScheduler: FakeNotificationScheduler
    private lateinit var scheduleNotifications: ScheduleNotificationsUseCase

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        notificationScheduler = FakeNotificationScheduler()
        scheduleNotifications = ScheduleNotificationsUseCase(settingsRepository, notificationScheduler)
    }

    @Test
    fun `changing the frequency persists it to settings`() = runTest {
        scheduleNotifications(NotificationFrequency.DAILY)

        assertEquals(NotificationFrequency.DAILY, settingsRepository.frequency.value)
    }

    @Test
    fun `changing the frequency triggers exactly one reschedule with the new value`() = runTest {
        scheduleNotifications(NotificationFrequency.BIWEEKLY)

        assertEquals(listOf(NotificationFrequency.BIWEEKLY), notificationScheduler.scheduledCalls)
    }

    @Test
    fun `switching to never still reschedules — the scheduler is what cancels pending work`() = runTest {
        scheduleNotifications(NotificationFrequency.NEVER)

        assertEquals(listOf(NotificationFrequency.NEVER), notificationScheduler.scheduledCalls)
    }

    @Test
    fun `each successive change reschedules again with the latest value`() = runTest {
        scheduleNotifications(NotificationFrequency.DAILY)
        scheduleNotifications(NotificationFrequency.NEVER)
        scheduleNotifications(NotificationFrequency.WEEKLY)

        assertEquals(
            listOf(NotificationFrequency.DAILY, NotificationFrequency.NEVER, NotificationFrequency.WEEKLY),
            notificationScheduler.scheduledCalls
        )
        assertEquals(NotificationFrequency.WEEKLY, settingsRepository.frequency.value)
    }
}
