package com.voidrift.game.core.engine

import com.voidrift.game.core.math.AABB
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.model.Tile
import com.voidrift.game.domain.model.TileType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stateless collision system — takes a [GameWorld] and returns a new resolved [GameWorld].
 *
 * Handles:
 *  1. Player ↔ Wall AABB resolution (push-out via MTV)
 *  2. Player ↔ Enemy AABB overlap → apply contact damage to player
 *  3. Enemy ↔ Wall AABB resolution (enemies cannot walk through walls)
 *  4. Player ↔ Crystal tile — pickup detection
 *  5. Player ↔ Exit tile — room-cleared trigger
 */
@Singleton
class CollisionSystem @Inject constructor() {

    companion object {
        const val TILE_SIZE = 64f
        const val ENEMY_CONTACT_DAMAGE_PER_FRAME = 1
    }

    fun resolve(world: GameWorld): GameWorld {
        var player = world.player
        var enemies = world.enemies
        var dungeon = world.dungeon
        var score = world.score
        var isRoomCleared = world.isRoomCleared

        val wallBoxes: List<AABB> = dungeon.tiles
            .filter { it.type == TileType.WALL }
            .map { wallAabb(it) }

        // ── 1. Player vs Walls ──────────────────────────────────────────────
        var pBox = player.bounds
        for (wall in wallBoxes) {
            if (pBox.overlaps(wall)) {
                val mtv = pBox.mtv(wall)
                pBox = pBox.translate(mtv.x, mtv.y)
                player = player.copy(
                    position = Vector2D(pBox.centerX, pBox.centerY),
                    velocity = Vector2D.Zero,
                    bounds   = pBox
                )
            }
        }

        // ── 2. Enemies vs Walls ─────────────────────────────────────────────
        enemies = enemies.map { enemy ->
            var eBox = enemy.bounds
            for (wall in wallBoxes) {
                if (eBox.overlaps(wall)) {
                    val mtv = eBox.mtv(wall)
                    eBox = eBox.translate(mtv.x, mtv.y)
                }
            }
            enemy.copy(
                position = Vector2D(eBox.centerX, eBox.centerY),
                bounds   = eBox
            )
        }

        // ── 3. Player vs Enemies (contact damage) ───────────────────────────
        var hp = player.hp
        for (enemy in enemies) {
            if (enemy.isAlive && pBox.overlaps(enemy.bounds)) {
                hp = (hp - ENEMY_CONTACT_DAMAGE_PER_FRAME).coerceAtLeast(0)
            }
        }
        player = player.copy(hp = hp, bounds = pBox)

        // ── 4. Player vs Crystal & Void tiles ────────────────────────────────
        val playerGridX = (player.position.x / TILE_SIZE).toInt()
        val playerGridY = (player.position.y / TILE_SIZE).toInt()
        val currentTile = dungeon.tileAt(playerGridX, playerGridY)

        if (currentTile?.type == TileType.CRYSTAL && currentTile.hasItem) {
            dungeon = dungeon.replaceTile(
                currentTile.copy(type = TileType.FLOOR, hasItem = false)
            )
            score += 500L
            val refueledTorch = (player.torchRadius + 60f).coerceAtMost(com.voidrift.game.domain.model.Player.INITIAL_TORCH_RADIUS)
            player = player.copy(torchRadius = refueledTorch)
        } else if (currentTile != null && (currentTile.type == TileType.VOID || currentTile.voidProgress > 0.6f)) {
            // Stand on void corruption damage
            val voidHp = (player.hp - 1).coerceAtLeast(0)
            val voidTorch = (player.torchRadius - 0.5f).coerceAtLeast(0f)
            player = player.copy(hp = voidHp, torchRadius = voidTorch)
        }

        // ── 5. Player vs Exit tile ──────────────────────────────────────────
        if (currentTile?.type == TileType.EXIT) {
            isRoomCleared = true
        }

        return world.copy(
            player        = player,
            enemies       = enemies,
            dungeon       = dungeon,
            score         = score,
            isRoomCleared = isRoomCleared
        )
    }

    private fun wallAabb(tile: Tile) = AABB(
        centerX = tile.gridX * TILE_SIZE + TILE_SIZE / 2f,
        centerY = tile.gridY * TILE_SIZE + TILE_SIZE / 2f,
        halfW   = TILE_SIZE / 2f,
        halfH   = TILE_SIZE / 2f
    )
}
