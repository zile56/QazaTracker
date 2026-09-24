package com.zilehasnain.qazatracker.di

import android.content.Context
import androidx.room.Room
import com.zilehasnain.qazatracker.data.local.MIGRATION_1_2
import com.zilehasnain.qazatracker.data.local.QazaDatabase
import com.zilehasnain.qazatracker.data.local.dao.AdjustmentLogDao
import com.zilehasnain.qazatracker.data.local.dao.BaselineSnapshotDao
import com.zilehasnain.qazatracker.data.local.dao.CompletionLogDao
import com.zilehasnain.qazatracker.data.local.dao.MilestoneDao
import com.zilehasnain.qazatracker.data.local.dao.PrayerLedgerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQazaDatabase(@ApplicationContext context: Context): QazaDatabase =
        Room.databaseBuilder(context, QazaDatabase::class.java, "qaza-tracker.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideBaselineSnapshotDao(database: QazaDatabase): BaselineSnapshotDao =
        database.baselineSnapshotDao()

    @Provides
    fun provideAdjustmentLogDao(database: QazaDatabase): AdjustmentLogDao =
        database.adjustmentLogDao()

    @Provides
    fun provideCompletionLogDao(database: QazaDatabase): CompletionLogDao =
        database.completionLogDao()

    @Provides
    fun providePrayerLedgerDao(database: QazaDatabase): PrayerLedgerDao =
        database.prayerLedgerDao()

    @Provides
    fun provideMilestoneDao(database: QazaDatabase): MilestoneDao =
        database.milestoneDao()
}
