package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.AiState
import com.voiddrift.game.domain.model.AABB
import com.voiddrift.game.domain.model.DungeonRoom
import com.voiddrift.game.domain.model.DungeonRoom.Companion.TILE_SIZE
import com.voiddrift.game.domain.model.Enemy
import com.voiddrift.game.domain.model.EnemyType
import com.voiddrift.game.domain.model.GameWorld
import com.voiddrift.game.domain.model.Player
import com.voiddrift.game.domain.model.Tile
import com.voiddrift.game.domain.model.TileType
import com.voiddrift.game.domain.model.Vector2D
import javax.inject.Inject
import kotlin.random.Random

// ──────────────────────────────────────────────────────────────────────────────
// BSP Node
// ──────────────────────────────────────────────────────────────────────────────

private data class BspNode(
    val x: Int, val y: Int,
    val w: Int, val h: Int,
    var left: BspNode? = null,
    var right: BspNode? = null,
    var room: BspRoom? = null       // Leaf nodes carry a room
)

private data class BspRoom(
    val x: Int, val y: Int,
    val w: Int, val h: Int
) {
    val centerX: Int get() = x + w / 2
    val centerY: Int get() = y + h / 2
}

/**
 * GenerateDungeonUseCase — BSP (Binary Space Partitioning) procedural dungeon generator.
 *
 * Algorithm outline:
 * 1. Start with the full grid as the root BSP node.
 * 2. Recursively split nodes horizontally or vertically until each leaf is small enough.
 * 3. Place a room within each leaf node (with margin so wall borders remain).
 * 4. Carve corridors between sibling leaf rooms (L-shaped tunnels).
 * 5. Mark spawn, enemies, crystals, and exit.
 * 6. Run BFS from spawn to exit to guarantee reachability.
 */
class GenerateDungeonUseCase @Inject constructor() {

    companion object {
        private const val MIN_LEAF_W = 5
        private const val MIN_LEAF_H = 5
        private const val ROOM_MARGIN = 1  // Wall border around each room
        private const val ENEMY_COUNT_MIN = 2
        private const val ENEMY_COUNT_MAX = 5
        private const val CRYSTAL_COUNT_MIN = 3
        private const val CRYSTAL_COUNT_MAX = 6
    }

