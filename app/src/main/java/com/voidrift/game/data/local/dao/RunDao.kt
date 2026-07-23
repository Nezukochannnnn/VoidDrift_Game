package com.voidrift.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voidrift.game.data.local.entity.RunRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: RunRecord): Long

    @Query("SELECT * FROM run_records ORDER BY score DESC LIMIT :limit")
    fun getTopByScore(limit: Int = 20): Flow<List<RunRecord>>

    @Query("SELECT * FROM run_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RunRecord>>

    @Query("SELECT * FROM run_records ORDER BY depth DESC LIMIT 1")
    fun getDeepest(): Flow<RunRecord?>

    @Query("DELETE FROM run_records")
    suspend fun deleteAll()
}
