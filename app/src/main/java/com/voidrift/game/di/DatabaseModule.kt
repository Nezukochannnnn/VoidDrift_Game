package com.voidrift.game.di

import android.content.Context
import androidx.room.Room
import com.voidrift.game.data.local.VoidriftDatabase
import com.voidrift.game.data.local.dao.PlayerDao
import com.voidrift.game.data.local.dao.RunDao
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
    fun provideDatabase(@ApplicationContext ctx: Context): VoidriftDatabase =
        Room.databaseBuilder(ctx, VoidriftDatabase::class.java, "voidrift.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideRunDao(db: VoidriftDatabase): RunDao = db.runDao()

    @Provides
    @Singleton
    fun providePlayerDao(db: VoidriftDatabase): PlayerDao = db.playerDao()
}
