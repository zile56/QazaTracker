package com.zilehasnain.qazatracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/** Covers the three launch scenarios: first install, after the tutorial, and an existing user. */
class RoutesTest {

    @Test
    fun `first install shows the tutorial before onboarding`() {
        assertEquals(
            Routes.TUTORIAL,
            Routes.startDestinationFor(hasBaseline = false, hasSeenTutorial = false)
        )
    }

    @Test
    fun `once the tutorial has been seen it never appears again, onboarding is next`() {
        assertEquals(
            Routes.ONBOARDING,
            Routes.startDestinationFor(hasBaseline = false, hasSeenTutorial = true)
        )
    }

    @Test
    fun `normal fresh start with a baseline goes straight to the dashboard`() {
        assertEquals(
            Routes.DASHBOARD,
            Routes.startDestinationFor(hasBaseline = true, hasSeenTutorial = true)
        )
    }

    @Test
    fun `an existing user with a baseline skips the tutorial even if the flag was never set`() {
        assertEquals(
            Routes.DASHBOARD,
            Routes.startDestinationFor(hasBaseline = true, hasSeenTutorial = false)
        )
    }
}
