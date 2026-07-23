package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.AiState
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.EnemyType
import com.voidrift.game.presentation.game.renderer.RenderConstants.ATTACK_RING
import com.voidrift.game.presentation.game.renderer.RenderConstants.CRAWLER_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.HP_BAR_BG
import com.voidrift.game.presentation.game.renderer.RenderConstants.HP_BAR_FG
import com.voidrift.game.presentation.game.renderer.RenderConstants.LURKER_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.WRAITH_COLOR
import kotlin.math.sin

/**
 * Renders enemies onto a Compose Canvas DrawScope.
 *
 * Enemy visual encoding by type:
 *  CRAWLER     → NeonPink (#FF4DA6) angular square, sharp corner cues danger
 *  LURKER      → Hot orange rounded rect, mid-size threat
 *  VOID_WRAITH → Purple glow (#9C27B0) translucent circle with pulsing corona
 *
 * Each enemy also shows:
 *  - HP bar (neon pink, 4px height, with dark background)
 *  - ATTACKING state → concentric pulsing ring
 *  - SPAWNING state → low opacity fade-in
 */
object EnemyRenderer {

    fun DrawScope.drawEnemies(
        enemies: List<Enemy>,
        camX: Float,
        camY: Float,
        timeSec: Float = 0f
    ) {
        for (enemy in enemies) {
            if (!enemy.isAlive) continue

            val sx = enemy.position.x - camX
            val sy = enemy.position.y - camY
            val r  = enemy.type.halfSize

            // Frustum cull
            if (sx < -r * 3 || sx > size.width + r * 3) continue
            if (sy < -r * 3 || sy > size.height + r * 3) continue

            val center = Offset(sx, sy)
            val bodyAlpha = if (enemy.aiState == AiState.SPAWNING) 0.5f else 1f

            val baseColor: Color = when (enemy.type) {
                EnemyType.CRAWLER     -> CRAWLER_COLOR
                EnemyType.LURKER      -> LURKER_COLOR
                EnemyType.VOID_WRAITH -> WRAITH_COLOR
            }

            // ── Attacking pulsing ring ────────────────────────────────────
            if (enemy.aiState == AiState.ATTACKING) {
                val ringPulse = 0.5f + 0.5f * sin(timeSec * 9f).toFloat()
                drawCircle(
                    color  = ATTACK_RING.copy(alpha = ringPulse * 0.55f),
                    radius = r * 2.2f,
                    center = center
                )
                drawCircle(
                    color  = ATTACK_RING.copy(alpha = 0.2f),
                    radius = r * 1.5f,
                    center = center
                )
            }

            // ── Chasing: outer detection aura ────────────────────────────
            if (enemy.aiState == AiState.CHASING) {
                drawCircle(
                    color  = baseColor.copy(alpha = 0.08f),
                    radius = r * 3f,
                    center = center
                )
            }

            // ── Body by type ──────────────────────────────────────────────
            when (enemy.type) {
                EnemyType.CRAWLER -> {
                    // Angular square — sharp, fast, dangerous
                    drawRect(
                        color   = baseColor.copy(alpha = bodyAlpha),
                        topLeft = Offset(sx - r, sy - r),
                        size    = Size(r * 2f, r * 2f),
                        style   = Fill
                    )
                    drawRect(
                        color   = baseColor,
                        topLeft = Offset(sx - r, sy - r),
                        size    = Size(r * 2f, r * 2f),
                        style   = Stroke(width = 1.5f)
                    )
                    // Inner X mark
                    drawLine(baseColor.copy(alpha = 0.5f), Offset(sx - r * 0.6f, sy - r * 0.6f),
                        Offset(sx + r * 0.6f, sy + r * 0.6f), strokeWidth = 1f)
                    drawLine(baseColor.copy(alpha = 0.5f), Offset(sx + r * 0.6f, sy - r * 0.6f),
                        Offset(sx - r * 0.6f, sy + r * 0.6f), strokeWidth = 1f)
                }
                EnemyType.LURKER -> {
                    // Diamond shape — slow lurking predator
                    val path = Path().apply {
                        moveTo(sx,         sy - r)
                        lineTo(sx + r,     sy)
                        lineTo(sx,         sy + r)
                        lineTo(sx - r,     sy)
                        close()
                    }
                    drawPath(path, baseColor.copy(alpha = bodyAlpha), style = Fill)
                    drawPath(path, baseColor, style = Stroke(width = 1.5f))
                }
                EnemyType.VOID_WRAITH -> {
                    // Translucent pulsing corona — wraith-like ethereal
                    val wraithPulse = 1f + 0.12f * sin(timeSec * 4f + enemy.hashCode()).toFloat()
                    drawCircle(
                        color  = baseColor.copy(alpha = 0.18f * bodyAlpha),
                        radius = r * wraithPulse * 1.8f,
                        center = center
                    )
                    drawCircle(
                        color  = baseColor.copy(alpha = 0.55f * bodyAlpha),
                        radius = r * wraithPulse,
                        center = center
                    )
                    drawCircle(
                        color  = Color(0xFFE040FB).copy(alpha = 0.9f * bodyAlpha),
                        radius = r * 0.45f,
                        center = center
                    )
                    drawCircle(
                        color  = baseColor,
                        radius = r * wraithPulse,
                        center = center,
                        style  = Stroke(width = 1.5f)
                    )
                }
            }

            // ── HP bar ────────────────────────────────────────────────────
            val hpPct  = (enemy.hp.toFloat() / enemy.type.baseHp.toFloat()).coerceIn(0f, 1f)
            val barW   = r * 2.4f
            val barX   = sx - barW / 2f
            val barY   = sy - r - 10f
            drawRect(HP_BAR_BG, Offset(barX, barY),     Size(barW,       4f))
            drawRect(HP_BAR_FG, Offset(barX, barY),     Size(barW * hpPct, 4f))
        }
    }
}
