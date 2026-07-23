package com.voidrift.game.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurface
import kotlinx.coroutines.withFrameNanos
import kotlin.random.Random

private data class FloatingParticle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartGame: () -> Unit = {},
    onRunHistory: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Pulsing logo glow animation
    val logoGlow = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        logoGlow.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Parallax scrolling particles frame tick
    var timeSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameNanos ->
                timeSec = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    val particles = remember {
        val rng = Random(42)
        val colors = listOf(NeonCyan, NeonPink, TorchAmber, Color(0xFF7DF9FF))
        List(50) {
            FloatingParticle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                radius = rng.nextFloat() * 3f + 1.5f,
                speed = rng.nextFloat() * 0.08f + 0.03f,
                alpha = rng.nextFloat() * 0.6f + 0.2f,
                color = colors[rng.nextInt(colors.size)]
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VoidBackground, VoidCorrupt, VoidBackground)
                )
            )
    ) {
        // ── 50 Parallax Upward Drifting Particles Canvas ─────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (p in particles) {
                val currentY = ((p.y - timeSec * p.speed) % 1.0f + 1.0f) % 1.0f
                val px = p.x * size.width
                val py = currentY * size.height
                drawCircle(
                    color  = p.color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Pulsing Logo ─────────────────────────────────────────────
            Text(
                text          = "✦ VOIDRIFT ✦",
                color         = NeonPink.copy(alpha = logoGlow.value),
                fontSize      = 46.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 7.sp,
                textAlign     = TextAlign.Center
            )
            Text(
                text          = "THE VOID CLAIMS ALL",
                color         = NeonCyan.copy(alpha = 0.7f),
                fontSize      = 12.sp,
                letterSpacing = 5.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Player Name Display from DataStore / Profile ──────────────
            state.profile?.let { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = VoidSurface),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "⚔  ${profile.playerName}",
                            color      = TorchAmber,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatChip("RUNS",       profile.totalRuns.toString())
                            StatChip("BEST DEPTH", profile.bestDepth.toString())
                            StatChip("TOP SCORE",  profile.bestScore.toString())
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Three Animated Menu Buttons (Press scale to 0.95f) ───────
            AnimatedNeonButton(
                label        = "ENTER THE VOID",
                neonColor    = NeonCyan,
                onClick      = onStartGame,
                modifier     = Modifier.fillMaxWidth().height(60.dp)
            )

            Spacer(Modifier.height(12.dp))

            AnimatedNeonButton(
                label        = "RUN HISTORY",
                neonColor    = NeonPink,
                onClick      = onRunHistory,
                modifier     = Modifier.fillMaxWidth().height(52.dp)
            )

            Spacer(Modifier.height(12.dp))

            AnimatedNeonButton(
                label        = "SETTINGS",
                neonColor    = TorchAmber,
                onClick      = onSettings,
                modifier     = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

@Composable
private fun AnimatedNeonButton(
    label: String,
    neonColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(120),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(neonColor.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp))
            .border(1.5.dp, neonColor.copy(alpha = if (isPressed) 1.0f else 0.5f), shape = RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text          = label,
            color         = neonColor,
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.2.sp)
    }
}
