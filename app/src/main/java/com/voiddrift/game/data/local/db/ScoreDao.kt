package com.voiddrift.game.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT :limit")
    fun getTopHighScores(limit: Int): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getHighestScore(): Flow<ScoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(scoreEntity: ScoreEntity): Long

    @Query("DELETE FROM high_scores")
    suspend fun clearAllScores()
}
