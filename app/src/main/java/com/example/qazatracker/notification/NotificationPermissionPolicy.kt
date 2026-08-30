package com.example.qazatracker.notification

import android.os.Build

/**
 * Pure decision logic for whether to show the POST_NOTIFICATIONS system prompt — kept
 * separate from MainActivity so it's testable without Robolectric/instrumentation, and so
 * the "don't touch this API below 33" guard is provably a plain int comparison rather than
 * something that could reach a permission call on an SDK level where it doesn't exist.
 */
object NotificationPermissionPolicy {
    fun shouldPrompt(sdkInt: Int, alreadyPrompted: Boolean, alreadyGranted: Boolean): Boolean =
        sdkInt >= Build.VERSION_CODES.TIRAMISU && !alreadyPrompted && !alreadyGranted
}
