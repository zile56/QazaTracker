package com.zilehasnain.qazatracker.ui.widget

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The merged manifest as the system will see it: registered, findable by the widget host, locked down. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = Application::class)
class WidgetManifestTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val component = ComponentName(context, QazaTrackerWidget::class.java)

    @Test
    fun `the widget receiver is registered`() {
        val info = context.packageManager.getReceiverInfo(component, PackageManager.GET_META_DATA)

        assertEquals(QazaTrackerWidget::class.java.name, info.name)
    }

    @Test
    fun `the receiver is not exported, so other apps cannot send it the log action`() {
        val info = context.packageManager.getReceiverInfo(component, 0)

        assertFalse(info.exported)
    }

    @Test
    fun `it declares the appwidget provider metadata pointing at the info file`() {
        val info = context.packageManager.getReceiverInfo(component, PackageManager.GET_META_DATA)

        assertEquals(R.xml.qaza_tracker_widget_info, info.metaData.getInt("android.appwidget.provider"))
    }

    @Test
    fun `it listens for the widget update broadcast the host sends`() {
        val receivers = context.packageManager.queryBroadcastReceivers(
            Intent("android.appwidget.action.APPWIDGET_UPDATE").setPackage(context.packageName),
            0
        )

        assertTrue(receivers.any { it.activityInfo.name == QazaTrackerWidget::class.java.name })
    }

    @Test
    fun `the provider info is a 3x3, home-screen, event-driven widget using the widget layout`() {
        val parser = context.resources.getXml(R.xml.qaza_tracker_widget_info)
        while (parser.next() != android.content.res.XmlResourceParser.START_TAG) { /* skip to root */ }
        val ns = "http://schemas.android.com/apk/res/android"

        assertEquals("appwidget-provider", parser.name)
        assertEquals(R.layout.widget_qaza_tracker, parser.getAttributeResourceValue(ns, "initialLayout", 0))
        assertEquals("0", parser.getAttributeValue(ns, "updatePeriodMillis"))
        assertNotNull(parser.getAttributeValue(ns, "minWidth"))
        assertNotNull(parser.getAttributeValue(ns, "minHeight"))
        assertEquals("3", parser.getAttributeValue(ns, "targetCellWidth"))
        assertEquals("3", parser.getAttributeValue(ns, "targetCellHeight"))
    }
}
