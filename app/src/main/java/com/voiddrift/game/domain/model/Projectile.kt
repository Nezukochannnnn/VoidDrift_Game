package com.voiddrift.game.domain.model

import java.util.UUID

data class Projectile(
    val id: String = UUID.randomUUID().toString(),
    val isPlayerOwner: Boolean = true,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D.Zero,
    val damage: Float = 20f,
    val radius: Float = 8f,
    val weaponType: WeaponType = WeaponType.PLASMA_SINGLE,
    val ttlMs: Long = 2500L
) {
    val bounds: CollisionBounds.Circle
        get() = CollisionBounds.Circle(position, radius)
}
