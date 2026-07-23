package com.voiddrift.game.domain.model

sealed class CollisionBounds {
    data class Circle(
        val center: Vector2D,
        val radius: Float
    ) : CollisionBounds()

    data class Box(
        val center: Vector2D,
        val width: Float,
        val height: Float
    ) : CollisionBounds() {
        val left: Float get() = center.x - width / 2f
        val right: Float get() = center.x + width / 2f
        val top: Float get() = center.y - height / 2f
        val bottom: Float get() = center.y + height / 2f
    }
}
