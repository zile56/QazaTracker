package com.example.qazatracker.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qazatracker.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val REMINDER_NOTIFICATION_ID = 1001

/**
 * Fires a single gentle reminder notification. Scheduling cadence (or whether this ever
 * runs at all) is entirely decided by [com.example.qazatracker.notification.WorkManagerNotificationScheduler]
 * based on the user's Settings preference — this worker just shows the notification.
 */
@HiltWorker
class QazaNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        showReminderNotification(applicationContext)
        return Result.success()
    }
}

private fun showReminderNotification(context: Context) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return

    val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
        PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }

    val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Time to catch up on your Qaza prayers?")
        .setContentText("Open the app to log today's prayers.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .apply { contentIntent?.let(::setContentIntent) }
        .build()

    NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
}
