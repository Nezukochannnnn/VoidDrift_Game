package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.voidrift.game.domain.model.Particle
import com.voidrift.game.domain.model.ParticleType
import com.voidrift.game.ui.theme.CrystalBlue
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidCorruptGlow

/**
 * Layer 3 — Particle Renderer
 * Renders circle particles with velocity, lifetime, and alpha fade.
 */
object ParticleRenderer {

    fun DrawScope.drawParticles(
        particles: List<Particle>,
        camX: Float,
        camY: Float
    ) {
        for (p in particles) {
            if (!p.isAlive) continue

            val px = p.position.x - camX
            val py = p.position.y - camY

            if (px < -20f || px > size.width + 20f) continue
            if (py < -20f || py > size.height + 20f) continue

            val color = when (p.type) {
                ParticleType.CRYSTAL_GLINT -> CrystalBlue          // Ice-blue burst
                ParticleType.BLOOD_SPLATTER -> NeonPink            // Neon pink explosion
                ParticleType.VOID_SPARK,
                ParticleType.VOID_CONSUME  -> VoidCorruptGlow       // Slow purple wisps
                ParticleType.TORCH_EMBER   -> TorchAmber
            }

            drawCircle(
                color = color.copy(alpha = p.alpha),
                radius = 4f * p.scale,
                center = Offset(px, py),
                blendMode = BlendMode.Screen
            )
        }
    }
}
