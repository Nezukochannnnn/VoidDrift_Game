package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voidrift.game.domain.model.GameWorld

/** Renders the in-game HUD: HP bar, torch bar, score, depth, and kill count. */
object HudRenderer {

    private val HUD_BG        = Color(0xCC0D0D1A)
    private val HP_BG         = Color(0xFF3E2723)
    private val HP_FG_HIGH    = Color(0xFF4CAF50)
    private val HP_FG_LOW     = Color(0xFFF44336)
    private val TORCH_BG      = Color(0xFF3E2723)
    private val TORCH_FG      = Color(0xFFFF9800)
    private val LABEL_COLOR   = Color(0xFFB0BEC5)
    private val SCORE_COLOR   = Color(0xFF00E5FF)
    private val DEPTH_COLOR   = Color(0xFFEA80FC)

    fun DrawScope.drawHud(world: GameWorld, measurer: TextMeasurer) {
        // ── HUD background panel ──────────────────────────────────────────
        drawRoundRect(
            color        = HUD_BG,
            topLeft      = Offset(12f, 12f),
            size         = Size(230f, 80f),
            cornerRadius = CornerRadius(10f)
        )

        // ── HP bar ───────────────────────────────────────────────────────
        val hpPct     = (world.player.hp / 100f).coerceIn(0f, 1f)
        val hpFgColor = if (hpPct > 0.4f) HP_FG_HIGH else HP_FG_LOW
        drawRoundRect(HP_BG,   Offset(20f, 22f), Size(200f, 14f), CornerRadius(4f))
        drawRoundRect(hpFgColor, Offset(20f, 22f), Size(200f * hpPct, 14f), CornerRadius(4f))

        // ── Torch bar ────────────────────────────────────────────────────
        val torchPct = (world.player.torchRadius / 300f).coerceIn(0f, 1f)
        drawRoundRect(TORCH_BG,   Offset(20f, 44f), Size(200f, 14f), CornerRadius(4f))
        drawRoundRect(TORCH_FG, Offset(20f, 44f), Size(200f * torchPct, 14f), CornerRadius(4f))

        // ── Score ─────────────────────────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "SCORE  ${world.score}",
            topLeft      = Offset(20f, 66f),
            style        = TextStyle(color = SCORE_COLOR, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )

        // ── Depth & kill count ────────────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "DEPTH ${world.runDepth}   ☠ ${world.killCount}",
            topLeft      = Offset(size.width - 140f, 14f),
            style        = TextStyle(color = DEPTH_COLOR, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
    }
}
