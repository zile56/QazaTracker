package com.zilehasnain.qazatracker.di

import com.zilehasnain.qazatracker.data.repository.DataExportRepositoryImpl
import com.zilehasnain.qazatracker.data.repository.QazaRepositoryImpl
import com.zilehasnain.qazatracker.data.repository.SettingsRepositoryImpl
import com.zilehasnain.qazatracker.domain.repository.DataExportRepository
import com.zilehasnain.qazatracker.domain.repository.NotificationScheduler
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import com.zilehasnain.qazatracker.notification.WorkManagerNotificationScheduler
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
