package com.voidrift.game.presentation.game.renderer

import android.graphics.Paint
import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.voidrift.game.core.shaders.TorchShader
import com.voidrift.game.domain.model.Player
import com.voidrift.game.ui.theme.TorchAmber

/**
 * Layer 5 — Torch Lighting Mask Renderer (SIGNATURE EFFECT)
 * Applies torch lighting mask over all game layers using saveLayer + BlendMode.DstIn / BlendMode.Modulate.
 * Uses AGSL TorchShader on API 33+, RadialGradient fallback on API 29-32.
 */
object TorchMaskRenderer {

    fun DrawScope.drawTorchMask(
        player: Player,
        camX: Float,
        camY: Float,
        timeSec: Float
    ) {
        val playerScreenX = player.position.x - camX
        val playerScreenY = player.position.y - camY
        val radius = player.torchRadius

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && TorchShader.isSupported) {
            val shader = TorchShader.create()
            TorchShader.setUniforms(shader, playerScreenX, playerScreenY, radius, timeSec)
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    setShader(shader)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        blendMode = android.graphics.BlendMode.DST_IN
                    }
                }
                canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
            }
        } else {
            // Fallback for API 29-32
            val flicker = 0.93f + 0.07f * kotlin.math.sin(timeSec * 19.3f)
            val flickeredRadius = (radius * flicker).coerceAtLeast(10f)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF176).copy(alpha = 0.95f),
                        TorchAmber.copy(alpha = 0.8f),
                        TorchAmber.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(playerScreenX, playerScreenY),
                    radius = flickeredRadius
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }
}
