package com.voidrift.game.domain.usecase

import com.voidrift.game.core.math.AABB
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.AiState
import com.voidrift.game.domain.model.DungeonRoom
import com.voidrift.game.domain.model.DungeonRoom.Companion.TILE_SIZE
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.EnemyType
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.model.Player
import com.voidrift.game.domain.model.Tile
import com.voidrift.game.domain.model.TileType
import javax.inject.Inject
import kotlin.random.Random

// ── BSP internals ──────────────────────────────────────────────────────────────

private data class BspNode(
    val x: Int, val y: Int, val w: Int, val h: Int,
    var left: BspNode? = null,
    var right: BspNode? = null,
    var room: BspRoom? = null
)

private data class BspRoom(val x: Int, val y: Int, val w: Int, val h: Int) {
    val cx: Int get() = x + w / 2
    val cy: Int get() = y + h / 2
}

/**
 * Procedural BSP dungeon generator.
 *
 * - Seeds [kotlin.random.Random] for full reproducibility.
 * - Grid size 8×8 → 16×16 scaling with [depth].
 * - Places 2–5 enemies, 3–6 crystals, 1 exit.
 * - Guarantees exit reachable from spawn via BFS, falling back to forced tunnel.
 */
class GenerateDungeonUseCase @Inject constructor() {

    companion object {
        private const val MIN_LEAF = 5
        private const val MARGIN   = 1
        private const val ENEMY_MIN = 2; private const val ENEMY_MAX = 5
        private const val CRYSTAL_MIN = 3; private const val CRYSTAL_MAX = 6
    }

    operator fun invoke(seed: Long, depth: Int = 1): GameWorld {
        val rng   = Random(seed)
        val gridW = (8 + depth.coerceAtMost(8)).coerceIn(8, 16)
        val gridH = (8 + depth.coerceAtMost(8)).coerceIn(8, 16)

        val grid = Array(gridH) { y -> Array(gridW) { x ->
            Tile(tileId = y * gridW + x, gridX = x, gridY = y, type = TileType.WALL)
        }}

        val root = BspNode(0, 0, gridW, gridH)
        split(root, rng, 0)
        carveRooms(root, grid)
        carveCorridors(root, grid, rng)

        val rooms = mutableListOf<BspRoom>()
        collectRooms(root, rooms)

        val spawnRoom = rooms.first()
        val exitRoom  = rooms.last()

        // Exit tile
        grid[exitRoom.cy][exitRoom.cx] = grid[exitRoom.cy][exitRoom.cx].copy(type = TileType.EXIT)

        // BFS reachability guarantee
        if (!bfsReachable(grid, gridW, gridH, spawnRoom.cx, spawnRoom.cy, exitRoom.cx, exitRoom.cy)) {
            hLine(grid, spawnRoom.cy, spawnRoom.cx, exitRoom.cx)
            vLine(grid, exitRoom.cx, spawnRoom.cy, exitRoom.cy)
        }

        // Crystals
        val floors = grid.flatten().filter { it.type == TileType.FLOOR }
        floors.shuffled(rng).take(rng.nextInt(CRYSTAL_MIN, CRYSTAL_MAX + 1)).forEach { t ->
            grid[t.gridY][t.gridX] = t.copy(type = TileType.CRYSTAL, hasItem = true)
        }

        // Void seed (far from spawn)
        floors.filter { it.type == TileType.FLOOR }
            .sortedByDescending { t ->
                val dx = t.gridX - spawnRoom.cx; val dy = t.gridY - spawnRoom.cy
                dx * dx + dy * dy
            }.take(3).shuffled(rng).take(1).forEach { t ->
                grid[t.gridY][t.gridX] = t.copy(voidProgress = 0.5f)
            }

        // Enemies
        val enemyCount = (ENEMY_MIN + depth / 2).coerceIn(ENEMY_MIN, ENEMY_MAX)
        val spawnTiles = grid.flatten()
            .filter { it.type == TileType.FLOOR && !(it.gridX == spawnRoom.cx && it.gridY == spawnRoom.cy) }
            .shuffled(rng).take(enemyCount)
        val enemies = spawnTiles.map { t ->
            val type = EnemyType.entries.random(rng)
            Enemy.spawn(type, Vector2D(t.gridX * TILE_SIZE + TILE_SIZE / 2f,
                                       t.gridY * TILE_SIZE + TILE_SIZE / 2f))
        }

        val spawnPx = Vector2D(
            spawnRoom.cx * TILE_SIZE + TILE_SIZE / 2f,
            spawnRoom.cy * TILE_SIZE + TILE_SIZE / 2f
        )
        val player = Player(
            position = spawnPx,
            bounds   = AABB.fromCenter(spawnPx.x, spawnPx.y, Player.HALF_SIZE)
        )
        val dungeon = DungeonRoom(
            tiles         = grid.flatten().toList(),
            widthInTiles  = gridW,
            heightInTiles = gridH,
            spawnPoint    = spawnPx,
            exitTile      = exitRoom.cx to exitRoom.cy,
            seed          = seed
        )
        return GameWorld(player = player, enemies = enemies, dungeon = dungeon, runDepth = depth)
    }

