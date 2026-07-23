package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.ScoreRecord
import com.voiddrift.game.domain.repository.ScoreRepository
import javax.inject.Inject

class SaveHighScoreUseCase @Inject constructor(
    private val scoreRepository: ScoreRepository
) {
    suspend operator fun invoke(playerName: String, score: Int, wave: Int): Long {
        if (score <= 0) return -1L
        val record = ScoreRecord(
            playerName = playerName.ifBlank { "Drifter" },
            score = score,
            waveReached = wave
        )
        return scoreRepository.insertScore(record)
    }
}
