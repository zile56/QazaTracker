package com.example.qazatracker.di

import android.content.Context
import androidx.room.Room
import com.example.qazatracker.data.local.QazaDatabase
import com.example.qazatracker.data.local.dao.AdjustmentLogDao
import com.example.qazatracker.data.local.dao.BaselineSnapshotDao
import com.example.qazatracker.data.local.dao.CompletionLogDao
import com.example.qazatracker.data.local.dao.PrayerLedgerDao
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
        Room.databaseBuilder(context, QazaDatabase::class.java, "qaza-tracker.db").build()

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
}
