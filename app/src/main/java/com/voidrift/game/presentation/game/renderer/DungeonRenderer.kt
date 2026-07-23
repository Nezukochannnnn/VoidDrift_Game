package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.DungeonRoom.Companion.TILE_SIZE
import com.voidrift.game.domain.model.TileType
import com.voidrift.game.presentation.game.renderer.RenderConstants.VOID_ALPHA

/** Renders the dungeon tile grid with void corruption overlays. */
object DungeonRenderer {

    private val FLOOR_COLOR  = Color(0xFF1A1A2E)
    private val WALL_COLOR   = Color(0xFF0D0D1A)
    private val CRYSTAL_BASE = Color(0xFF00E5FF)
    private val EXIT_COLOR   = Color(0xFF39FF14)
    private val VOID_BASE    = Color(0xFF6A0DAD)
    private val GRID_STROKE  = Color(0xFF252540)

    fun DrawScope.drawDungeon(
        tiles: List<com.voidrift.game.domain.model.Tile>,
        cameraOffsetX: Float,
        cameraOffsetY: Float
    ) {
        for (tile in tiles) {
            val wx = tile.gridX * TILE_SIZE - cameraOffsetX
            val wy = tile.gridY * TILE_SIZE - cameraOffsetY

            // Cull off-screen tiles
            if (wx > size.width + TILE_SIZE || wx < -TILE_SIZE) continue
            if (wy > size.height + TILE_SIZE || wy < -TILE_SIZE) continue

            val tileColor = when (tile.type) {
                TileType.FLOOR   -> FLOOR_COLOR
                TileType.WALL    -> WALL_COLOR
                TileType.VOID    -> VOID_BASE
                TileType.CRYSTAL -> CRYSTAL_BASE.copy(alpha = 0.25f)
                TileType.EXIT    -> EXIT_COLOR.copy(alpha = 0.25f)
            }

            drawRect(
                color   = tileColor,
                topLeft = Offset(wx, wy),
                size    = Size(TILE_SIZE, TILE_SIZE),
                style   = Fill
            )

            // Grid lines only on floor
            if (tile.type != TileType.WALL) {
                drawRect(
                    color   = GRID_STROKE,
                    topLeft = Offset(wx, wy),
                    size    = Size(TILE_SIZE, TILE_SIZE),
                    style   = Stroke(width = 0.5f)
                )
            }

            // Void corruption progress overlay
            if (tile.voidProgress > 0f && tile.type != TileType.VOID) {
                drawRect(
                    color   = VOID_BASE.copy(alpha = tile.voidProgress * VOID_ALPHA),
                    topLeft = Offset(wx, wy),
                    size    = Size(TILE_SIZE, TILE_SIZE)
                )
            }

            // Crystal shimmer dot
            if (tile.type == TileType.CRYSTAL && tile.hasItem) {
                drawCircle(
                    color  = CRYSTAL_BASE,
                    radius = 10f,
                    center = Offset(wx + TILE_SIZE / 2f, wy + TILE_SIZE / 2f)
                )
            }

            // Exit indicator
            if (tile.type == TileType.EXIT) {
                drawCircle(
                    color  = EXIT_COLOR,
                    radius = 14f,
                    center = Offset(wx + TILE_SIZE / 2f, wy + TILE_SIZE / 2f)
                )
            }
        }
    }
}