    /**
     * @param seed   RNG seed for reproducible generation.
     * @param depth  Current run depth (influences grid size and enemy counts).
     */
    operator fun invoke(seed: Long, depth: Int = 1): GameWorld {
        val rng = Random(seed)

        // Scale grid 8x8 → 16x16 over first 8 depths
        val gridW = (8 + depth.coerceAtMost(8)).coerceIn(8, 16)
        val gridH = (8 + depth.coerceAtMost(8)).coerceIn(8, 16)

        // ── 1. Build BSP tree ──────────────────────────────────────────────
        val root = BspNode(0, 0, gridW, gridH)
        splitNode(root, rng, depth = 0)

        // ── 2. Carve rooms into tile grid ──────────────────────────────────
        val grid = Array(gridH) { y -> Array(gridW) { x ->
            Tile(tileId = y * gridW + x, gridX = x, gridY = y, type = TileType.WALL)
        }}
        carveRooms(root, grid)

        // ── 3. Collect all room centers & carve corridors ──────────────────
        val rooms = mutableListOf<BspRoom>()
        collectRooms(root, rooms)
        carveCorridors(root, grid, rng)

        // ── 4. Seed a single void origin tile far from spawn ───────────────
        val spawnRoom = rooms.first()
        val spawnWorld = Vector2D(
            x = spawnRoom.centerX * TILE_SIZE + TILE_SIZE / 2f,
            y = spawnRoom.centerY * TILE_SIZE + TILE_SIZE / 2f
        )

        val exitRoom = rooms.last()
        grid[exitRoom.centerY][exitRoom.centerX] = grid[exitRoom.centerY][exitRoom.centerX]
            .copy(type = TileType.EXIT)

        // ── 5. BFS guarantee: exit is reachable ────────────────────────────
        // If BFS fails, force a straight corridor from last room to exit room
        if (!bfsReachable(grid, gridW, gridH, spawnRoom.centerX, spawnRoom.centerY,
                exitRoom.centerX, exitRoom.centerY)) {
            carveStraightCorridor(grid, spawnRoom.centerX, spawnRoom.centerY,
                exitRoom.centerX, exitRoom.centerY)
        }

        // ── 6. Place crystals ──────────────────────────────────────────────
        val crystalCount = rng.nextInt(CRYSTAL_COUNT_MIN, CRYSTAL_COUNT_MAX + 1)
        val floorTiles = grid.flatten().filter { it.type == TileType.FLOOR }
        floorTiles.shuffled(rng).take(crystalCount).forEach { tile ->
            grid[tile.gridY][tile.gridX] = tile.copy(type = TileType.CRYSTAL, hasItem = true)
        }

        // ── 7. Place enemies ──────────────────────────────────────────────
        val enemyCount = (ENEMY_COUNT_MIN + depth / 2).coerceIn(ENEMY_COUNT_MIN, ENEMY_COUNT_MAX)
        val enemyTypes = EnemyType.entries.toTypedArray()
        val enemySpawnTiles = grid.flatten()
            .filter { it.type == TileType.FLOOR &&
                    it.gridX != spawnRoom.centerX && it.gridY != spawnRoom.centerY }
            .shuffled(rng)
            .take(enemyCount)

        val enemies = enemySpawnTiles.map { tile ->
            val type = enemyTypes[rng.nextInt(enemyTypes.size)]
            val pos = Vector2D(
                tile.gridX * TILE_SIZE + TILE_SIZE / 2f,
                tile.gridY * TILE_SIZE + TILE_SIZE / 2f
            )
            Enemy.create(type, pos)
        }

        // ── 8. Seed one void origin ────────────────────────────────────────
        val voidSeedTiles = grid.flatten()
            .filter { it.type == TileType.FLOOR }
            .sortedByDescending { tile ->
                val dx = tile.gridX - spawnRoom.centerX
                val dy = tile.gridY - spawnRoom.centerY
                dx * dx + dy * dy
            }.take(3).shuffled(rng)

        voidSeedTiles.take(1).forEach { tile ->
            grid[tile.gridY][tile.gridX] = tile.copy(voidProgress = 0.5f)
        }

        // ── 9. Assemble final tile list & DungeonRoom ─────────────────────
        val flatTiles = grid.flatten().toList()
        val room = DungeonRoom(
            tiles = flatTiles,
            widthInTiles = gridW,
            heightInTiles = gridH,
            spawnPoint = spawnWorld,
            exitTile = exitRoom.centerX to exitRoom.centerY,
            seed = seed
        )

        val player = Player(
            position = spawnWorld,
            bounds = AABB(spawnWorld.x, spawnWorld.y, Player.HALF_SIZE, Player.HALF_SIZE)
        )

        return GameWorld(
            player = player,
            enemies = enemies,
            dungeon = room,
            runDepth = depth
        )
    }

    // ─── BSP recursive split ────────────────────────────────────────────────

    private fun splitNode(node: BspNode, rng: Random, depth: Int) {
        val maxDepth = 4
        if (depth >= maxDepth) return
        if (node.w < MIN_LEAF_W * 2 && node.h < MIN_LEAF_H * 2) return

        val splitHorizontal = when {
            node.w < MIN_LEAF_W * 2 -> true   // Too narrow → must split horizontal
            node.h < MIN_LEAF_H * 2 -> false   // Too short → must split vertical
            else -> rng.nextBoolean()
        }

        if (splitHorizontal) {
            val splitY = rng.nextInt(MIN_LEAF_H, node.h - MIN_LEAF_H + 1)
            node.left  = BspNode(node.x, node.y, node.w, splitY)
            node.right = BspNode(node.x, node.y + splitY, node.w, node.h - splitY)
        } else {
            val splitX = rng.nextInt(MIN_LEAF_W, node.w - MIN_LEAF_W + 1)
            node.left  = BspNode(node.x, node.y, splitX, node.h)
            node.right = BspNode(node.x + splitX, node.y, node.w - splitX, node.h)
        }

        splitNode(node.left!!, rng, depth + 1)
        splitNode(node.right!!, rng, depth + 1)
    }

