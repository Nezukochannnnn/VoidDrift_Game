package com.voiddrift.game.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.voiddrift.game.domain.model.ScoreRecord

@Entity(tableName = "high_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playerName: String,
    val score: Int,
    val waveReached: Int,
    val timestamp: Long
) {
    fun toDomain(): ScoreRecord {
        return ScoreRecord(
            id = id,
            playerName = playerName,
            score = score,
            waveReached = waveReached,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(record: ScoreRecord): ScoreEntity {
            return ScoreEntity(
                id = record.id,
                playerName = record.playerName,
                score = record.score,
                waveReached = record.waveReached,
                timestamp = record.timestamp
            )
        }
    }
}
