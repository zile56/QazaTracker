package com.zilehasnain.qazatracker.ui.common

import com.zilehasnain.qazatracker.domain.model.PrayerType

fun PrayerType.displayName(): String = when (this) {
    PrayerType.FAJR -> "Fajr"
    PrayerType.DHUHR -> "Dhuhr"
    PrayerType.ASR -> "Asr"
    PrayerType.MAGHRIB -> "Maghrib"
    PrayerType.ISHA -> "Isha"
}
