package com.voiddrift.game.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScoreEntity::class,
        RunRecordEntity::class,
        PlayerProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun runRecordDao(): RunRecordDao
    abstract fun playerProfileDao(): PlayerProfileDao
}
