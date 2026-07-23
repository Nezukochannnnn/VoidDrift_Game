package com.voidrift.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voidrift.game.data.local.entity.PlayerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createIfAbsent(profile: PlayerProfile)

    @Update
    suspend fun update(profile: PlayerProfile)

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun observe(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getOnce(): PlayerProfile?

    @Query("UPDATE player_profile SET playerName = :name WHERE id = 1")
    suspend fun updateName(name: String)
}
