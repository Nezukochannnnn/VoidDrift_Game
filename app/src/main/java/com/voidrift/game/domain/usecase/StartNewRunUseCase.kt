package com.voidrift.game.domain.usecase

import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.repository.IPlayerRepository
import javax.inject.Inject
import kotlin.random.Random

class StartNewRunUseCase @Inject constructor(
    private val generateDungeon: GenerateDungeonUseCase,
    private val playerRepo: IPlayerRepository
) {
    suspend operator fun invoke(depth: Int = 1, seed: Long = Random.nextLong()): GameWorld {
        playerRepo.ensureProfileExists()
        return generateDungeon(seed = seed, depth = depth)
    }
}
