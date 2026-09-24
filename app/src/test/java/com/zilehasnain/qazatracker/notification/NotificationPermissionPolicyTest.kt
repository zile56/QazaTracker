package com.zilehasnain.qazatracker.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Plain JUnit, no Robolectric/instrumentation — sdkInt is passed in as a plain Int, so these
 * cases exercise API levels below 33 (where POST_NOTIFICATIONS doesn't exist) without ever
 * calling a real permission API, proving the below-33 path can't reach one and crash.
 */
class NotificationPermissionPolicyTest {

    @Test
    fun `never prompts below API 33, regardless of prompted or granted state`() {
        // 26 is this project's min SDK; 32 is one below the POST_NOTIFICATIONS cutoff (33).
        for (sdkInt in listOf(26, 30, 32)) {
            assertFalse(
                "sdkInt=$sdkInt, alreadyPrompted=false, alreadyGranted=false",
                NotificationPermissionPolicy.shouldPrompt(sdkInt, alreadyPrompted = false, alreadyGranted = false)
            )
            assertFalse(
                "sdkInt=$sdkInt, alreadyPrompted=true, alreadyGranted=false",
                NotificationPermissionPolicy.shouldPrompt(sdkInt, alreadyPrompted = true, alreadyGranted = false)
            )
        }
    }

    @Test
    fun `prompts on API 33+ on first launch, when not yet prompted or granted`() {
        assertTrue(NotificationPermissionPolicy.shouldPrompt(33, alreadyPrompted = false, alreadyGranted = false))
        assertTrue(NotificationPermissionPolicy.shouldPrompt(35, alreadyPrompted = false, alreadyGranted = false))
    }

    @Test
    fun `does not prompt again once already prompted, even on API 33+`() {
        assertFalse(NotificationPermissionPolicy.shouldPrompt(33, alreadyPrompted = true, alreadyGranted = false))
    }

    @Test
    fun `does not prompt when permission is already granted`() {
        assertFalse(NotificationPermissionPolicy.shouldPrompt(33, alreadyPrompted = false, alreadyGranted = true))
    }
}
