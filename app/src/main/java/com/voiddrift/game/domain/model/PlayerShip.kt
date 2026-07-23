package com.voiddrift.game.domain.model

data class PlayerShip(
    val position: Vector2D = Vector2D(540f, 1600f),
    val velocity: Vector2D = Vector2D.Zero,
    val rotationDegrees: Float = 0f,
    val radius: Float = 40f,
    val health: Float = 100f,
    val maxHealth: Float = 100f,
    val shield: Float = 50f,
    val maxShield: Float = 50f,
    val energy: Float = 100f,
    val maxEnergy: Float = 100f,
    val isInvulnerable: Boolean = false,
    val invulnerabilityTimerMs: Long = 0L,
    val currentWeapon: WeaponType = WeaponType.PLASMA_SINGLE,
    val weaponLevel: Int = 1,
    val lastFiredTimestamp: Long = 0L,
    val driftFactor: Float = 0.94f, // Dynamic momentum inertia
    val isBoostActive: Boolean = false
) {
    val bounds: CollisionBounds.Circle
        get() = CollisionBounds.Circle(position, radius)

    val isAlive: Boolean
        get() = health > 0f
}
