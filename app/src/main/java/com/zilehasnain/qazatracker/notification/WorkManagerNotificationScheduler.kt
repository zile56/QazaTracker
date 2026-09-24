package com.zilehasnain.qazatracker.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zilehasnain.qazatracker.domain.model.NotificationFrequency
import com.zilehasnain.qazatracker.domain.model.repeatInterval
import com.zilehasnain.qazatracker.domain.repository.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val UNIQUE_WORK_NAME = "qaza_reminder_work"

class WorkManagerNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    override fun schedule(frequency: NotificationFrequency) {
        val workManager = WorkManager.getInstance(context)
        val interval = frequency.repeatInterval()

        if (interval == null) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val request = PeriodicWorkRequestBuilder<QazaNotificationWorker>(interval).build()
        // UPDATE: replaces the currently-enqueued periodic work with the new interval instead
        // of leaving the old cadence running alongside a second one.
        workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
