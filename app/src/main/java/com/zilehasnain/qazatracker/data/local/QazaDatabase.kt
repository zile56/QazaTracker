package com.zilehasnain.qazatracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zilehasnain.qazatracker.data.local.dao.AdjustmentLogDao
import com.zilehasnain.qazatracker.data.local.dao.BaselineSnapshotDao
import com.zilehasnain.qazatracker.data.local.dao.CompletionLogDao
import com.zilehasnain.qazatracker.data.local.dao.InspirationDao
import com.zilehasnain.qazatracker.data.local.dao.MilestoneDao
import com.zilehasnain.qazatracker.data.local.dao.PrayerLedgerDao
import com.zilehasnain.qazatracker.data.local.entity.AdjustmentLog
import com.zilehasnain.qazatracker.data.local.entity.BaselineSnapshot
import com.zilehasnain.qazatracker.data.local.entity.CompletionLog
import com.zilehasnain.qazatracker.data.local.entity.InspirationBookmarkEntity
import com.zilehasnain.qazatracker.data.local.entity.MilestoneEntity

@Database(
    entities = [BaselineSnapshot::class, AdjustmentLog::class, CompletionLog::class, MilestoneEntity::class, InspirationBookmarkEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QazaDatabase : RoomDatabase() {
    abstract fun baselineSnapshotDao(): BaselineSnapshotDao
    abstract fun adjustmentLogDao(): AdjustmentLogDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun prayerLedgerDao(): PrayerLedgerDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun inspirationDao(): InspirationDao
}
