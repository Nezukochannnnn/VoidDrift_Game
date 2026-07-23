package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.CollisionBounds
import com.voiddrift.game.domain.model.Enemy
import com.voiddrift.game.domain.model.GameState
import com.voiddrift.game.domain.model.Obstacle
import com.voiddrift.game.domain.model.PowerUp
import com.voiddrift.game.domain.model.PowerUpType
import com.voiddrift.game.domain.model.Projectile
import com.voiddrift.game.domain.model.Vector2D
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CheckCollisionsUseCase @Inject constructor() {

    data class CollisionResult(
        val updatedState: GameState,
        val destroyedEnemies: List<Enemy> = emptyList(),
        val destroyedObstacles: List<Obstacle> = emptyList(),
        val collectedPowerUps: List<PowerUp> = emptyList(),
        val playerHit: Boolean = false
    )

    operator fun invoke(state: GameState): CollisionResult {
        var player = state.player
        val enemies = state.enemies.toMutableList()
        val obstacles = state.obstacles.toMutableList()
        val projectiles = state.projectiles.toMutableList()
        val powerUps = state.powerUps.toMutableList()

        val destroyedEnemies = mutableListOf<Enemy>()
        val destroyedObstacles = mutableListOf<Obstacle>()
        val collectedPowerUps = mutableListOf<PowerUp>()
        var scoreAdd = 0
        var combo = state.comboCount
        var playerWasHit = false

        // 1. Player Projectiles vs Enemies
        val projectilesToRemove = mutableSetOf<String>()
        val enemiesToRemove = mutableSetOf<String>()

        for (proj in projectiles.filter { it.isPlayerOwner }) {
            for (enemy in enemies) {
                if (enemy.id in enemiesToRemove) continue
                if (checkCircleCollision(proj.bounds, enemy.bounds)) {
                    projectilesToRemove.add(proj.id)
                    val newHp = enemy.health - proj.damage
                    if (newHp <= 0f) {
                        enemiesToRemove.add(enemy.id)
                        destroyedEnemies.add(enemy)
                        combo++
                        scoreAdd += enemy.scoreValue * min(5, (1 + combo / 5))
                    } else {
                        val idx = enemies.indexOfFirst { it.id == enemy.id }
                        if (idx >= 0) enemies[idx] = enemy.copy(health = newHp)
                    }
                    break
                }
            }
        }

        // 2. Player Projectiles vs Obstacles
        val obstaclesToRemove = mutableSetOf<String>()
        for (proj in projectiles.filter { it.isPlayerOwner }) {
            if (proj.id in projectilesToRemove) continue
            for (obs in obstacles) {
                if (obs.id in obstaclesToRemove) continue
                if (checkCircleCollision(proj.bounds, obs.bounds)) {
                    projectilesToRemove.add(proj.id)
                    val newHp = obs.health - proj.damage
                    if (newHp <= 0f) {
                        obstaclesToRemove.add(obs.id)
                        destroyedObstacles.add(obs)
                        scoreAdd += 50
                    } else {
                        val idx = obstacles.indexOfFirst { it.id == obs.id }
                        if (idx >= 0) obstacles[idx] = obs.copy(health = newHp)
                    }
                    break
                }
            }
        }

        // 3. Enemy Projectiles / Enemies / Obstacles vs Player
        if (!player.isInvulnerable) {
            // Enemy Projectiles vs Player
            for (proj in projectiles.filter { !it.isPlayerOwner }) {
                if (proj.id in projectilesToRemove) continue
                if (checkCircleCollision(proj.bounds, player.bounds)) {
                    projectilesToRemove.add(proj.id)
                    playerWasHit = true
                    player = applyDamageToPlayer(player, proj.damage)
                    break
                }
            }

            // Enemies vs Player
            if (!playerWasHit) {
                for (enemy in enemies) {
                    if (enemy.id in enemiesToRemove) continue
                    if (checkCircleCollision(enemy.bounds, player.bounds)) {
                        enemiesToRemove.add(enemy.id)
                        destroyedEnemies.add(enemy)
                        playerWasHit = true
                        player = applyDamageToPlayer(player, 30f)
                        break
                    }
                }
            }

            // Obstacles vs Player
            if (!playerWasHit) {
                for (obs in obstacles) {
                    if (obs.id in obstaclesToRemove) continue
                    if (checkCircleCollision(obs.bounds, player.bounds)) {
                        obstaclesToRemove.add(obs.id)
                        destroyedObstacles.add(obs)
                        playerWasHit = true
                        player = applyDamageToPlayer(player, 25f)
                        break
                    }
                }
            }
        }

        // 4. Player vs PowerUps
        val powerUpsToRemove = mutableSetOf<String>()
        var activePUpType = state.activePowerUpType
        var pUpTimer = state.powerUpTimerMs

        for (pUp in powerUps) {
            if (checkCircleCollision(pUp.bounds, player.bounds)) {
                powerUpsToRemove.add(pUp.id)
                collectedPowerUps.add(pUp)
                activePUpType = pUp.type
                pUpTimer = 8000L

                player = when (pUp.type) {
                    PowerUpType.SHIELD_REPAIR -> player.copy(
                        shield = min(player.maxShield, player.shield + 30f),
                        health = min(player.maxHealth, player.health + 20f)
                    )
                    PowerUpType.WEAPON_OVERLOAD -> player.copy(
                        weaponLevel = min(3, player.weaponLevel + 1)
                    )
                    else -> player
                }
            }
        }

        enemies.removeAll { it.id in enemiesToRemove }
        obstacles.removeAll { it.id in obstaclesToRemove }
        projectiles.removeAll { it.id in projectilesToRemove }
        powerUps.removeAll { it.id in powerUpsToRemove }

        val finalState = state.copy(
            player = player,
            enemies = enemies,
            obstacles = obstacles,
            projectiles = projectiles,
            powerUps = powerUps,
            score = state.score + scoreAdd,
            comboCount = if (playerWasHit) 0 else combo,
            activePowerUpType = activePUpType,
            powerUpTimerMs = pUpTimer
        )

        return CollisionResult(
            updatedState = finalState,
            destroyedEnemies = destroyedEnemies,
            destroyedObstacles = destroyedObstacles,
            collectedPowerUps = collectedPowerUps,
            playerHit = playerWasHit
        )
    }

    private fun checkCircleCollision(c1: CollisionBounds.Circle, c2: CollisionBounds.Circle): Boolean {
        val distSquare = (c1.center.x - c2.center.x) * (c1.center.x - c2.center.x) +
                (c1.center.y - c2.center.y) * (c1.center.y - c2.center.y)
        val radiusSum = c1.radius + c2.radius
        return distSquare <= radiusSum * radiusSum
    }

    private fun applyDamageToPlayer(player: PlayerShip, damage: Float): PlayerShip {
        var shield = player.shield
        var health = player.health

        if (shield > 0f) {
            if (shield >= damage) {
                shield -= damage
            } else {
                val rem = damage - shield
                shield = 0f
                health = max(0f, health - rem)
            }
        } else {
            health = max(0f, health - damage)
        }

        return player.copy(
            shield = shield,
            health = health,
            isInvulnerable = true,
            invulnerabilityTimerMs = 1500L
        )
    }
}
