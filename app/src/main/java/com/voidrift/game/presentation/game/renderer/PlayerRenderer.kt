package com.voidrift.game.presentation.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.voidrift.game.domain.model.Player

/** Renders the player as an angular arrowhead with a torch-glow halo. */
object PlayerRenderer {

    private val SHIP_FILL    = Color(0xFFE0E0FF)
    private val SHIP_STROKE  = Color(0xFF7E57C2)
    private val TORCH_RING   = Color(0xFFFF9800)
    private val TORCH_INNER  = Color(0xFFFFF176)
    private val INVULN_COLOR = Color(0xFF40C4FF)

    fun DrawScope.drawPlayer(player: Player, camX: Float, camY: Float, timeSec: Float) {
        val sx = player.position.x - camX
        val sy = player.position.y - camY

        // Torch glow ring — pulsating
        val pulseScale = 1f + 0.06f * kotlin.math.sin(timeSec * 6f)
        drawCircle(
            color  = TORCH_RING.copy(alpha = 0.12f),
            radius = player.torchRadius * pulseScale,
            center = Offset(sx, sy)
        )
        // Inner warm glow
        drawCircle(
            color  = TORCH_INNER.copy(alpha = 0.22f),
            radius = 50f * pulseScale,
            center = Offset(sx, sy)
        )

        // Player body — arrowhead pointing UP, rotated
        rotate(degrees = 0f, pivot = Offset(sx, sy)) {
            val path = Path().apply {
                moveTo(sx,        sy - 22f)   // tip
                lineTo(sx - 14f,  sy + 14f)   // bottom-left
                lineTo(sx,        sy + 6f)    // inner notch
                lineTo(sx + 14f,  sy + 14f)   // bottom-right
                close()
            }
            drawPath(path, SHIP_FILL,   style = Fill)
            drawPath(path, SHIP_STROKE, style = Stroke(width = 2f))
        }
    }
}
