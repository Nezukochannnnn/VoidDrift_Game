package com.voiddrift.game.domain.model

import java.util.UUID

enum class EnemyType(
    val baseHp: Int,
    val baseMoveSpeed: Float,
    val attackDamage: Int,
    val detectionRadius: Float
) {
    CRAWLER(
        baseHp = 20,
        baseMoveSpeed = 80f,
        attackDamage = 8,
        detectionRadius = 160f
    ),
    LURKER(
        baseHp = 35,
        baseMoveSpeed = 50f,
        attackDamage = 15,
        detectionRadius = 220f
    ),
    VOID_WRAITH(
        baseHp = 50,
        baseMoveSpeed = 120f,
        attackDamage = 22,
        detectionRadius = 300f
    )
}

enum class AiState {
    IDLE,
    CHASING,
    ATTACKING,
    SPAWNING
}

data class Enemy(
    val id: UUID = UUID.randomUUID(),
    val type: EnemyType = EnemyType.CRAWLER,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D.Zero,
    val hp: Int = EnemyType.CRAWLER.baseHp,
    val bounds: AABB = AABB(0f, 0f, 18f, 18f),
    val aiState: AiState = AiState.SPAWNING
) {
    val isAlive: Boolean get() = hp > 0

    /** Returns updated enemy with bounds synchronized to position */
    fun withSyncedBounds(): Enemy =
        copy(bounds = bounds.copy(centerX = position.x, centerY = position.y))

    companion object {
        fun create(type: EnemyType, spawnPosition: Vector2D): Enemy {
            val halfSize = when (type) {
                EnemyType.CRAWLER     -> 14f
                EnemyType.LURKER      -> 18f
                EnemyType.VOID_WRAITH -> 22f
            }
            return Enemy(
                type = type,
                position = spawnPosition,
                hp = type.baseHp,
                bounds = AABB(spawnPosition.x, spawnPosition.y, halfSize, halfSize),
                aiState = AiState.SPAWNING
            )
        }
    }
}
