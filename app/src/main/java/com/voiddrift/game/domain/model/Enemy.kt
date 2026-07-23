package com.voiddrift.game.domain.model

import java.util.UUID

enum class EnemyType {
    VOID_SCOUT,
    PHANTOM_CRUISER,
    WARP_INTERCEPTOR,
    VOID_ABERRATION_BOSS
}

data class Enemy(
    val id: String = UUID.randomUUID().toString(),
    val type: EnemyType = EnemyType.VOID_SCOUT,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D.Zero,
    val rotationDegrees: Float = 0f,
    val radius: Float = 30f,
    val health: Float = 30f,
    val maxHealth: Float = 30f,
    val scoreValue: Int = 100,
    val lastFiredTimestamp: Long = 0L,
    val attackCooldownMs: Long = 1200L,
    val phase: Int = 1
) {
    val bounds: CollisionBounds.Circle
        get() = CollisionBounds.Circle(position, radius)

    val isAlive: Boolean
        get() = health > 0f
}
