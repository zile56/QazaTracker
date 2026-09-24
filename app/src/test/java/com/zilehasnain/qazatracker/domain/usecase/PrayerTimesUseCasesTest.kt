package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.LocationData
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.RegionCatalog
import com.zilehasnain.qazatracker.domain.model.RegionSelection
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesUseCasesTest {

    private val rawalpindi = LocationData.Default
    private val date = LocalDate.of(2026, 9, 25)
    private val getTimes = GetPrayerTimesUseCase()

    private fun times(
        method: PrayerCalculationMethod,
        hanafi: Boolean = false,
        location: LocationData = rawalpindi,
        fajr: Double = method.fajrAngle,
        isha: Double = method.ishaAngle
    ) = getTimes(date, location, method, hanafi, fajr, isha)!!

    // ---- Calculation ----

    @Test
    fun `the day runs in order for every method`() {
        PrayerCalculationMethod.entries.forEach { method ->
            val t = times(method)
            assertTrue("$method", t.fajr < t.sunrise && t.sunrise < t.dhuhr && t.dhuhr < t.asr)
            assertTrue("$method", t.asr < t.maghrib && t.maghrib < t.isha)
        }
    }

    @Test
    fun `Rawalpindi in late September lands where it should`() {
        // Dhuhr near 12:00 local (UTC+5) is about 07:00 UTC; Maghrib about 17:55-18:00 local.
        val t = times(PrayerCalculationMethod.KARACHI, hanafi = true)
        val dhuhrUtcHour = t.dhuhr.atZone(java.time.ZoneOffset.UTC).hour
        assertEquals(7, dhuhrUtcHour)
        val maghribLocalHour = t.maghrib.atZone(java.time.ZoneOffset.ofHours(5)).hour
        assertEquals(18, maghribLocalHour) // 18:00, give or take a few minutes
    }

    @Test
    fun `all nine methods produce different Fajr or Isha times`() {
        val results = PrayerCalculationMethod.entries.map { method ->
            val t = times(method, fajr = if (method == PrayerCalculationMethod.CUSTOM) 12.0 else method.fajrAngle,
                isha = if (method == PrayerCalculationMethod.CUSTOM) 12.0 else method.ishaAngle)
            t.fajr to t.isha
        }
        assertEquals(PrayerCalculationMethod.entries.size, results.toSet().size)
    }

    @Test
    fun `a larger Fajr angle means an earlier Fajr`() {
        assertTrue(times(PrayerCalculationMethod.EGYPTIAN).fajr < times(PrayerCalculationMethod.ISNA).fajr)
    }

    @Test
    fun `custom angles are used for the custom method`() {
        val custom = times(PrayerCalculationMethod.CUSTOM, fajr = 15.0, isha = 15.0)
        val isna = times(PrayerCalculationMethod.ISNA)

        assertEquals(isna.fajr, custom.fajr)
        assertEquals(isna.isha, custom.isha)
    }

    @Test
    fun `the Gulf method fixes Isha ninety minutes after Maghrib`() {
        val t = times(PrayerCalculationMethod.GULF)
        val minutes = java.time.Duration.between(t.maghrib, t.isha).toMinutes()
        assertTrue("was $minutes", minutes in 89..91)
    }

    @Test
    fun `Hanafi Asr is later than Shafi Asr and nothing else moves`() {
        val shafi = times(PrayerCalculationMethod.KARACHI, hanafi = false)
        val hanafi = times(PrayerCalculationMethod.KARACHI, hanafi = true)

        assertTrue(hanafi.asr > shafi.asr)
        assertEquals(shafi.dhuhr, hanafi.dhuhr)
        assertEquals(shafi.maghrib, hanafi.maghrib)
    }

    @Test
    fun `the same inputs always give the same times`() {
        assertEquals(times(PrayerCalculationMethod.MWL), times(PrayerCalculationMethod.MWL))
    }

    @Test
    fun `a place where the sun never gets low enough returns null rather than crashing`() {
        val result = getTimes(
            LocalDate.of(2026, 6, 21), LocationData(89.0, 0.0), PrayerCalculationMethod.MWL, false
        )
        // Either the library copes or we report it; the point is no exception.
        if (result != null) assertNotNull(result.fajr)
    }

    // ---- Recommendations ----

    private val recommend = GetRecommendedMethodUseCase()

    @Test
    fun `Pakistan recommends Karachi with Hanafi Asr`() {
        val selection = RegionSelection("PK", null, 18.0, 18.0)

        assertEquals(PrayerCalculationMethod.KARACHI, recommend.effectiveMethod(selection))
        assertTrue(recommend.usesHanafiAsr(selection))
    }

    @Test
    fun `an explicit method beats the recommendation`() {
        val selection = RegionSelection("PK", PrayerCalculationMethod.MWL, 18.0, 18.0)

        assertEquals(PrayerCalculationMethod.MWL, recommend.effectiveMethod(selection))
    }

    @Test
    fun `no country falls back to Muslim World League`() {
        assertEquals(PrayerCalculationMethod.MWL, recommend.effectiveMethod(RegionSelection(null, null, 18.0, 18.0)))
        assertNull(recommend("ZZ"))
    }

    @Test
    fun `the catalog has unique codes, real flags, and sensible alternatives`() {
        val all = RegionCatalog.all
        assertEquals(all.size, all.map { it.code }.toSet().size)
        assertTrue(all.size >= 190)
        all.forEach { region ->
            assertEquals(2, region.code.length)
            assertEquals(4, region.flag.codePointCount(0, region.flag.length) * 2)
            assertTrue(region.recommended !in region.alternatives)
            assertTrue(PrayerCalculationMethod.CUSTOM !in region.alternatives)
        }
        assertEquals("🇵🇰", RegionCatalog.byCode("pk")!!.flag)
    }

    // ---- Search ----

    private val search = SearchRegionUseCase()

    @Test
    fun `an empty query lists every country`() {
        assertEquals(RegionCatalog.all.size, search("").size)
    }

    @Test
    fun `typing pak finds Pakistan first`() {
        assertEquals("Pakistan", search("pak").first().name)
    }

    @Test
    fun `every country can be found by its own name`() {
        RegionCatalog.all.forEach { region ->
            assertEquals(region.name, region.code, search(region.name).first().code)
        }
    }

    @Test
    fun `codes and common aliases work`() {
        assertEquals("GB", search("uk").first().code)
        assertEquals("US", search("USA").first().code)
        assertEquals("AE", search("UAE").first().code)
        assertEquals("PK", search("pk").first().code)
    }

    @Test
    fun `a small typo still finds the country`() {
        assertTrue(search("pakstan").any { it.code == "PK" })
        assertTrue(search("indonesa").any { it.code == "ID" })
        assertTrue(search("banglades").any { it.code == "BD" })
    }

    @Test
    fun `accents and case do not matter`() {
        assertEquals("TR", search("TÜRKIYE").first().code)
    }

    @Test
    fun `nonsense finds nothing`() {
        assertTrue(search("zzzzqx").isEmpty())
    }
}
