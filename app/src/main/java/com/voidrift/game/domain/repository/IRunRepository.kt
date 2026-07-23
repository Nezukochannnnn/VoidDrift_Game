package com.voidrift.game.domain.repository

import com.voidrift.game.data.local.entity.RunRecord
import kotlinx.coroutines.flow.Flow

interface IRunRepository {
    fun getAllRuns(): Flow<List<RunRecord>>
    fun getTopRunsByScore(limit: Int = 20): Flow<List<RunRecord>>
    fun getDeepestRun(): Flow<RunRecord?>
    suspend fun saveRun(run: RunRecord): Long
    suspend fun deleteAll()
}
