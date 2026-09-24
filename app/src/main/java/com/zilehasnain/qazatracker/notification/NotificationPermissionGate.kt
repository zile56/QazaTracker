package com.zilehasnain.qazatracker.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val HAS_PROMPTED_KEY = booleanPreferencesKey("notification_permission_prompted")

/**
 * Tracks whether the POST_NOTIFICATIONS system prompt has ever been shown, so MainActivity
 * only asks once per install. The OS gives no reliable "permanently denied" signal before the
 * very first ask (`shouldShowRequestPermissionRationale` can't distinguish "never asked" from
 * "denied forever" until after one denial) — asking once, ever, sidesteps that entirely.
 */
class NotificationPermissionGate @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun observeHasPrompted(): Flow<Boolean> = dataStore.data.map { it[HAS_PROMPTED_KEY] ?: false }

    suspend fun markPrompted() {
        dataStore.edit { it[HAS_PROMPTED_KEY] = true }
    }
}
