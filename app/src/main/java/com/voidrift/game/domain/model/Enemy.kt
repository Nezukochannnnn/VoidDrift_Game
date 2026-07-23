package com.voidrift.game.domain.model

import com.voidrift.game.core.math.AABB
import com.voidrift.game.core.math.Vector2D
import java.util.UUID

enum class EnemyType(
    val baseHp: Int,
    val speed: Float,
    val damage: Int,
    val detectionRadius: Float,
    val halfSize: Float
) {
    CRAWLER    (baseHp = 20, speed = 80f,  damage = 8,  detectionRadius = 160f, halfSize = 14f),
    LURKER     (baseHp = 35, speed = 50f,  damage = 15, detectionRadius = 220f, halfSize = 18f),
    VOID_WRAITH(baseHp = 50, speed = 120f, damage = 22, detectionRadius = 300f, halfSize = 22f)
}

enum class AiState { IDLE, CHASING, ATTACKING, SPAWNING }

data class Enemy(
    val id: UUID = UUID.randomUUID(),
    val type: EnemyType = EnemyType.CRAWLER,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D.Zero,
    val hp: Int = EnemyType.CRAWLER.baseHp,
    val bounds: AABB = AABB.fromCenter(0f, 0f, EnemyType.CRAWLER.halfSize),
    val aiState: AiState = AiState.SPAWNING
) {
    val isAlive: Boolean get() = hp > 0

    fun syncBounds(): Enemy = copy(bounds = bounds.moveTo(position.x, position.y))

    companion object {
        fun spawn(type: EnemyType, pos: Vector2D) = Enemy(
            type     = type,
            position = pos,
            hp       = type.baseHp,
            bounds   = AABB.fromCenter(pos.x, pos.y, type.halfSize),
            aiState  = AiState.SPAWNING
        )
    }
}
