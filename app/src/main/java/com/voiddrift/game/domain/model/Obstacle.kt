package com.voiddrift.game.domain.model

import java.util.UUID

enum class ObstacleType {
    ASTEROID_SMALL,
    ASTEROID_LARGE,
    SPACE_DEBRIS,
    BLACK_HOLE_WELL
}

data class Obstacle(
    val id: String = UUID.randomUUID().toString(),
    val type: ObstacleType = ObstacleType.ASTEROID_SMALL,
    val position: Vector2D = Vector2D.Zero,
    val velocity: Vector2D = Vector2D.Zero,
    val angularVelocity: Float = 1.5f,
    val rotationDegrees: Float = 0f,
    val radius: Float = 35f,
    val health: Float = 40f,
    val maxHealth: Float = 40f
) {
    val bounds: CollisionBounds.Circle
        get() = CollisionBounds.Circle(position, radius)

    val isAlive: Boolean
        get() = health > 0f
}
