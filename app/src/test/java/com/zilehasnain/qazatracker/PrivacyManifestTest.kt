package com.zilehasnain.qazatracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The app promises to be offline and private, so the manifest must not ask for the internet or location. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PrivacyManifestTest {

    @Test
    fun `the app requests neither the internet nor location`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val requested = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toList()

        assertFalse(requested.contains(Manifest.permission.INTERNET))
        assertFalse(requested.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse(requested.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}
