package com.voiddrift.game.domain.model

data class ScoreRecord(
    val id: Long = 0L,
    val playerName: String = "Drifter",
    val score: Int,
    val waveReached: Int,
    val timestamp: Long = System.currentTimeMillis()
)
