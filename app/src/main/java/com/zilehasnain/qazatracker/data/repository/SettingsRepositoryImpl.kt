package com.zilehasnain.qazatracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val NOTIFICATION_FREQUENCY_KEY = stringPreferencesKey("notification_frequency")
private val HAS_SEEN_TUTORIAL_KEY = booleanPreferencesKey("has_seen_tutorial")

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observeNotificationFrequency(): Flow<NotificationFrequency> =
        dataStore.data.map { prefs ->
            prefs[NOTIFICATION_FREQUENCY_KEY]
                ?.let { runCatching { NotificationFrequency.valueOf(it) }.getOrNull() }
                ?: NotificationFrequency.WEEKLY
        }

    override suspend fun setNotificationFrequency(frequency: NotificationFrequency) {
        dataStore.edit { prefs -> prefs[NOTIFICATION_FREQUENCY_KEY] = frequency.name }
    }

    override fun observeHasSeenTutorial(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[HAS_SEEN_TUTORIAL_KEY] ?: false }

    override suspend fun markTutorialSeen() {
        dataStore.edit { prefs -> prefs[HAS_SEEN_TUTORIAL_KEY] = true }
    }
}
