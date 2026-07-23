package com.voidrift.game.domain.usecase

import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.repository.IPlayerRepository
import com.voidrift.game.domain.repository.IRunRepository
import javax.inject.Inject

class SaveRunRecordUseCase @Inject constructor(
    private val runRepo: IRunRepository,
    private val playerRepo: IPlayerRepository
) {
    suspend operator fun invoke(
        world: GameWorld,
        seed: Long,
        durationSeconds: Float,
        causeOfDeath: String
    ): Long {
        val record = RunRecord(
            seed            = seed,
            depth           = world.runDepth,
            score           = world.score,
            killCount       = world.killCount,
            durationSeconds = durationSeconds,
            causeOfDeath    = causeOfDeath
        )
        val id = runRepo.saveRun(record)
        playerRepo.recordRunCompletion(
            depth           = world.runDepth,
            score           = world.score,
            durationSeconds = durationSeconds
        )
        return id
    }
}
