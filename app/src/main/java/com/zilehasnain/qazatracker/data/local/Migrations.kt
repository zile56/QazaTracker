package com.zilehasnain.qazatracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: adds the milestone log. Purely additive, so every existing baseline, completion and
 * adjustment row is untouched — a wipe (fallbackToDestructiveMigration) here would delete a
 * user's whole prayer history, which is why it is deliberately not used anywhere.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `prayer_milestones` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`prayerType` TEXT NOT NULL, " +
                "`achievedAt` INTEGER NOT NULL)"
        )
    }
}
