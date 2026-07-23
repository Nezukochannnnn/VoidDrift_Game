package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.Enemy
import com.voiddrift.game.domain.model.EnemyType
import com.voiddrift.game.domain.model.GameState
import com.voiddrift.game.domain.model.Obstacle
import com.voiddrift.game.domain.model.ObstacleType
import com.voiddrift.game.domain.model.PowerUp
import com.voiddrift.game.domain.model.PowerUpType
import com.voiddrift.game.domain.model.Vector2D
import javax.inject.Inject
import kotlin.random.Random

class SpawnEntitiesUseCase @Inject constructor() {

    operator fun invoke(state: GameState, lastSpawnTimeMs: Long): Pair<GameState, Long> {
        val now = state.gameTimeMs
        val spawnInterval = maxOf(1000L, 3000L - (state.waveNumber * 200L))

        if (now - lastSpawnTimeMs < spawnInterval) {
            return Pair(state, lastSpawnTimeMs)
        }

        val enemies = state.enemies.toMutableList()
        val obstacles = state.obstacles.toMutableList()
        val powerUps = state.powerUps.toMutableList()

        val spawnX = Random.nextFloat() * (state.worldWidth - 100f) + 50f
        val spawnChoice = Random.nextInt(100)

        when {
            spawnChoice < 50 && enemies.size < (5 + state.waveNumber * 2) -> {
                // Spawn enemy
                val type = if (state.waveNumber > 3 && Random.nextBoolean()) EnemyType.PHANTOM_CRUISER else EnemyType.VOID_SCOUT
                val speed = 120f + (state.waveNumber * 15f)
                val enemy = Enemy(
                    type = type,
                    position = Vector2D(spawnX, -50f),
                    velocity = Vector2D(if (Random.nextBoolean()) 40f else -40f, speed),
                    radius = if (type == EnemyType.PHANTOM_CRUISER) 45f else 28f,
                    health = if (type == EnemyType.PHANTOM_CRUISER) 80f else 30f,
                    scoreValue = if (type == EnemyType.PHANTOM_CRUISER) 250 else 100
                )
                enemies.add(enemy)
            }
            spawnChoice < 85 && obstacles.size < 6 -> {
                // Spawn asteroid
                val isLarge = Random.nextBoolean()
                val obs = Obstacle(
                    type = if (isLarge) ObstacleType.ASTEROID_LARGE else ObstacleType.ASTEROID_SMALL,
                    position = Vector2D(spawnX, -60f),
                    velocity = Vector2D(Random.nextFloat() * 60f - 30f, 90f + Random.nextFloat() * 50f),
                    radius = if (isLarge) 50f else 28f,
                    health = if (isLarge) 60f else 25f
                )
                obstacles.add(obs)
            }
            spawnChoice < 95 && powerUps.size < 2 -> {
                // Spawn powerup
                val pType = PowerUpType.entries.toTypedArray().random()
                val pUp = PowerUp(
                    type = pType,
                    position = Vector2D(spawnX, -40f),
                    velocity = Vector2D(0f, 80f)
                )
                powerUps.add(pUp)
            }
        }

        val updatedState = state.copy(
            enemies = enemies,
            obstacles = obstacles,
            powerUps = powerUps
        )

        return Pair(updatedState, now)
    }
}
