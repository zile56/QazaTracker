package com.example.qazatracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val REMINDERS_CHANNEL_ID = "qaza_reminders"

    /** IMPORTANCE_DEFAULT: shows in the shade with a sound, never heads-up — a gentle nudge. */
    fun ensureRemindersChannel(context: Context) {
        val channel = NotificationChannel(
            REMINDERS_CHANNEL_ID,
            "Prayer reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle nudges to log your Qaza prayers"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
