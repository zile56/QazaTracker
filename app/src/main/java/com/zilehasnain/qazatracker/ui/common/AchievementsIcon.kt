package com.zilehasnain.qazatracker.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * A simple trophy glyph: cup, two handles, stem and base. The core icon set has none, and unlike
 * the 🏆 emoji a vector takes the same tint as the other top-bar icons.
 */
val AchievementsIcon: ImageVector = ImageVector.Builder(
    name = "Achievements",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Black)) {
        // Cup: flat rim, curved bowl.
        moveTo(7f, 3f)
        horizontalLineTo(17f)
        verticalLineTo(9f)
        curveTo(17f, 11.8f, 14.8f, 14f, 12f, 14f)
        curveTo(9.2f, 14f, 7f, 11.8f, 7f, 9f)
        close()

        // Handles.
        moveTo(3.5f, 5f)
        horizontalLineTo(7f)
        verticalLineTo(7.2f)
        horizontalLineTo(3.5f)
        close()
        moveTo(17f, 5f)
        horizontalLineTo(20.5f)
        verticalLineTo(7.2f)
        horizontalLineTo(17f)
        close()

        // Stem and base.
        moveTo(11f, 14f)
        horizontalLineTo(13f)
        verticalLineTo(18f)
        horizontalLineTo(11f)
        close()
        moveTo(8f, 18f)
        horizontalLineTo(16f)
        verticalLineTo(21f)
        horizontalLineTo(8f)
        close()
    }
}.build()
