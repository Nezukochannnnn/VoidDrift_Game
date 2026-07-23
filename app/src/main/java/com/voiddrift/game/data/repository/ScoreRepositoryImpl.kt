package com.voiddrift.game.data.repository

import com.voiddrift.game.data.local.db.ScoreDao
import com.voiddrift.game.data.local.db.ScoreEntity
import com.voiddrift.game.domain.model.ScoreRecord
import com.voiddrift.game.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepositoryImpl @Inject constructor(
    private val scoreDao: ScoreDao
) : ScoreRepository {

    override fun getTopHighScores(limit: Int): Flow<List<ScoreRecord>> {
        return scoreDao.getTopHighScores(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getHighScore(): Flow<ScoreRecord?> {
        return scoreDao.getHighestScore().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertScore(scoreRecord: ScoreRecord): Long {
        return scoreDao.insertScore(ScoreEntity.fromDomain(scoreRecord))
    }

    override suspend fun clearScores() {
        scoreDao.clearAllScores()
    }
}
