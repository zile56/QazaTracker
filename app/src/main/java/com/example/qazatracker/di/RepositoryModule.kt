package com.example.qazatracker.di

import com.example.qazatracker.data.repository.QazaRepositoryImpl
import com.example.qazatracker.domain.repository.QazaRepository
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
}
