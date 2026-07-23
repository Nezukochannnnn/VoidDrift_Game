package com.voidrift.game.domain.model

import com.voidrift.game.core.math.AABB

enum class TileType { FLOOR, WALL, VOID, CRYSTAL, EXIT }

data class Tile(
    val tileId: Int,
    val gridX: Int,
    val gridY: Int,
    val type: TileType,
    val voidProgress: Float = 0f,   // 0f = healthy, 1f = fully consumed
    val hasItem: Boolean = false
) {
    val isPassable: Boolean get() = type != TileType.WALL && voidProgress < 1f

    fun worldCenter(tileSize: Float = 64f) = Pair(
        gridX * tileSize + tileSize / 2f,
        gridY * tileSize + tileSize / 2f
    )

    fun toAABB(tileSize: Float = 64f) = AABB(
        centerX = gridX * tileSize + tileSize / 2f,
        centerY = gridY * tileSize + tileSize / 2f,
        halfW   = tileSize / 2f,
        halfH   = tileSize / 2f
    )
}
