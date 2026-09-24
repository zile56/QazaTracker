package com.zilehasnain.qazatracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dynamic color is deliberately not offered: the design import defines a specific
// brand palette (cream/terracotta/sage), not a Material "you" theme.

private val QazaLightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Cream,
    primaryContainer = Terracotta100,
    onPrimaryContainer = Terracotta800,
    secondary = Sage,
    onSecondary = Cream,
    secondaryContainer = Sage100,
    onSecondaryContainer = Sage800,
    background = Cream,
    onBackground = InkText,
    surface = Surface,
    onSurface = InkText,
    surfaceVariant = Neutral200,
    onSurfaceVariant = Neutral700,
    outline = Neutral300,
    outlineVariant = Divider
)

// No dark theme was defined in the design import; this is a same-hue variant
// derived for parity rather than part of the original spec.
private val QazaDarkColors = darkColorScheme(
    primary = Terracotta500,
    onPrimary = Neutral900,
    primaryContainer = Terracotta900,
    onPrimaryContainer = Terracotta100,
    secondary = Sage500,
    onSecondary = Neutral900,
    secondaryContainer = Sage900,
    onSecondaryContainer = Sage100,
    background = Neutral900,
    onBackground = Neutral100,
    surface = Neutral800,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral700,
    outlineVariant = Neutral700
)

@Composable
fun QazaTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) QazaDarkColors else QazaLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
