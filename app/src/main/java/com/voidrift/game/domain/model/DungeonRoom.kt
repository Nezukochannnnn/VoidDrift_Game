package com.voidrift.game.domain.model

import com.voidrift.game.core.math.Vector2D

data class DungeonRoom(
    val tiles: List<Tile> = emptyList(),
    val widthInTiles: Int = 0,
    val heightInTiles: Int = 0,
    val spawnPoint: Vector2D = Vector2D.Zero,
    val exitTile: Pair<Int, Int> = 0 to 0,
    val seed: Long = 0L
) {
    companion object { const val TILE_SIZE = 64f }

    fun tileAt(x: Int, y: Int): Tile? =
        if (x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) null
        else tiles.getOrNull(y * widthInTiles + x)

    fun replaceTile(updated: Tile): DungeonRoom {
        val idx = updated.gridY * widthInTiles + updated.gridX
        if (idx !in tiles.indices) return this
        return copy(tiles = tiles.toMutableList().also { it[idx] = updated })
    }

    fun tilesOfType(type: TileType): List<Tile> = tiles.filter { it.type == type }

    fun isInBounds(x: Int, y: Int): Boolean =
        x in 0 until widthInTiles && y in 0 until heightInTiles
}
