package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.Player
import com.voidrift.game.presentation.game.renderer.RenderConstants.PLAYER_FILL
import com.voidrift.game.presentation.game.renderer.RenderConstants.PLAYER_STROKE
import com.voidrift.game.presentation.game.renderer.RenderConstants.TORCH_INNER
import com.voidrift.game.presentation.game.renderer.RenderConstants.TORCH_OUTER
import kotlin.math.sin

/**
 * Renders the player as an angular arrowhead sprite with layered torch glow halos.
 *
 * Visual layers (back to front):
 *  1. Outer torch radius ring (amber, low alpha, pulsing)
 *  2. Mid torch glow disc (amber, medium alpha, pulsing)
 *  3. Innermost hot core (ivory, fading with torchRadius)
 *  4. Arrowhead body — soft-white fill with neon cyan outline
 *  5. Danger indicator when HP < 30% (pulsing red ring)
 */
object PlayerRenderer {

    fun DrawScope.drawPlayer(
        player: Player,
        camX: Float,
        camY: Float,
        timeSec: Float
    ) {
        val sx = player.position.x - camX
        val sy = player.position.y - camY

        // Visible area check
        if (sx < -60f || sx > size.width + 60f) return
        if (sy < -60f || sy > size.height + 60f) return

        val pulse      = 1f + 0.06f * sin(timeSec * 5.5f).toFloat()
        val torchNorm  = (player.torchRadius / 300f).coerceIn(0f, 1f)
        val center     = Offset(sx, sy)

        // ── 1. Far torch-radius ring — ambient scene tinting ──────────────
        if (torchNorm > 0.05f) {
            drawCircle(
                brush  = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        TORCH_OUTER.copy(alpha = 0.055f * torchNorm),
                        Color.Transparent
                    ),
                    center = center,
                    radius = player.torchRadius * pulse
                ),
                radius = player.torchRadius * pulse,
                center = center
            )
        }

        // ── 2. Mid glow disc ──────────────────────────────────────────────
        drawCircle(
            color  = TORCH_OUTER.copy(alpha = 0.18f * torchNorm * pulse),
            radius = 80f * pulse,
            center = center
        )

        // ── 3. Inner hot core ─────────────────────────────────────────────
        drawCircle(
            color  = TORCH_INNER.copy(alpha = 0.35f * torchNorm),
            radius = 28f,
            center = center
        )

        // ── 4. Arrowhead body ─────────────────────────────────────────────
        val body = Path().apply {
            moveTo(sx,        sy - 22f)   // tip
            lineTo(sx - 13f,  sy + 13f)   // bottom-left wing
            lineTo(sx,        sy + 6f)    // inner notch
            lineTo(sx + 13f,  sy + 13f)   // bottom-right wing
            close()
        }
        drawPath(body, PLAYER_FILL,   style = Fill)
        drawPath(body, PLAYER_STROKE.copy(alpha = 0.9f), style = Stroke(width = 2.2f))

        // Center dot
        drawCircle(color = PLAYER_STROKE, radius = 3.5f, center = center)

        // ── 5. Low-HP danger ring ─────────────────────────────────────────
        if (player.hp < 30) {
            val dangerAlpha = 0.5f + 0.5f * sin(timeSec * 8f).toFloat().coerceIn(0f, 1f)
            drawCircle(
                color  = RenderConstants.HP_LOW.copy(alpha = dangerAlpha * 0.7f),
                radius = 28f,
                center = center,
                style  = Stroke(width = 2f)
            )
        }
    }
}
