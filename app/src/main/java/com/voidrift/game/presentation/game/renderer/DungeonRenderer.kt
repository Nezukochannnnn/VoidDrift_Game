package com.voidrift.game.presentation.game.renderer

import android.graphics.Paint
import android.os.Build
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.voidrift.game.core.shaders.VoidCorruptShader
import com.voidrift.game.domain.model.Tile
import com.voidrift.game.domain.model.TileType
import com.voidrift.game.ui.theme.CrystalBlue
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidCorruptGlow
import com.voidrift.game.ui.theme.VoidSurface

/**
 * Layer 1 — Dungeon Base Renderer
 * Draws tiles as rounded rectangles:
 *  - FLOOR: dark slate (#12121A) + grid lines
 *  - WALL: solid dark + highlight edge
 *  - VOID: AGSL noise shader on API 33+, alpha + color lerp fallback on API 29-32
 *  - CRYSTAL: pulsing ice-blue glow (BlendMode.Screen)
 *  - EXIT: green portal with rotating dashed border
 */
object DungeonRenderer {

    private const val TILE_SIZE = 64f
    private val CORNER_RADIUS = CornerRadius(8f, 8f)

    fun DrawScope.drawDungeon(
        tiles: List<Tile>,
        cameraOffsetX: Float,
        cameraOffsetY: Float,
        timeSec: Float
    ) {
        val voidShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            VoidCorruptShader.create()
        } else null

        for (tile in tiles) {
            val wx = tile.gridX * TILE_SIZE - cameraOffsetX
            val wy = tile.gridY * TILE_SIZE - cameraOffsetY

            // Frustum cull
            if (wx > size.width + TILE_SIZE || wx < -TILE_SIZE) continue
            if (wy > size.height + TILE_SIZE || wy < -TILE_SIZE) continue

            val offset = Offset(wx, wy)
            val tileSize = Size(TILE_SIZE, TILE_SIZE)

            when (tile.type) {
                TileType.FLOOR -> {
                    // Dark slate rounded rect
                    drawRoundRect(
                        color = VoidSurface,
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS
                    )
                    // Grid line
                    drawRoundRect(
                        color = Color(0xFF1E1E2E),
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS,
                        style = Stroke(width = 0.8f)
                    )
                }

                TileType.WALL -> {
                    // Solid dark wall
                    drawRoundRect(
                        color = VoidBackground,
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS
                    )
                    // Highlight top/left edge
                    drawRoundRect(
                        color = Color(0xFF2C2C3E),
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS,
                        style = Stroke(width = 1.5f)
                    )
                }

                TileType.VOID -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && voidShader != null) {
                        VoidCorruptShader.setUniforms(voidShader, size.width, size.height, TILE_SIZE, timeSec)
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply { setShader(voidShader) }
                            canvas.nativeCanvas.drawRoundRect(wx, wy, wx + TILE_SIZE, wy + TILE_SIZE, 8f, 8f, paint)
                        }
                    } else {
                        // Color lerp fallback
                        val voidAlpha = (0.6f + 0.4f * kotlin.math.sin(timeSec * 3f)).coerceIn(0f, 1f)
                        drawRoundRect(
                            color = VoidCorrupt,
                            topLeft = offset,
                            size = tileSize,
                            cornerRadius = CORNER_RADIUS
                        )
                        drawRoundRect(
                            color = VoidCorruptGlow.copy(alpha = voidAlpha * 0.5f),
                            topLeft = offset,
                            size = tileSize,
                            cornerRadius = CORNER_RADIUS
                        )
                    }
                }

                TileType.CRYSTAL -> {
                    // Floor base
                    drawRoundRect(
                        color = VoidSurface,
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS
                    )
                    if (tile.hasItem) {
                        val cx = wx + TILE_SIZE / 2f
                        val cy = wy + TILE_SIZE / 2f
                        val pulse = 1f + 0.15f * kotlin.math.sin(timeSec * 5f).toFloat()

                        // Pulsing glow with BlendMode.Screen
                        drawCircle(
                            color = CrystalBlue.copy(alpha = 0.35f),
                            radius = 20f * pulse,
                            center = Offset(cx, cy),
                            blendMode = BlendMode.Screen
                        )
                        drawCircle(
                            color = CrystalBlue,
                            radius = 8f,
                            center = Offset(cx, cy)
                        )
                    }
                }

                TileType.EXIT -> {
                    val cx = wx + TILE_SIZE / 2f
                    val cy = wy + TILE_SIZE / 2f

                    drawRoundRect(
                        color = VoidSurface,
                        topLeft = offset,
                        size = tileSize,
                        cornerRadius = CORNER_RADIUS
                    )

                    // Green portal
                    val portalGreen = Color(0xFF39FF14)
                    drawCircle(
                        color = portalGreen.copy(alpha = 0.3f),
                        radius = 22f,
                        center = Offset(cx, cy),
                        blendMode = BlendMode.Screen
                    )

                    // Rotating dashed border
                    val dashAngle = timeSec * 60f
                    drawCircle(
                        color = portalGreen,
                        radius = 18f,
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), dashAngle)
                        )
                    )
                }
            }

            // Partial void corruption overlay for floor tiles
            if (tile.voidProgress > 0.05f && tile.type != TileType.VOID) {
                drawRoundRect(
                    color = VoidCorrupt.copy(alpha = tile.voidProgress * 0.75f),
                    topLeft = offset,
                    size = tileSize,
                    cornerRadius = CORNER_RADIUS
                )
            }
        }
    }
}
