package com.zilehasnain.qazatracker.ui.achievements

import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.common.displayName
import com.zilehasnain.qazatracker.ui.common.formatLongDate
import java.time.LocalDate

/**
 * Emoji are written as escapes so a source file's encoding can never turn them into question
 * marks, and each is named so a card's icon reads as intent rather than a code point.
 */
internal object AchievementEmoji {
    const val PARTY = "🎉" // 🎉
    const val FIRE = "🔥" // 🔥
    const val TROPHY = "🏆" // 🏆
    const val STAR = "⭐" // ⭐
    const val CHART = "📈" // 📈
    const val CHECK = "✓" // ✓
    const val SEEDLING = "🌱" // 🌱
    const val HERB = "🌿" // 🌿
    const val TREE = "🌳" // 🌳

    /** The prayer's time of day: sunrise, sun, sun behind cloud, sunset, moon. */
    fun forPrayer(prayerType: PrayerType): String = when (prayerType) {
        PrayerType.FAJR -> "🌅" // 🌅
        PrayerType.DHUHR -> "☀️" // ☀️
        PrayerType.ASR -> "🌤️" // 🌤️
        PrayerType.MAGHRIB -> "🌇" // 🌇
        PrayerType.ISHA -> "🌙" // 🌙
    }

    /** Grows with the milestone: seedling, herb, tree, trophy. */
    fun forProgress(percentage: Int): String = when {
        percentage >= 100 -> TROPHY
        percentage >= 75 -> TREE
        percentage >= 50 -> HERB
        else -> SEEDLING
    }
}

internal fun milestoneTitle(prayerType: PrayerType): String =
    "All ${prayerType.displayName()} Prayers Completed ${AchievementEmoji.PARTY}"

internal fun currentStreakTitle(days: Int): String = "$days-day streak ${AchievementEmoji.FIRE}"

internal fun bestStreakTitle(days: Int): String = "Best streak: ${daysPhrase(days)} ${AchievementEmoji.STAR}"

internal fun bestStreakDetail(days: Int): String = "Best: ${daysPhrase(days)}"

internal fun progressTitle(percentage: Int): String = "$percentage% complete ${AchievementEmoji.CHECK}"

internal fun unlockedText(date: LocalDate, today: LocalDate): String = "Unlocked on: ${formatLongDate(date, today)}"

internal fun unlockedCountText(count: Int): String = when (count) {
    0 -> "Nothing unlocked yet"
    1 -> "1 achievement unlocked"
    else -> "$count achievements unlocked"
}

private fun daysPhrase(days: Int): String = if (days == 1) "1 day" else "$days days"
