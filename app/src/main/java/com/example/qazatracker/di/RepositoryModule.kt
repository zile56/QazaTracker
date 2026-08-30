package com.example.qazatracker.di

import com.example.qazatracker.data.repository.DataExportRepositoryImpl
import com.example.qazatracker.data.repository.QazaRepositoryImpl
import com.example.qazatracker.data.repository.SettingsRepositoryImpl
import com.example.qazatracker.domain.repository.DataExportRepository
import com.example.qazatracker.domain.repository.NotificationScheduler
import com.example.qazatracker.domain.repository.QazaRepository
import com.example.qazatracker.domain.repository.SettingsRepository
import com.example.qazatracker.notification.WorkManagerNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQazaRepository(impl: QazaRepositoryImpl): QazaRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDataExportRepository(impl: DataExportRepositoryImpl): DataExportRepository

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(impl: WorkManagerNotificationScheduler): NotificationScheduler
}
