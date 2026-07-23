package com.voiddrift.game.di

import com.voiddrift.game.data.repository.AudioRepositoryImpl
import com.voiddrift.game.data.repository.ScoreRepositoryImpl
import com.voiddrift.game.data.repository.SettingsRepositoryImpl
import com.voiddrift.game.domain.repository.AudioRepository
import com.voiddrift.game.domain.repository.ScoreRepository
import com.voiddrift.game.domain.repository.SettingsRepository
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
    abstract fun bindScoreRepository(
        impl: ScoreRepositoryImpl
    ): ScoreRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        impl: AudioRepositoryImpl
    ): AudioRepository
}
