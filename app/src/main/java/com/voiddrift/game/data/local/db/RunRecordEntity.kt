package com.voiddrift.game.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists a completed run record.
 *
 * @param id               Auto-generated primary key.
 * @param seed             BSP dungeon seed used for this run.
 * @param depth            How deep the player reached.
 * @param score            Final score accumulated.
 * @param killCount        Total enemies killed.
 * @param durationSeconds  Total play time in seconds.
 * @param causeOfDeath     Human-readable cause (e.g. "Void consumed torch", "VOID_WRAITH strike").
 * @param timestamp        Unix epoch ms when the run ended.
 */
@Entity(tableName = "run_records")
data class RunRecordEntity(
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
