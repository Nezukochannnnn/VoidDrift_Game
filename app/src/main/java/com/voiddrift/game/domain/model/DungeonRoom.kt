package com.voiddrift.game.domain.model

/**
 * Represents a single procedurally generated dungeon room.
 *
 * @param tiles     Flat list of all tiles. Index via [tileAt].
 * @param widthInTiles  Number of tile columns.
 * @param heightInTiles Number of tile rows.
 * @param spawnPoint    World-space spawn coordinate for the player.
 * @param exitTile      Grid coordinates (x,y) of the exit tile.
 * @param seed          The seed used to generate this room (for reproducibility).
 */
data class DungeonRoom(
    val tiles: List<Tile> = emptyList(),
    val widthInTiles: Int = 0,
    val heightInTiles: Int = 0,
    val spawnPoint: Vector2D = Vector2D.Zero,
    val exitTile: Pair<Int, Int> = 0 to 0,
    val seed: Long = 0L
) {
    fun tileAt(x: Int, y: Int): Tile? =
        if (x < 0 || x >= widthInTiles || y < 0 || y >= heightInTiles) null
        else tiles.getOrNull(y * widthInTiles + x)

    fun replaceTile(updated: Tile): DungeonRoom {
        val idx = updated.gridY * widthInTiles + updated.gridX
        if (idx < 0 || idx >= tiles.size) return this
        return copy(tiles = tiles.toMutableList().also { it[idx] = updated })
    }

    /** Returns all tiles matching a given type */
    fun tilesOfType(type: TileType): List<Tile> = tiles.filter { it.type == type }

    companion object {
        const val TILE_SIZE = 64f   // Pixels per tile in world space
    }
}
