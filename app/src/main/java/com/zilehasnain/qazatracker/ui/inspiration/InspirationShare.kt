package com.zilehasnain.qazatracker.ui.inspiration

import com.zilehasnain.qazatracker.domain.model.Hadith
import com.zilehasnain.qazatracker.domain.model.InspirationItem
import com.zilehasnain.qazatracker.domain.model.QuranVerse

private const val WATERMARK = "— Shared from Qaza Tracker"

/** The plain text used for both Share and Copy: the quote, where it is from, and the app's name. */
fun shareText(verse: QuranVerse): String =
    "${verse.arabicText}\n\n\"${verse.englishText}\"\n\nQuran — ${verse.source}\n$WATERMARK"

fun shareText(hadith: Hadith): String =
    "\"${hadith.text}\"\n\n${hadith.attribution}\n$WATERMARK"

fun shareText(item: InspirationItem): String = when (item) {
    is InspirationItem.Verse -> shareText(item.verse)
    is InspirationItem.Saying -> shareText(item.hadith)
}
