package com.voiddrift.game.domain.model

import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UpdatePhysicsUseCase @Inject constructor() {

    operator fun invoke(currentState: GameState, deltaTimeSec: Float): GameState {
        if (currentState.status != GameStatus.RUNNING) return currentState

        val player = currentState.player
        val newVel = (player.velocity * player.driftFactor)
        var newPos = player.position + (newVel * deltaTimeSec)

        // Screen boundary clamp with elastic rebound
        val minX = player.radius
        val maxX = currentState.worldWidth - player.radius
        val minY = player.radius
        val maxY = currentState.worldHeight - player.radius

        var finalVel = newVel
        if (newPos.x < minX) {
            newPos = newPos.copy(x = minX)
            finalVel = finalVel.copy(x = -finalVel.x * 0.5f)
        } else if (newPos.x > maxX) {
            newPos = newPos.copy(x = maxX)
            finalVel = finalVel.copy(x = -finalVel.x * 0.5f)
        }

        if (newPos.y < minY) {
            newPos = newPos.copy(y = minY)
            finalVel = finalVel.copy(y = -finalVel.y * 0.5f)
        } else if (newPos.y > maxY) {
            newPos = newPos.copy(y = maxY)
            finalVel = finalVel.copy(y = -finalVel.y * 0.5f)
        }

        // Energy regen
        val newEnergy = min(player.maxEnergy, player.energy + 15f * deltaTimeSec)

        // Invulnerability timer
        val newInvulTimer = max(0L, player.invulnerabilityTimerMs - (deltaTimeSec * 1000f).toLong())
        val updatedPlayer = player.copy(
            position = newPos,
            velocity = finalVel,
            energy = newEnergy,
            invulnerabilityTimerMs = newInvulTimer,
            isInvulnerable = newInvulTimer > 0L
        )

        // Update Projectiles
        val updatedProjectiles = currentState.projectiles.mapNotNull { proj ->
            val pPos = proj.position + (proj.velocity * deltaTimeSec)
            val pTtl = proj.ttlMs - (deltaTimeSec * 1000f).toLong()
            if (pTtl <= 0L || pPos.x < -50f || pPos.x > currentState.worldWidth + 50f ||
                pPos.y < -50f || pPos.y > currentState.worldHeight + 50f
            ) {
                null
            } else {
                proj.copy(position = pPos, ttlMs = pTtl)
            }
        }

        // Update Enemies
        val updatedEnemies = currentState.enemies.map { enemy ->
            val ePos = enemy.position + (enemy.velocity * deltaTimeSec)
            enemy.copy(position = ePos)
        }

        // Update Obstacles
        val updatedObstacles = currentState.obstacles.map { obs ->
            val oPos = obs.position + (obs.velocity * deltaTimeSec)
            val oRot = (obs.rotationDegrees + obs.angularVelocity * deltaTimeSec * 50f) % 360f
            obs.copy(position = oPos, rotationDegrees = oRot)
        }

        // Update PowerUps
        val updatedPowerUps = currentState.powerUps.mapNotNull { pUp ->
            val pos = pUp.position + (pUp.velocity * deltaTimeSec)
            val ttl = pUp.ttlMs - (deltaTimeSec * 1000f).toLong()
            if (ttl <= 0L || pos.y > currentState.worldHeight + 100f) null
            else pUp.copy(position = pos, ttlMs = ttl)
        }

        // Update Active PowerUp timer
        val pUpTimer = max(0L, currentState.powerUpTimerMs - (deltaTimeSec * 1000f).toLong())
        val activePUpType = if (pUpTimer > 0L) currentState.activePowerUpType else null

        return currentState.copy(
            player = updatedPlayer,
            projectiles = updatedProjectiles,
            enemies = updatedEnemies,
            obstacles = updatedObstacles,
            powerUps = updatedPowerUps,
            powerUpTimerMs = pUpTimer,
            activePowerUpType = activePUpType,
            gameTimeMs = currentState.gameTimeMs + (deltaTimeSec * 1000f).toLong()
        )
    }
}
