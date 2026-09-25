package com.zilehasnain.qazatracker.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zilehasnain.qazatracker.data.local.entity.MilestoneEntity
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Builds a genuine version-1 database file (the exact v1 schema from schemas/.../1.json, filled
 * like a tester's phone would be), then opens it through Room's real builder with MIGRATION_1_2.
 * That is the production upgrade path, and Room itself validates the migrated tables against the
 * current entities, so a wrong migration fails here. Guards the one thing that would be
 * unforgivable: wiping a user's prayer history on update.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"

    @Before
    fun createVersion1Database() {
        val file = context.getDatabasePath(dbName)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `BaselineSnapshot` (`prayerType` TEXT NOT NULL, " +
                    "`initialCount` INTEGER NOT NULL, `calculatedAt` INTEGER NOT NULL, " +
                    "`method` TEXT NOT NULL, PRIMARY KEY(`prayerType`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `AdjustmentLog` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`prayerType` TEXT NOT NULL, `delta` INTEGER NOT NULL, `reason` TEXT NOT NULL, " +
                    "`note` TEXT, `timestamp` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `CompletionLog` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`prayerType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `batchId` TEXT)"
            )

            db.execSQL(
                "INSERT INTO BaselineSnapshot (prayerType, initialCount, calculatedAt, method) " +
                    "VALUES ('FAJR', 100, 1000, 'EXACT_DATES')"
            )
            db.execSQL("INSERT INTO CompletionLog (prayerType, timestamp, batchId) VALUES ('FAJR', 2000, NULL)")
            db.execSQL("INSERT INTO CompletionLog (prayerType, timestamp, batchId) VALUES ('ASR', 2500, 'b1')")
            db.execSQL(
                "INSERT INTO AdjustmentLog (prayerType, delta, reason, note, timestamp) " +
                    "VALUES ('FAJR', -5, 'MANUAL_CORRECTION', 'recount', 3000)"
            )
            db.version = 1
        }
    }

    @After
    fun deleteDatabase() {
        context.deleteDatabase(dbName)
    }

    private fun openMigrated(): QazaDatabase =
        Room.databaseBuilder(context, QazaDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `migrating 1 to 2 keeps every existing row`() = runTest {
        val db = openMigrated()
        try {
            val baseline = db.baselineSnapshotDao().observeByType(PrayerType.FAJR).first()
            assertEquals(100, baseline?.initialCount)
            assertEquals(CalculationMethod.EXACT_DATES, baseline?.method)

            assertEquals(2, db.completionLogDao().observeAll().first().size)
            assertEquals("b1", db.completionLogDao().observeByType(PrayerType.ASR).first().single().batchId)

            val adjustment = db.adjustmentLogDao().observeAll().first().single()
            assertEquals(-5, adjustment.delta)
            assertEquals(AdjustmentReason.MANUAL_CORRECTION, adjustment.reason)
            assertEquals("recount", adjustment.note)

            // 100 baseline - 5 adjustment - 1 Fajr completion: the derived count survives intact.
            assertEquals(94, db.prayerLedgerDao().observeRemainingCount(PrayerType.FAJR).first()?.remaining)
        } finally {
            db.close()
        }
    }

    @Test
    fun `migrating 1 to 2 adds an empty milestone table that accepts rows`() = runTest {
        val db = openMigrated()
        try {
            assertTrue(db.milestoneDao().getMilestones().first().isEmpty())

            val achievedAt = Instant.parse("2026-03-10T05:00:00Z")
            db.milestoneDao().insertMilestone(MilestoneEntity(prayerType = PrayerType.FAJR, achievedAt = achievedAt))

            val stored = db.milestoneDao().getMilestones().first().single()
            assertEquals(PrayerType.FAJR, stored.prayerType)
            assertEquals(achievedAt, stored.achievedAt)
        } finally {
            db.close()
        }
    }

    @Test
    fun `migrating 1 to 3 adds the bookmark table and still keeps prayer data`() = runTest {
        val db = openMigrated()
        try {
            assertTrue(db.inspirationDao().observeBookmarks().first().isEmpty())

            db.inspirationDao().insert(
                com.zilehasnain.qazatracker.data.local.entity.InspirationBookmarkEntity("q2:153", Instant.parse("2026-03-10T05:00:00Z"))
            )

            assertEquals(listOf("q2:153"), db.inspirationDao().observeBookmarks().first().map { it.itemId })
            assertEquals(94, db.prayerLedgerDao().observeRemainingCount(PrayerType.FAJR).first()?.remaining)
        } finally {
            db.close()
        }
    }
}
