package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.DailyInspiration
import com.zilehasnain.qazatracker.domain.model.Hadith
import com.zilehasnain.qazatracker.domain.model.InspirationCatalog
import com.zilehasnain.qazatracker.domain.model.QuranVerse
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

/**
 * Picks the day's verse and hadith. Nothing is stored: the library is put in a fixed shuffled
 * order and the day number walks through it, so the same date always gives the same pair (it
 * stays put all day and after a restart), tomorrow gives the next, and nothing repeats until the
 * whole library has been seen.
 *
 * [offset] steps away from the day's pair for Previous, Next and Refresh.
 */
class GetDailyInspirationUseCase @Inject constructor() {

    operator fun invoke(date: LocalDate, offset: Int = 0): DailyInspiration = pick(date, offset)

    companion object {
        private val verseOrder: List<QuranVerse> = InspirationCatalog.verses.shuffled(Random(VERSE_SEED))
        private val hadithOrder: List<Hadith> = InspirationCatalog.hadith.shuffled(Random(HADITH_SEED))

        fun pick(
            date: LocalDate,
            offset: Int = 0,
            verses: List<QuranVerse> = verseOrder,
            hadith: List<Hadith> = hadithOrder
        ): DailyInspiration {
            val day = date.toEpochDay() + offset
            return DailyInspiration(
                verse = verses[Math.floorMod(day, verses.size.toLong()).toInt()],
                hadith = hadith[Math.floorMod(day, hadith.size.toLong()).toInt()],
                date = date
            )
        }

        private const val VERSE_SEED = 1L
        private const val HADITH_SEED = 2L
    }
}
