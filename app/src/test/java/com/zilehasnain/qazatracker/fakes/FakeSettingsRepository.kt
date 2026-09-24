package com.zilehasnain.qazatracker.fakes

import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory stand-in for the DataStore-backed repository — plain Kotlin, no Android needed. */
class FakeSettingsRepository : SettingsRepository {
    val frequency = MutableStateFlow(NotificationFrequency.WEEKLY)
    val hasSeenTutorial = MutableStateFlow(false)

    override fun observeNotificationFrequency(): Flow<NotificationFrequency> = frequency

    override suspend fun setNotificationFrequency(frequency: NotificationFrequency) {
        this.frequency.value = frequency
    }

    override fun observeHasSeenTutorial(): Flow<Boolean> = hasSeenTutorial

    override suspend fun markTutorialSeen() {
        hasSeenTutorial.value = true
    }
}
