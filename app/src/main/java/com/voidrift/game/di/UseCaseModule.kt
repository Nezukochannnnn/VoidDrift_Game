package com.voidrift.game.di

import com.voidrift.game.domain.repository.IPlayerRepository
import com.voidrift.game.domain.repository.IRunRepository
import com.voidrift.game.domain.usecase.GenerateDungeonUseCase
import com.voidrift.game.domain.usecase.GetRunHistoryUseCase
import com.voidrift.game.domain.usecase.SaveRunRecordUseCase
import com.voidrift.game.domain.usecase.StartNewRunUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides @ViewModelScoped
    fun provideGenerateDungeon() = GenerateDungeonUseCase()

    @Provides @ViewModelScoped
    fun provideStartNewRun(
        gen: GenerateDungeonUseCase,
        playerRepo: IPlayerRepository
    ) = StartNewRunUseCase(gen, playerRepo)

    @Provides @ViewModelScoped
    fun provideSaveRunRecord(
        runRepo: IRunRepository,
        playerRepo: IPlayerRepository
    ) = SaveRunRecordUseCase(runRepo, playerRepo)

    @Provides @ViewModelScoped
    fun provideGetRunHistory(runRepo: IRunRepository) = GetRunHistoryUseCase(runRepo)
}