    // ─── Room placement inside leaf nodes ───────────────────────────────────

    private fun carveRooms(node: BspNode, grid: Array<Array<Tile>>) {
        if (node.left == null && node.right == null) {
            // Leaf — place room with margin
            val roomX = node.x + ROOM_MARGIN
            val roomY = node.y + ROOM_MARGIN
            val roomW = (node.w - ROOM_MARGIN * 2).coerceAtLeast(1)
            val roomH = (node.h - ROOM_MARGIN * 2).coerceAtLeast(1)
            node.room = BspRoom(roomX, roomY, roomW, roomH)

            for (y in roomY until (roomY + roomH).coerceAtMost(grid.size)) {
                for (x in roomX until (roomX + roomW).coerceAtMost(grid[0].size)) {
                    grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
                }
            }
        } else {
            node.left?.let { carveRooms(it, grid) }
            node.right?.let { carveRooms(it, grid) }
        }
    }

    // ─── Corridor carving between siblings ──────────────────────────────────

    private fun carveCorridors(node: BspNode, grid: Array<Array<Tile>>, rng: Random) {
        if (node.left == null || node.right == null) return

        val leftRoom  = getRoom(node.left!!)  ?: return
        val rightRoom = getRoom(node.right!!) ?: return

        // L-shaped tunnel: go horizontal then vertical
        val x1 = leftRoom.centerX
        val y1 = leftRoom.centerY
        val x2 = rightRoom.centerX
        val y2 = rightRoom.centerY

        if (rng.nextBoolean()) {
            carveHLine(grid, y1, x1, x2)
            carveVLine(grid, x2, y1, y2)
        } else {
            carveVLine(grid, x1, y1, y2)
            carveHLine(grid, y2, x1, x2)
        }

        carveCorridors(node.left!!, grid, rng)
        carveCorridors(node.right!!, grid, rng)
    }

    private fun getRoom(node: BspNode): BspRoom? =
        node.room ?: getRoom(node.left ?: return null) ?: getRoom(node.right ?: return null)

    private fun carveHLine(grid: Array<Array<Tile>>, y: Int, x1: Int, x2: Int) {
        val (from, to) = if (x1 < x2) x1 to x2 else x2 to x1
        for (x in from..to) {
            if (y in grid.indices && x in grid[0].indices)
                grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
        }
    }

    private fun carveVLine(grid: Array<Array<Tile>>, x: Int, y1: Int, y2: Int) {
        val (from, to) = if (y1 < y2) y1 to y2 else y2 to y1
        for (y in from..to) {
            if (y in grid.indices && x in grid[0].indices)
                grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
        }
    }

    // ─── Collect all leaf rooms ─────────────────────────────────────────────

    private fun collectRooms(node: BspNode, rooms: MutableList<BspRoom>) {
        node.room?.let { rooms.add(it) }
        node.left?.let { collectRooms(it, rooms) }
        node.right?.let { collectRooms(it, rooms) }
    }

    // ─── BFS reachability check ─────────────────────────────────────────────

    private fun bfsReachable(
        grid: Array<Array<Tile>>, w: Int, h: Int,
        startX: Int, startY: Int, goalX: Int, goalY: Int
    ): Boolean {
        val visited = Array(h) { BooleanArray(w) }
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(startX to startY)
        visited[startY][startX] = true

        val dirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        while (queue.isNotEmpty()) {
            val (cx, cy) = queue.removeFirst()
            if (cx == goalX && cy == goalY) return true
            for ((dx, dy) in dirs) {
                val nx = cx + dx; val ny = cy + dy
                if (nx in 0 until w && ny in 0 until h &&
                    !visited[ny][nx] && grid[ny][nx].type != TileType.WALL) {
                    visited[ny][nx] = true
                    queue.add(nx to ny)
                }
            }
        }
        return false
    }

    // ─── Fallback: force straight corridor spawn→exit ───────────────────────

    private fun carveStraightCorridor(
        grid: Array<Array<Tile>>, x1: Int, y1: Int, x2: Int, y2: Int
    ) {
        carveHLine(grid, y1, x1, x2)
        carveVLine(grid, x2, y1, y2)
    }
}
