package com.zilehasnain.qazatracker.ui.theme

import androidx.compose.ui.graphics.Color

// "Organic" palette from the Claude Design import (Qaza Tracker.dc.html /
// _ds_bundle styles.css) — warm cream + terracotta + sage, no alert red.
// Only a light theme was defined in the source; QazaDarkColors below is a
// same-hue dark variant derived for parity, not part of the original spec.

val Cream = Color(0xFFF5EAD8)
val Surface = Color(0xFFEBDDC5)
val InkText = Color(0xFF201E1D)

val Neutral100 = Color(0xFFF9F4ED)
val Neutral200 = Color(0xFFEEE7DB)
val Neutral300 = Color(0xFFDCD3C4)
val Neutral600 = Color(0xFF82796A)
val Neutral700 = Color(0xFF645C50)
val Neutral800 = Color(0xFF474238)
val Neutral900 = Color(0xFF2E2B25)

val Terracotta100 = Color(0xFFFFF2EB)
val Terracotta500 = Color(0xFFD67F48)
val Terracotta600 = Color(0xFFB2622D)
val Terracotta700 = Color(0xFF8C491A)
val Terracotta800 = Color(0xFF643312)
val Terracotta900 = Color(0xFF402310)
val Terracotta = Color(0xFFC67139)

val Sage100 = Color(0xFFF0FAE1)
val Sage500 = Color(0xFF8FA073)
val Sage700 = Color(0xFF56633F)
val Sage800 = Color(0xFF3D472B)
val Sage900 = Color(0xFF272E1B)
val Sage = Color(0xFF7A8A5E)

val Divider = Color(0x29201E1D) // ink at ~16% alpha

// Colour coding for the estimated completion date (near = green, medium = yellow, far = orange),
// tuned warm to sit with the rest of the palette. Each has a container/content pair for text on a
// tinted pill, plus an accent for the timeline bar; the dark set keeps the same hues readable
// on the dark surfaces.
val PaceGreenContainer = Color(0xFFD3E4B3)
val PaceGreenContent = Color(0xFF2F3B1B)
val PaceGreenAccent = Color(0xFF7A8A5E)
val PaceYellowContainer = Color(0xFFF3DE94)
val PaceYellowContent = Color(0xFF4D3C05)
val PaceYellowAccent = Color(0xFFC9A23A)
val PaceOrangeContainer = Color(0xFFF2C49F)
val PaceOrangeContent = Color(0xFF5A2B0A)
val PaceOrangeAccent = Color(0xFFC67139)

val PaceGreenContainerDark = Color(0xFF3F4B2A)
val PaceGreenContentDark = Color(0xFFD9EBB8)
val PaceGreenAccentDark = Color(0xFF8FA073)
val PaceYellowContainerDark = Color(0xFF5A4A12)
val PaceYellowContentDark = Color(0xFFF6E6A6)
val PaceYellowAccentDark = Color(0xFFD9B44A)
val PaceOrangeContainerDark = Color(0xFF6B3A1B)
val PaceOrangeContentDark = Color(0xFFF7D3B8)
val PaceOrangeAccentDark = Color(0xFFD67F48)
