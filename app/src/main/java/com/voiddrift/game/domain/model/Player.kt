package com.voiddrift.game.domain.model

data class Player(
    val position: Vector2D = Vector2D(0f, 0f),
    val hp: Int = 100,                    // Max 100
    val torchRadius: Float = 300f,        // Decays at 8f/sec
    val velocity: Vector2D = Vector2D.Zero,
    val bounds: AABB = AABB(0f, 0f, 20f, 20f),
    val inventory: List<Item> = emptyList()
) {
    val isAlive: Boolean get() = hp > 0
    val isTorchExtinguished: Boolean get() = torchRadius <= 0f

    /** Returns updated player with torch decayed by deltaTime */
    fun decayTorch(deltaTime: Float): Player {
        val newRadius = (torchRadius - TORCH_DECAY_RATE * deltaTime).coerceAtLeast(0f)
        return copy(torchRadius = newRadius)
    }

    /** Returns an updated player with bounds synchronized to position */
    fun withSyncedBounds(): Player =
        copy(bounds = bounds.copy(centerX = position.x, centerY = position.y))

    companion object {
        const val MAX_HP = 100
        const val INITIAL_TORCH_RADIUS = 300f
        const val TORCH_DECAY_RATE = 8f   // units/sec
        const val HALF_SIZE = 20f
    }
}
