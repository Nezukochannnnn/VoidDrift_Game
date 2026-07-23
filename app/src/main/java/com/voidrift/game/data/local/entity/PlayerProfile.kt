package com.voidrift.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey
    val id: Int = 1,
    val playerName: String = "Drifter",
    val totalRuns: Int = 0,
    val bestDepth: Int = 0,
    val bestScore: Long = 0L,
    val totalPlaytimeSeconds: Float = 0f
)
