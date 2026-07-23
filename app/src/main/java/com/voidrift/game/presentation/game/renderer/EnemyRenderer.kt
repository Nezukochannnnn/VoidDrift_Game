package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.AiState
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.EnemyType
import com.voidrift.game.domain.model.Player
import com.voidrift.game.ui.theme.NeonPink
import kotlin.math.sin

/**
 * Layer 2 — Enemy Renderer
 *  - CRAWLER: neon pink (#FF4DA6) outer glow circle + inner circle
 *  - LURKER: visible ONLY when inside torch radius (alpha = 0 outside light)
 *  - VOID_WRAITH: shifting purple color
 */
object EnemyRenderer {

    fun DrawScope.drawEnemies(
        enemies: List<Enemy>,
        player: Player,
        camX: Float,
        camY: Float,
        timeSec: Float
    ) {
        for (enemy in enemies) {
            if (!enemy.isAlive) continue

            val sx = enemy.position.x - camX
            val sy = enemy.position.y - camY
            val r  = enemy.type.halfSize
            val center = Offset(sx, sy)

            // Cull
            if (sx < -r * 3 || sx > size.width + r * 3) continue
            if (sy < -r * 3 || sy > size.height + r * 3) continue

            // Distance to player for light visibility checks
            val distToPlayer = enemy.position.distanceTo(player.position)

            // LURKER is invisible outside torch radius
            val alpha = when (enemy.type) {
                EnemyType.LURKER -> {
                    if (distToPlayer <= player.torchRadius) {
                        (1f - distToPlayer / player.torchRadius).coerceIn(0.2f, 1f)
                    } else {
                        0f // Alpha = 0 outside light
                    }
                }
                else -> 1f
            }

            if (alpha <= 0f) continue

            val baseColor: Color = when (enemy.type) {
                EnemyType.CRAWLER -> NeonPink
                EnemyType.LURKER  -> Color(0xFFFF7043)
                EnemyType.VOID_WRAITH -> {
                    // Shifting purple
                    val shift = 0.5f + 0.5f * sin(timeSec * 3f).toFloat()
                    Color(0xFF9C27B0).copy(
                        red = 0.6f + 0.3f * shift,
                        blue = 0.8f + 0.2f * (1f - shift)
                    )
                }
            }

            // ── Outer Glow Circle (BlendMode.Screen) ─────────────────────
            val pulse = 1f + 0.1f * sin(timeSec * 6f).toFloat()
            drawCircle(
                color = baseColor.copy(alpha = 0.35f * alpha),
                radius = r * 2.0f * pulse,
                center = center,
                blendMode = BlendMode.Screen
            )

            // ── Solid Inner Circle ───────────────────────────────────────
            drawCircle(
                color = baseColor.copy(alpha = alpha),
                radius = r,
                center = center,
                style = Fill
            )

            // Outline ring
            drawCircle(
                color = baseColor.copy(alpha = alpha),
                radius = r * 1.15f,
                center = center,
                style = Stroke(width = 1.8f)
            )
        }
    }
}
