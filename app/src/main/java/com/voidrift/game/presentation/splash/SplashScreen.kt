package com.voidrift.game.presentation.splash

import android.graphics.RenderEffect
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voidrift.game.core.shaders.VoidSwirlShader
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.VoidBackground
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    var timeSec by remember { mutableFloatStateOf(0f) }

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            VoidSwirlShader.create()
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1000))
        delay(1200)
        alphaAnim.animateTo(0f, animationSpec = tween(500))
        onSplashFinished()
    }

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
            .background(VoidBackground)
            .graphicsLayer { alpha = alphaAnim.value },
        contentAlignment = Alignment.Center
    ) {
        // ── Background Void Swirl Canvas ─────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
                VoidSwirlShader.setUniforms(shader, size.width, size.height, timeSec)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        setShader(shader)
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
                }
            } else {
                // Radial gradient fallback for API 29-32
                val radius = size.minDimension * 0.8f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6A0DAD).copy(alpha = 0.5f),
                            Color(0xFF1A0030).copy(alpha = 0.8f),
                            Color.Black
                        ),
                        center = center,
                        radius = radius * (1f + 0.1f * kotlin.math.sin(timeSec * 2f))
                    ),
                    radius = radius,
                    center = center
                )
            }
        }

        // ── Neon Cyan Glowing Text ───────────────────────────────────────────
        Text(
            text          = "VOIDRIFT",
            color         = NeonCyan,
            fontSize      = 52.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = 8.sp,
            modifier      = Modifier.graphicsLayer {
                // blendMode = BlendMode.Screen
                shadowElevation = 24f
            }
        )
    }
}