    // ── BSP helpers ────────────────────────────────────────────────────────────

    private fun split(n: BspNode, rng: Random, depth: Int) {
        if (depth >= 4) return
        if (n.w < MIN_LEAF * 2 && n.h < MIN_LEAF * 2) return
        val horiz = when {
            n.w < MIN_LEAF * 2 -> true
            n.h < MIN_LEAF * 2 -> false
            else -> rng.nextBoolean()
        }
        if (horiz) {
            val sy = rng.nextInt(MIN_LEAF, n.h - MIN_LEAF + 1)
            n.left  = BspNode(n.x, n.y,        n.w, sy);        split(n.left!!, rng, depth + 1)
            n.right = BspNode(n.x, n.y + sy,   n.w, n.h - sy);  split(n.right!!, rng, depth + 1)
        } else {
            val sx = rng.nextInt(MIN_LEAF, n.w - MIN_LEAF + 1)
            n.left  = BspNode(n.x,       n.y, sx,        n.h);  split(n.left!!, rng, depth + 1)
            n.right = BspNode(n.x + sx,  n.y, n.w - sx,  n.h);  split(n.right!!, rng, depth + 1)
        }
    }

    private fun carveRooms(n: BspNode, grid: Array<Array<Tile>>) {
        if (n.left == null && n.right == null) {
            val rx = n.x + MARGIN; val ry = n.y + MARGIN
            val rw = (n.w - MARGIN * 2).coerceAtLeast(1)
            val rh = (n.h - MARGIN * 2).coerceAtLeast(1)
            n.room = BspRoom(rx, ry, rw, rh)
            for (y in ry until (ry + rh).coerceAtMost(grid.size))
                for (x in rx until (rx + rw).coerceAtMost(grid[0].size))
                    grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
        } else { n.left?.let { carveRooms(it, grid) }; n.right?.let { carveRooms(it, grid) } }
    }

    private fun carveCorridors(n: BspNode, grid: Array<Array<Tile>>, rng: Random) {
        if (n.left == null || n.right == null) return
        val l = room(n.left!!) ?: return
        val r = room(n.right!!) ?: return
        if (rng.nextBoolean()) { hLine(grid, l.cy, l.cx, r.cx); vLine(grid, r.cx, l.cy, r.cy) }
        else                   { vLine(grid, l.cx, l.cy, r.cy); hLine(grid, r.cy, l.cx, r.cx) }
        carveCorridors(n.left!!, grid, rng); carveCorridors(n.right!!, grid, rng)
    }

    private fun room(n: BspNode): BspRoom? = n.room ?: n.left?.let { room(it) } ?: n.right?.let { room(it) }
    private fun collectRooms(n: BspNode, out: MutableList<BspRoom>) { n.room?.let { out.add(it) }; n.left?.let { collectRooms(it, out) }; n.right?.let { collectRooms(it, out) } }

    private fun hLine(grid: Array<Array<Tile>>, y: Int, x1: Int, x2: Int) {
        val (a, b) = if (x1 < x2) x1 to x2 else x2 to x1
        for (x in a..b) if (y in grid.indices && x in grid[0].indices)
            grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
    }
    private fun vLine(grid: Array<Array<Tile>>, x: Int, y1: Int, y2: Int) {
        val (a, b) = if (y1 < y2) y1 to y2 else y2 to y1
        for (y in a..b) if (y in grid.indices && x in grid[0].indices)
            grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
    }

    private fun bfsReachable(grid: Array<Array<Tile>>, w: Int, h: Int, sx: Int, sy: Int, gx: Int, gy: Int): Boolean {
        val vis = Array(h) { BooleanArray(w) }
        val q = ArrayDeque<Pair<Int, Int>>().also { it.add(sx to sy) }
        vis[sy][sx] = true
        val dirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        while (q.isNotEmpty()) {
            val (cx, cy) = q.removeFirst()
            if (cx == gx && cy == gy) return true
            for ((dx, dy) in dirs) {
                val nx = cx + dx; val ny = cy + dy
                if (nx in 0 until w && ny in 0 until h && !vis[ny][nx] && grid[ny][nx].type != TileType.WALL) {
                    vis[ny][nx] = true; q.add(nx to ny)
                }
            }
        }
        return false
    }
}
