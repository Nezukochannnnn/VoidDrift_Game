package com.voidrift.game.data.repository

import com.voidrift.game.data.local.dao.RunDao
import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.domain.repository.IRunRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepositoryImpl @Inject constructor(
    private val dao: RunDao
) : IRunRepository {
    override fun getAllRuns(): Flow<List<RunRecord>> = dao.getAll()
    override fun getTopRunsByScore(limit: Int): Flow<List<RunRecord>> = dao.getTopByScore(limit)
    override fun getDeepestRun(): Flow<RunRecord?> = dao.getDeepest()
    override suspend fun saveRun(run: RunRecord): Long = dao.insert(run)
    override suspend fun deleteAll() = dao.deleteAll()
}
