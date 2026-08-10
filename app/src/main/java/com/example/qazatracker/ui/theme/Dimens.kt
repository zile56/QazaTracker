package com.example.qazatracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing scale from the "Organic" design import (styles.css --space-*). */
object QazaSpacing {
    val space1 = 4.4.dp
    val space2 = 8.8.dp
    val space3 = 13.2.dp
    val space4 = 17.6.dp
    val space6 = 26.4.dp
    val space8 = 35.2.dp
}

/**
 * Corner radii from the design import, including its own override rule that bumps
 * cards/dialogs to `--radius-lg * 1.15` and makes buttons/inputs fully pill-shaped.
 */
object QazaShapes {
    val cardRadius = 32.dp
    val cardShape = RoundedCornerShape(cardRadius)
    val pillShape = RoundedCornerShape(50)
}
