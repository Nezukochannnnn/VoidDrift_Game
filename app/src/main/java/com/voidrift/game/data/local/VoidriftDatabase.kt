package com.voidrift.game.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voidrift.game.data.local.dao.PlayerDao
import com.voidrift.game.data.local.dao.RunDao
import com.voidrift.game.data.local.entity.PlayerProfile
import com.voidrift.game.data.local.entity.RunRecord

@Database(
    entities = [RunRecord::class, PlayerProfile::class],
    version  = 1,
    exportSchema = false
)
abstract class VoidriftDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun playerDao(): PlayerDao
}
