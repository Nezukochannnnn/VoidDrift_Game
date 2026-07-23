package com.voiddrift.game.domain.model

import java.util.UUID

enum class PowerUpType {
    SHIELD_REPAIR,
    WEAPON_OVERLOAD,
    EMP_SHOCKWAVE,
    VOID_TIME_FREEZE
}

data class PowerUp(
    val id: String = UUID.randomUUID().toString(),
    val type: PowerUpType = PowerUpType.SHIELD_REPAIR,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D(0f, 60f),
    val radius: Float = 22f,
    val ttlMs: Long = 8000L
) {
    val bounds: CollisionBounds.Circle
        get() = CollisionBounds.Circle(position, radius)
}
