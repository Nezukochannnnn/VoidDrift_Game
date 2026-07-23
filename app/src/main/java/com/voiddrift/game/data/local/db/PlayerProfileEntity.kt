package com.voiddrift.game.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists a player's cumulative profile across all runs.
 *
 * @param id                   Singleton row — always id=1.
 * @param playerName           Display name set by the player.
 * @param totalRuns            Total number of runs ever started.
 * @param bestDepth            Deepest dungeon depth ever reached.
 * @param bestScore            All-time highest score.
 * @param totalPlaytimeSeconds Cumulative play time in seconds.
 */
@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey
    val id: Int = 1,         // Singleton — only one profile row
    val playerName: String = "Drifter",
    val totalRuns: Int = 0,
    val bestDepth: Int = 0,
    val bestScore: Long = 0L,
    val totalPlaytimeSeconds: Float = 0f
)
