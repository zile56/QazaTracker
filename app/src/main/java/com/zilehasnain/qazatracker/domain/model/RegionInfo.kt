package com.zilehasnain.qazatracker.domain.model

/**
 * A country and the calculation method its authorities or majority commonly use. These are
 * suggestions, not rulings: practice varies inside a country, so the UI always offers the
 * alternatives and points people to their local mosque.
 */
data class RegionInfo(
    val code: String,
    val name: String,
    val recommended: PrayerCalculationMethod,
    val alternatives: List<PrayerCalculationMethod> = emptyList(),
    /** Asr shadow length: the Hanafi school (2x) is standard across South Asia and Turkey. */
    val hanafiAsr: Boolean = false,
    val note: String? = null
) {
    /** The flag emoji, built from the ISO code's regional-indicator letters. */
    val flag: String
        get() = code.uppercase().map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
}
