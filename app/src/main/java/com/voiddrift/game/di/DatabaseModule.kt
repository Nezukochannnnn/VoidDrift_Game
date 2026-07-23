package com.voiddrift.game.di

import android.content.Context
import androidx.room.Room
import com.voiddrift.game.data.local.datastore.UserPreferencesDataStore
import com.voiddrift.game.data.local.db.AppDatabase
import com.voiddrift.game.data.local.db.PlayerProfileDao
import com.voiddrift.game.data.local.db.RunRecordDao
import com.voiddrift.game.data.local.db.ScoreDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voiddrift_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideScoreDao(appDatabase: AppDatabase): ScoreDao {
        return appDatabase.scoreDao()
    }

    @Provides
    @Singleton
    fun provideRunRecordDao(appDatabase: AppDatabase): RunRecordDao {
        return appDatabase.runRecordDao()
    }

    @Provides
    @Singleton
    fun providePlayerProfileDao(appDatabase: AppDatabase): PlayerProfileDao {
        return appDatabase.playerProfileDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }
}
