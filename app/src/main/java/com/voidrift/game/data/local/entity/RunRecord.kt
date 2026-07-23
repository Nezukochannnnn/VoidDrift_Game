package com.voidrift.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_records")
data class RunRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val seed: Long,
    val depth: Int,
    val score: Long,
    val killCount: Int,
    val durationSeconds: Float,
    val causeOfDeath: String,
    val timestamp: Long = System.currentTimeMillis()
)
