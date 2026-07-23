package com.voidrift.game.di

import com.voidrift.game.data.repository.PlayerRepositoryImpl
import com.voidrift.game.data.repository.RunRepositoryImpl
import com.voidrift.game.domain.repository.IPlayerRepository
import com.voidrift.game.domain.repository.IRunRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindRunRepository(impl: RunRepositoryImpl): IRunRepository

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): IPlayerRepository
}
