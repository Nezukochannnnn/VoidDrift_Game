package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.TileType
import com.voidrift.game.presentation.game.renderer.RenderConstants.CRYSTAL_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.EXIT_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.FLOOR_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.GRID_LINE_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.TILE_SIZE
import com.voidrift.game.presentation.game.renderer.RenderConstants.VOID_ALPHA
import com.voidrift.game.presentation.game.renderer.RenderConstants.VOID_TILE_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.WALL_COLOR
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidCorruptGlow

/**
 * Renders the dungeon tile grid onto a Compose Canvas DrawScope.
 *
 * Tile color mapping:
 *  FLOOR   → VoidSurface  (#12121A)
 *  WALL    → VoidBackground (#0A0A0F)
 *  VOID    → VoidCorrupt  (#1A0030) + glow overlay
 *  CRYSTAL → CrystalBlue  (#7DF9FF)
 *  EXIT    → NeonCyan     (#00FFD1)
 *
 * Void progress is rendered as a deep-purple alpha overlay.
 */
object DungeonRenderer {

    fun DrawScope.drawDungeon(
        tiles: List<com.voidrift.game.domain.model.Tile>,
        cameraOffsetX: Float,
        cameraOffsetY: Float
    ) {
        for (tile in tiles) {
            val wx = tile.gridX * TILE_SIZE - cameraOffsetX
            val wy = tile.gridY * TILE_SIZE - cameraOffsetY

            // Frustum cull — skip tiles fully off-screen
            if (wx > size.width + TILE_SIZE || wx < -TILE_SIZE) continue
            if (wy > size.height + TILE_SIZE || wy < -TILE_SIZE) continue

            val offset = Offset(wx, wy)
            val sz     = Size(TILE_SIZE, TILE_SIZE)

            // ── Base tile color ──────────────────────────────────────────
            val baseColor = when (tile.type) {
                TileType.FLOOR   -> FLOOR_COLOR
                TileType.WALL    -> WALL_COLOR
                TileType.VOID    -> VOID_TILE_COLOR
                TileType.CRYSTAL -> CRYSTAL_COLOR.copy(alpha = 0.18f)
                TileType.EXIT    -> EXIT_COLOR.copy(alpha = 0.18f)
            }
            drawRect(color = baseColor, topLeft = offset, size = sz, style = Fill)

            // ── Grid lines for non-wall tiles ────────────────────────────
            if (tile.type != TileType.WALL) {
                drawRect(color = GRID_LINE_COLOR, topLeft = offset, size = sz,
                    style = Stroke(width = 0.6f))
            }

            // ── Void corruption progress overlay ─────────────────────────
            if (tile.voidProgress > 0.01f && tile.type != TileType.VOID) {
                drawRect(
                    color   = VoidCorrupt.copy(alpha = tile.voidProgress * VOID_ALPHA),
                    topLeft = offset,
                    size    = sz
                )
                // Purple glow edge at medium corruption
                if (tile.voidProgress > 0.45f) {
                    drawRect(
                        color   = VoidCorruptGlow.copy(alpha = (tile.voidProgress - 0.45f) * 0.6f),
                        topLeft = offset,
                        size    = sz,
                        style   = Stroke(width = 2f)
                    )
                }
            }

            val cx = wx + TILE_SIZE / 2f
            val cy = wy + TILE_SIZE / 2f

            // ── Crystal — ice-blue pulsing dot ───────────────────────────
            if (tile.type == TileType.CRYSTAL && tile.hasItem) {
                drawCircle(
                    color  = CRYSTAL_COLOR.copy(alpha = 0.25f),
                    radius = 18f,
                    center = Offset(cx, cy)
                )
                drawCircle(color = CRYSTAL_COLOR, radius = 8f, center = Offset(cx, cy))
            }

            // ── Exit — neon cyan chevron glow ────────────────────────────
            if (tile.type == TileType.EXIT) {
                drawCircle(
                    color  = EXIT_COLOR.copy(alpha = 0.25f),
                    radius = 24f,
                    center = Offset(cx, cy)
                )
                drawCircle(color = EXIT_COLOR, radius = 10f, center = Offset(cx, cy))
                drawRect(
                    color   = EXIT_COLOR.copy(alpha = 0.4f),
                    topLeft = offset,
                    size    = sz,
                    style   = Stroke(width = 2.5f)
                )
            }
        }
    }
}
