package com.voidrift.game.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonCyanContainer
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurface
import com.voidrift.game.ui.theme.VoidSurfaceVariant
import com.voidrift.game.ui.theme.voidColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartGame: () -> Unit = {},
    onRunHistory: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val state    by viewModel.state.collectAsState()
    val extended =  MaterialTheme.voidColors

    val pulse by rememberInfiniteTransition(label = "title_pulse").animateFloat(
        initialValue  = 0.82f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label         = "title_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VoidBackground, VoidCorrupt, VoidBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo wordmark ─────────────────────────────────────────────
            Text(
                text          = "✦ VOIDRIFT ✦",
                color         = extended.neonPink.copy(alpha = pulse),
                fontSize      = 44.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 7.sp,
                textAlign     = TextAlign.Center
            )
            Text(
                text          = "THE VOID CLAIMS ALL",
                color         = NeonCyan.copy(alpha = 0.65f),
                fontSize      = 12.sp,
                letterSpacing = 5.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ── Player profile card ───────────────────────────────────────
            state.profile?.let { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = VoidSurface),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            "⚔  ${profile.playerName}",
                            color      = TorchAmber,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatChip("RUNS",       profile.totalRuns.toString())
                            StatChip("DEPTH",      profile.bestDepth.toString())
                            StatChip("TOP SCORE",  profile.bestScore.toString())
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Primary CTA ───────────────────────────────────────────────
            Button(
                onClick  = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyanContainer
                )
            ) {
                Text(
                    text          = "ENTER THE VOID",
                    color         = NeonCyan,
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onRunHistory,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, NeonCyan.copy(alpha = 0.4f)
                    )
                ) { Text("HISTORY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

                OutlinedButton(
                    onClick  = onSettings,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TorchAmber),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, TorchAmber.copy(alpha = 0.4f)
                    )
                ) { Text("SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
            }

            Spacer(Modifier.height(24.dp))

            state.bestRun?.let { run ->
                Text(
                    "DEEPEST VOID: Depth ${run.depth}  •  Score ${run.score}",
                    color     = extended.neonPink.copy(alpha = 0.5f),
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color      = NeonCyan,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            label,
            color         = TextSecondary,
            fontSize      = 9.sp,
            letterSpacing = 1.5.sp,
            fontWeight    = FontWeight.Medium
        )
    }
}
