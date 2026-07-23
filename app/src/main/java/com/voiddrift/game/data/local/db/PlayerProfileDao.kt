package com.voiddrift.game.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createIfAbsent(profile: PlayerProfileEntity)

    @Update
    suspend fun update(profile: PlayerProfileEntity)

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    /**
     * Atomically update the profile with stats from a just-completed run.
     * Falls back gracefully if no profile row exists yet.
     */
    suspend fun recordRunCompletion(depth: Int, score: Long, durationSeconds: Float) {
        val current = getProfileOnce() ?: PlayerProfileEntity()
        update(
            current.copy(
                totalRuns = current.totalRuns + 1,
                bestDepth = maxOf(current.bestDepth, depth),
                bestScore = maxOf(current.bestScore, score),
                totalPlaytimeSeconds = current.totalPlaytimeSeconds + durationSeconds
            )
        )
    }
}
