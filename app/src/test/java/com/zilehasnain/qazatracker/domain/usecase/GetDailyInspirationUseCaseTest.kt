package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.InspirationCatalog
import com.zilehasnain.qazatracker.ui.inspiration.shareText
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDailyInspirationUseCaseTest {

    private val getDaily = GetDailyInspirationUseCase()
    private val day = LocalDate.of(2026, 9, 25)
    private val arabicLetters = Regex("[\\u0621-\\u064A]")

    // ---- The library ----

    @Test
    fun `the library has at least thirty of each`() {
        assertTrue(InspirationCatalog.verses.size >= 30)
        assertTrue(InspirationCatalog.hadith.size >= 30)
    }

    @Test
    fun `every item is complete and ids are unique`() {
        val all = InspirationCatalog.verses.map { it.id } + InspirationCatalog.hadith.map { it.id }
        assertEquals(all.size, all.toSet().size)
        InspirationCatalog.verses.forEach {
            assertTrue(it.id, arabicLetters.containsMatchIn(it.arabicText))
            assertTrue(it.id, it.englishText.isNotBlank() && it.reflection.isNotBlank())
            assertTrue(it.id, it.surah.isNotBlank() && it.surahNumber in 1..114 && it.ayah >= 1)
        }
        InspirationCatalog.hadith.forEach {
            assertTrue(it.id, it.text.isNotBlank() && it.narrator.isNotBlank() && it.source.isNotBlank())
        }
    }

    @Test
    fun `the source line reads as surah and ayah`() {
        val fatiha = InspirationCatalog.verses.first { it.id == "q1:5" }
        assertEquals("Surah Al-Fatiha, Ayah 5 (1:5)", fatiha.source)
    }

    @Test
    fun `items can be found by id`() {
        assertNotNull(InspirationCatalog.find("q2:153"))
        assertNotNull(InspirationCatalog.find("h01"))
        assertNull(InspirationCatalog.find("nope"))
    }

    // ---- Picking ----

    @Test
    fun `the same day always gives the same pair`() {
        assertEquals(getDaily(day), getDaily(day))
    }

    @Test
    fun `the next day gives a new verse and a new hadith`() {
        val today = getDaily(day)
        val tomorrow = getDaily(day.plusDays(1))

        assertNotEquals(today.verse, tomorrow.verse)
        assertNotEquals(today.hadith, tomorrow.hadith)
    }

    @Test
    fun `nothing repeats until the whole library has been seen`() {
        val verses = (0 until InspirationCatalog.verses.size).map { getDaily(day.plusDays(it.toLong())).verse.id }
        val hadith = (0 until InspirationCatalog.hadith.size).map { getDaily(day.plusDays(it.toLong())).hadith.id }

        assertEquals(InspirationCatalog.verses.size, verses.toSet().size)
        assertEquals(InspirationCatalog.hadith.size, hadith.toSet().size)
    }

    @Test
    fun `next and previous step to the neighbouring days picks`() {
        assertEquals(getDaily(day.plusDays(1)).verse, getDaily(day, offset = 1).verse)
        assertEquals(getDaily(day.minusDays(1)).hadith, getDaily(day, offset = -1).hadith)
    }

    @Test
    fun `a huge or negative offset still lands on a real item`() {
        assertNotNull(getDaily(day, offset = -100_000).verse)
        assertNotNull(getDaily(day, offset = 1_000_000).hadith)
    }

    // ---- Sharing ----

    @Test
    fun `shared text carries the quote, its source and the app name`() {
        val verse = InspirationCatalog.verses.first { it.id == "q2:153" }
        val hadith = InspirationCatalog.hadith.first { it.id == "h01" }

        val verseText = shareText(verse)
        assertTrue(verseText.contains(verse.arabicText) && verseText.contains(verse.englishText))
        assertTrue(verseText.contains("Surah Al-Baqarah, Ayah 153") && verseText.contains("Qaza Tracker"))

        val hadithText = shareText(hadith)
        assertTrue(hadithText.contains(hadith.text) && hadithText.contains("Sahih al-Bukhari"))
        assertTrue(hadithText.contains("Qaza Tracker"))
    }
}
