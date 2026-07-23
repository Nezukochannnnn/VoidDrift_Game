package com.voiddrift.game.domain.repository

import com.voiddrift.game.domain.model.ScoreRecord
import kotlinx.coroutines.flow.Flow

interface ScoreRepository {
    fun getTopHighScores(limit: Int = 10): Flow<List<ScoreRecord>>
    fun getHighScore(): Flow<ScoreRecord?>
    suspend fun insertScore(scoreRecord: ScoreRecord): Long
    suspend fun clearScores()
}
