package com.example.qazatracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Design import specifies Caprasimo (headings) + Figtree (body) via Google Fonts.
// No font files are bundled yet, so both fall back to the platform default —
// swap these for real FontFamily resources once the .ttf assets are added.
val HeadingFontFamily = FontFamily.Default
val BodyFontFamily = FontFamily.Default

// Base type scale from the design import (styles.css h1..h6, body).
val Typography = Typography(
    headlineLarge = TextStyle( // h1
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 47.sp,
        letterSpacing = (-0.6).sp // ~-0.015em at 42sp
    ),
    headlineMedium = TextStyle( // h2
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle( // h3
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle( // h4
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle( // h5 / card titles
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),
    titleSmall = TextStyle( // h6, uppercase kickers
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 18.sp
    )
)
