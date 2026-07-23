package com.voiddrift.game.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunRecordEntity): Long

    @Query("SELECT * FROM run_records ORDER BY score DESC LIMIT :limit")
    fun getTopRuns(limit: Int = 20): Flow<List<RunRecordEntity>>

    @Query("SELECT * FROM run_records ORDER BY depth DESC LIMIT 1")
    fun getDeepestRun(): Flow<RunRecordEntity?>

    @Query("SELECT COUNT(*) FROM run_records")
    suspend fun getTotalRunCount(): Int

    @Query("DELETE FROM run_records")
    suspend fun clearAll()
}
