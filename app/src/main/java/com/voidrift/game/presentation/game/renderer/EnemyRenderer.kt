package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.voidrift.game.domain.model.AiState
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.EnemyType

/** Renders each enemy with a type-specific colour, health bar, and AI state indicator. */
object EnemyRenderer {

    private val CRAWLER_COLOR     = Color(0xFFFF5252)
    private val LURKER_COLOR      = Color(0xFFFF6D00)
    private val WRAITH_COLOR      = Color(0xFFEA80FC)
    private val HP_BAR_BG         = Color(0xFF3E2723)
    private val HP_BAR_FG         = Color(0xFFE53935)
    private val ATTACK_RING       = Color(0xFFFF1744)

    fun DrawScope.drawEnemies(enemies: List<Enemy>, camX: Float, camY: Float) {
        for (enemy in enemies) {
            if (!enemy.isAlive) continue
            val sx = enemy.position.x - camX
            val sy = enemy.position.y - camY

            // Cull
            val r = enemy.type.halfSize
            if (sx < -r * 2 || sx > size.width + r * 2) continue
            if (sy < -r * 2 || sy > size.height + r * 2) continue

            val baseColor = when (enemy.type) {
                EnemyType.CRAWLER     -> CRAWLER_COLOR
                EnemyType.LURKER      -> LURKER_COLOR
                EnemyType.VOID_WRAITH -> WRAITH_COLOR
            }

            // Attack pulsing ring
            if (enemy.aiState == AiState.ATTACKING) {
                drawCircle(
                    color  = ATTACK_RING.copy(alpha = 0.35f),
                    radius = r * 1.8f,
                    center = Offset(sx, sy)
                )
            }

            // Body
            when (enemy.type) {
                EnemyType.CRAWLER, EnemyType.LURKER -> drawRect(
                    color   = baseColor,
                    topLeft = Offset(sx - r, sy - r),
                    size    = Size(r * 2f, r * 2f),
                    style   = Fill
                )
                EnemyType.VOID_WRAITH -> drawCircle(
                    color  = baseColor,
                    radius = r,
                    center = Offset(sx, sy)
                )
            }
            // Body outline
            drawCircle(
                color  = baseColor.copy(alpha = 0.6f),
                radius = r,
                center = Offset(sx, sy),
                style  = Stroke(width = 1.5f)
            )

            // HP bar
            val barW = r * 2.2f
            val barH = 4f
            val barX = sx - barW / 2f
            val barY = sy - r - 8f
            val hpPct = enemy.hp.toFloat() / enemy.type.baseHp.toFloat()
            drawRect(HP_BAR_BG, Offset(barX, barY), Size(barW, barH))
            drawRect(HP_BAR_FG, Offset(barX, barY), Size(barW * hpPct, barH))
        }
    }
}
