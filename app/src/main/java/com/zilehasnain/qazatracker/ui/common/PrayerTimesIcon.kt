package com.zilehasnain.qazatracker.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** A small mosque: a central dome with a finial, a minaret each side, and a wide base. */
val PrayerTimesIcon: ImageVector = ImageVector.Builder(
    name = "PrayerTimes",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        // Dome: a half circle centred at x=12 sitting on y=14.
        moveTo(7.5f, 14f)
        arcTo(4.5f, 4.5f, 0f, false, true, 16.5f, 14f)
        close()

        // Finial.
        moveTo(11.4f, 4.5f)
        horizontalLineTo(12.6f)
        verticalLineTo(7.6f)
        horizontalLineTo(11.4f)
        close()

        // Minarets with pointed tops.
        moveTo(2.5f, 8f)
        lineTo(4.25f, 5f)
        lineTo(6f, 8f)
        verticalLineTo(21f)
        horizontalLineTo(2.5f)
        close()
        moveTo(18f, 8f)
        lineTo(19.75f, 5f)
        lineTo(21.5f, 8f)
        verticalLineTo(21f)
        horizontalLineTo(18f)
        close()

        // Prayer hall body.
        moveTo(7f, 14.5f)
        horizontalLineTo(17f)
        verticalLineTo(21f)
        horizontalLineTo(7f)
        close()
    }
}.build()
