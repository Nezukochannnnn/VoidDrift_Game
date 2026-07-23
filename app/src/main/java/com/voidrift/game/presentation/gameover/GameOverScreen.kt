package com.voidrift.game.presentation.gameover

import android.graphics.Paint
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidrift.game.core.shaders.ChromaticAberrationShader
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurface
import kotlinx.coroutines.withFrameNanos

@Composable
fun GameOverScreen(
    world: GameWorld,
    causeOfDeath: String,
    onDriftAgain: () -> Unit,
    onViewHistory: () -> Unit
) {
    var timeSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameNanos ->
                timeSec = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBackground),
        contentAlignment = Alignment.Center
    ) {
        // ── Void Swirl Edge Vignette Canvas ──────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * (0.9f - 0.2f * (timeSec * 0.1f).coerceIn(0f, 0.4f))
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        VoidCorrupt.copy(alpha = 0.6f),
                        VoidBackground
                    ),
                    center = center,
                    radius = radius
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Glitch "VOID CLAIMS ALL" Title ─────────────────────────────────
            GlitchTitle(text = "VOID CLAIMS ALL", timeSec = timeSec)

            Spacer(Modifier.height(24.dp))

            // ── Run Stats Card ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "RUN ANALYSIS",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cause: $causeOfDeath",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("DEPTH", world.runDepth.toString(), NeonPink)
                        StatItem("KILLS", world.killCount.toString(), TorchAmber)
                        StatItem("TIME", "${world.elapsedSeconds.toInt()}s", NeonCyan)
                        StatItem("SCORE", world.score.toString(), NeonCyan)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Action Buttons ───────────────────────────────────────────────
            Button(
                onClick = onDriftAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0030))
            ) {
                Text(
                    "DRIFT AGAIN",
                    color = NeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VoidSurface)
            ) {
                Text(
                    "VIEW HISTORY",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun GlitchTitle(text: String, timeSec: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ChromaticAberrationShader.isSupported) {
        val shader = remember { ChromaticAberrationShader.create() }
        Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            ChromaticAberrationShader.setUniforms(shader, size.width, size.height, timeSec)
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.parseColor("#FF4DA6")
                    textSize = 68f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    setShader(shader)
                }
                canvas.nativeCanvas.drawText(text, size.width / 2f, size.height / 2f + 20f, paint)
            }
        }
    } else {
        // Color split fallback for API 29-32
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = NeonCyan.copy(alpha = 0.7f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 3.dp)
            )
            Text(
                text = text,
                color = NeonPink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}
