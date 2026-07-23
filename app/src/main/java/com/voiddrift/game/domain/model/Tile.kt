package com.voiddrift.game.domain.model

enum class TileType {
    FLOOR,
    WALL,
    VOID,
    CRYSTAL,
    EXIT
}

data class Tile(
    val tileId: Int,
    val gridX: Int,
    val gridY: Int,
    val type: TileType,
    val voidProgress: Float = 0f, // 0f = healthy, 1f = fully consumed
    val hasItem: Boolean = false
) {
    /** A tile is passable if it is not a WALL and not fully void-consumed */
    val isPassable: Boolean
        get() = type != TileType.WALL && voidProgress < 1f

    /** World-space center of this tile */
    fun worldCenter(tileSize: Float): Vector2D =
        Vector2D(gridX * tileSize + tileSize / 2f, gridY * tileSize + tileSize / 2f)
}
