package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.graphics.Color
import com.voidrift.game.ui.theme.CrystalBlue
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidCorruptGlow
import com.voidrift.game.ui.theme.VoidSurface

/**
 * Shared render constants and brand colors for all Canvas DrawScope renderers.
 * Using the VoidDrift palette tokens directly — no magic hex strings in renderers.
 */
object RenderConstants {

    // ── Tile colors ──────────────────────────────────────────────────────────
    val FLOOR_COLOR     = VoidSurface                       // #12121A dark slate
    val WALL_COLOR      = VoidBackground                    // #0A0A0F near-black
    val VOID_TILE_COLOR = VoidCorrupt                       // #1A0030 deep purple-black
    val CRYSTAL_COLOR   = CrystalBlue                      // #7DF9FF ice blue
    val EXIT_COLOR      = NeonCyan                         // #00FFD1 neon cyan
    val GRID_LINE_COLOR = Color(0xFF1E1E2E)

    // ── Player colors ────────────────────────────────────────────────────────
    val PLAYER_FILL     = Color(0xFFE8E8FF)                // TextPrimary soft white
    val PLAYER_STROKE   = NeonCyan                         // #00FFD1
    val TORCH_OUTER     = TorchAmber                       // #FFB300
    val TORCH_INNER     = Color(0xFFFFF176)

    // ── Enemy colors ─────────────────────────────────────────────────────────
    val CRAWLER_COLOR   = NeonPink                         // #FF4DA6
    val LURKER_COLOR    = Color(0xFFFF7043)                // Hot orange
    val WRAITH_COLOR    = VoidCorruptGlow                  // #9C27B0 purple glow
    val HP_BAR_BG       = Color(0xFF1A0010)
    val HP_BAR_FG       = NeonPink
    val ATTACK_RING     = NeonPink

    // ── HUD colors ───────────────────────────────────────────────────────────
    val HUD_PANEL_BG    = Color(0xCC0A0A0F)
    val HP_HIGH         = NeonCyan
    val HP_LOW          = NeonPink
    val TORCH_BAR_FG    = TorchAmber
    val SCORE_COLOR     = NeonCyan
    val DEPTH_COLOR     = CrystalBlue

    // ── Particle colors (for legend — actual draw in Canvas) ─────────────────
    val VOID_SPARK_COLOR    = VoidCorruptGlow
    val TORCH_EMBER_COLOR   = TorchAmber
    val CRYSTAL_GLINT_COLOR = CrystalBlue
    val BLOOD_COLOR         = NeonPink

    // ── Misc ─────────────────────────────────────────────────────────────────
    const val VOID_ALPHA         = 0.78f
    const val PARTICLE_RADIUS    = 4f
    const val TILE_SIZE          = 64f
}
