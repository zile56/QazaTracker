package com.zilehasnain.qazatracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zilehasnain.qazatracker.domain.repository.NotificationScheduler
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import com.zilehasnain.qazatracker.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class QazaTrackerApp : Application(), Configuration.Provider {

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationScheduler: NotificationScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureRemindersChannel(this)

        // Enforces the stored preference (WEEKLY by default) as scheduled work on every cold
        // start — otherwise a fresh install's default would sit in DataStore without ever
        // actually being scheduled until the user happens to open Settings.
        applicationScope.launch {
            val frequency = settingsRepository.observeNotificationFrequency().first()
            notificationScheduler.schedule(frequency)
        }
    }
}
