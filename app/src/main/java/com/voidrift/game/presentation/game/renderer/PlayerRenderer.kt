package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.Player
import com.voidrift.game.ui.theme.NeonCyan

/**
 * Layer 4 — Player Renderer
 *  - White circle body with neon cyan (#00FFD1) outline
 *  - Direction indicator line showing movement direction
 */
object PlayerRenderer {

    fun DrawScope.drawPlayer(
        player: Player,
        camX: Float,
        camY: Float
    ) {
        val sx = player.position.x - camX
        val sy = player.position.y - camY
        val center = Offset(sx, sy)
        val radius = 16f

        // ── White Circle Body ─────────────────────────────────────────────
        drawCircle(
            color = Color.White,
            radius = radius,
            center = center,
            style = Fill
        )

        // ── Neon Cyan Outline ─────────────────────────────────────────────
        drawCircle(
            color = NeonCyan,
            radius = radius + 2f,
            center = center,
            style = Stroke(width = 3f)
        )

        // ── Direction Indicator Line ──────────────────────────────────────
        val vel = player.velocity
        val dir = if (vel.magnitudeSq() > 1f) vel.normalized() else com.voidrift.game.core.math.Vector2D.Up
        val lineEnd = center + Offset(dir.x * 28f, dir.y * 28f)

        drawLine(
            color = NeonCyan,
            start = center,
            end = lineEnd,
            strokeWidth = 3f
        )
    }
}
