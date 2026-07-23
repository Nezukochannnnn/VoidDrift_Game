package com.voidrift.game.domain.model

import com.voidrift.game.core.math.AABB
import com.voidrift.game.core.math.Vector2D

enum class ItemType { TORCH_REFILL, HEALTH_POTION, CRYSTAL_SHARD, VOID_WARD, WEAPON_UPGRADE }
data class Item(val type: ItemType, val quantity: Int = 1)

data class Player(
    val position: Vector2D = Vector2D(0f, 0f),
    val hp: Int = MAX_HP,
    val torchRadius: Float = INITIAL_TORCH_RADIUS,
    val velocity: Vector2D = Vector2D.Zero,
    val bounds: AABB = AABB.fromCenter(0f, 0f, HALF_SIZE),
    val inventory: List<Item> = emptyList()
) {
    val isAlive: Boolean get() = hp > 0
    val isTorchExtinguished: Boolean get() = torchRadius <= 0f

    fun decayTorch(dt: Float): Player =
        copy(torchRadius = (torchRadius - TORCH_DECAY_RATE * dt).coerceAtLeast(0f))

    fun syncBounds(): Player =
        copy(bounds = bounds.moveTo(position.x, position.y))

    companion object {
        const val MAX_HP              = 100
        const val INITIAL_TORCH_RADIUS = 300f
        const val TORCH_DECAY_RATE    = 8f      // units/sec
        const val HALF_SIZE           = 20f
    }
}
