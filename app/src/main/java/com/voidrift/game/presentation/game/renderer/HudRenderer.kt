package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.presentation.game.renderer.RenderConstants.DEPTH_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.HP_HIGH
import com.voidrift.game.presentation.game.renderer.RenderConstants.HP_LOW
import com.voidrift.game.presentation.game.renderer.RenderConstants.HUD_PANEL_BG
import com.voidrift.game.presentation.game.renderer.RenderConstants.SCORE_COLOR
import com.voidrift.game.presentation.game.renderer.RenderConstants.TORCH_BAR_FG
import com.voidrift.game.ui.theme.TextSecondary

/**
 * Renders the heads-up display (HUD) over the game canvas.
 *
 * HUD contents:
 *  ┌─────────────────────────────────────────┐
 *  │ ♥ [██████████░░░]  HP  30/100           │
 *  │ 🔥 [████████░░░░]  TORCH  240           │
 *  └─────────────────────────────────────────┘
 *  SCORE 12 500                DEPTH 3  ☠ 7
 *
 * Colors:
 *  HP bar  → NeonCyan (#00FFD1) high / NeonPink (#FF4DA6) critical
 *  Torch   → TorchAmber (#FFB300)
 *  Score   → NeonCyan
 *  Depth   → CrystalBlue (#7DF9FF)
 */
object HudRenderer {

    private val BAR_BG = Color(0xFF0A0A14)

    fun DrawScope.drawHud(world: GameWorld, measurer: TextMeasurer) {
        val hpPct    = (world.player.hp.toFloat() / 100f).coerceIn(0f, 1f)
        val torchPct = (world.player.torchRadius / 300f).coerceIn(0f, 1f)
        val hpColor  = if (hpPct > 0.35f) HP_HIGH else HP_LOW

        // ── Top-left panel background ────────────────────────────────────
        drawRoundRect(
            color        = HUD_PANEL_BG,
            topLeft      = Offset(10f, 10f),
            size         = Size(240f, 72f),
            cornerRadius = CornerRadius(10f)
        )

        // ── HP bar ────────────────────────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "HP",
            topLeft      = Offset(18f, 17f),
            style        = TextStyle(color = hpColor, fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        )
        drawRoundRect(BAR_BG,   Offset(40f, 18f), Size(196f, 12f), CornerRadius(4f))
        drawRoundRect(hpColor,  Offset(40f, 18f), Size(196f * hpPct, 12f), CornerRadius(4f))

        // HP text overlay
        drawText(
            textMeasurer = measurer,
            text         = "${world.player.hp}/100",
            topLeft      = Offset(40f, 32f),
            style        = TextStyle(color = TextSecondary, fontSize = 8.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
        )

        // ── Torch bar ─────────────────────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "🕯",
            topLeft      = Offset(16f, 44f),
            style        = TextStyle(color = TORCH_BAR_FG, fontSize = 10.sp)
        )
        drawRoundRect(BAR_BG,       Offset(40f, 45f), Size(196f, 12f), CornerRadius(4f))
        drawRoundRect(TORCH_BAR_FG, Offset(40f, 45f), Size(196f * torchPct, 12f), CornerRadius(4f))

        // Torch value
        drawText(
            textMeasurer = measurer,
            text         = "${world.player.torchRadius.toInt()}",
            topLeft      = Offset(40f, 59f),
            style        = TextStyle(color = TextSecondary, fontSize = 8.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
        )

        // ── Score — bottom-left ───────────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "◈ ${world.score}",
            topLeft      = Offset(12f, 90f),
            style        = TextStyle(color = SCORE_COLOR, fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        )

        // ── Depth + kills — top-right ─────────────────────────────────────
        drawText(
            textMeasurer = measurer,
            text         = "DEPTH ${world.runDepth}  ☠ ${world.killCount}",
            topLeft      = Offset(size.width - 160f, 14f),
            style        = TextStyle(color = DEPTH_COLOR, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )

        // ── Void warning when torch < 20% ────────────────────────────────
        if (torchPct < 0.2f) {
            drawText(
                textMeasurer = measurer,
                text         = "⚠ TORCH FADING",
                topLeft      = Offset(size.width / 2f - 60f, size.height - 60f),
                style        = TextStyle(color = HP_LOW, fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            )
        }
    }
}
