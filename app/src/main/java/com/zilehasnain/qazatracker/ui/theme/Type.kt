package com.zilehasnain.qazatracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zilehasnain.qazatracker.R

// Design import specifies Caprasimo (headings) + Figtree (body) via Google Fonts.
// Caprasimo only ships one static weight (400/Regular). Figtree ships only as a
// variable font, so its 400/700 weights are pulled from res/font/figtree.xml,
// a font-family resource that references the single variable TTF twice.
val HeadingFontFamily = FontFamily(Font(R.font.caprasimo_regular, FontWeight.Normal))
val BodyFontFamily = FontFamily(
    Font(R.font.figtree, FontWeight.Normal),
    Font(R.font.figtree, FontWeight.Bold)
)

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
