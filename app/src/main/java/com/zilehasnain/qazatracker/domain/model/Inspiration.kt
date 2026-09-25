package com.zilehasnain.qazatracker.domain.model

import java.time.LocalDate

/**
 * A verse of the Quran. [englishText] is the English rendering (the one text field for it:
 * a second "translation" copy would just duplicate it), and [reflection] is a one-line note on
 * why it matters, not a tafsir.
 */
data class QuranVerse(
    val id: String,
    val surah: String,
    val surahNumber: Int,
    val ayah: Int,
    val arabicText: String,
    val englishText: String,
    val reflection: String
) {
    val source: String get() = "Surah $surah, Ayah $ayah ($surahNumber:$ayah)"
}

/** A saying of the Prophet ﷺ, with who reported it and the collection it comes from. */
data class Hadith(
    val id: String,
    val narrator: String,
    val text: String,
    val source: String
) {
    val attribution: String get() = "Narrated by $narrator · $source"
}

data class DailyInspiration(
    val verse: QuranVerse,
    val hadith: Hadith,
    val date: LocalDate
)

/** Either kind of saved item, so the Saved tab can list them together. */
sealed interface InspirationItem {
    val id: String

    data class Verse(val verse: QuranVerse) : InspirationItem {
        override val id: String get() = verse.id
    }

    data class Saying(val hadith: Hadith) : InspirationItem {
        override val id: String get() = hadith.id
    }
}
