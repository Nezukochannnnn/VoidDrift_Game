package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.ScoreRecord
import com.voiddrift.game.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHighScoresUseCase @Inject constructor(
    private val scoreRepository: ScoreRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<ScoreRecord>> {
        return scoreRepository.getTopHighScores(limit)
    }

    fun getTopScore(): Flow<ScoreRecord?> {
        return scoreRepository.getHighScore()
    }
}
