package com.example.qazatracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.qazatracker.data.local.dao.AdjustmentLogDao
import com.example.qazatracker.data.local.dao.BaselineSnapshotDao
import com.example.qazatracker.data.local.dao.CompletionLogDao
import com.example.qazatracker.data.local.dao.PrayerLedgerDao
import com.example.qazatracker.data.local.entity.AdjustmentLog
import com.example.qazatracker.data.local.entity.BaselineSnapshot
import com.example.qazatracker.data.local.entity.CompletionLog

@Database(
    entities = [BaselineSnapshot::class, AdjustmentLog::class, CompletionLog::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QazaDatabase : RoomDatabase() {
    abstract fun baselineSnapshotDao(): BaselineSnapshotDao
    abstract fun adjustmentLogDao(): AdjustmentLogDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun prayerLedgerDao(): PrayerLedgerDao
}
