package com.zilehasnain.qazatracker.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * A three-bar chart glyph. material-icons-core has no bar-chart icon (it lives in the much
 * larger extended set), and unlike the 📊 emoji a vector takes the app's tint like the other
 * top-bar icons.
 */
val StatisticsIcon: ImageVector = ImageVector.Builder(
    name = "Statistics",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        // Short bar, tall bar, medium bar.
        moveTo(3f, 12f)
        horizontalLineTo(8f)
        verticalLineTo(21f)
        horizontalLineTo(3f)
        close()

        moveTo(9.5f, 4f)
        horizontalLineTo(14.5f)
        verticalLineTo(21f)
        horizontalLineTo(9.5f)
        close()

        moveTo(16f, 9f)
        horizontalLineTo(21f)
        verticalLineTo(21f)
        horizontalLineTo(16f)
        close()
    }
}.build()
