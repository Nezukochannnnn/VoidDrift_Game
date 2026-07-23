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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val BG_TOP    = Color(0xFF05050F)
private val BG_MID    = Color(0xFF0A0020)
private val BG_BOT    = Color(0xFF07000D)
private val CYAN      = Color(0xFF00E5FF)
private val PURPLE    = Color(0xFFEA80FC)
private val ORANGE    = Color(0xFFFF9800)
private val CARD_BG   = Color(0xFF0F0F22)
private val VOID_RED  = Color(0xFFFF1744)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartGame: () -> Unit = {},
    onRunHistory: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val infiniteAnim = rememberInfiniteTransition(label = "title_pulse")
    val titlePulse by infiniteAnim.animateFloat(
        initialValue = 0.85f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "title_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BG_TOP, BG_MID, BG_BOT))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo ──────────────────────────────────────────────────────
            Text(
                text       = "✦ VOIDRIFT ✦",
                color      = PURPLE.copy(alpha = titlePulse),
                fontSize   = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                textAlign  = TextAlign.Center
            )
            Text(
                text       = "THE VOID CLAIMS ALL",
                color      = CYAN.copy(alpha = 0.7f),
                fontSize   = 13.sp,
                letterSpacing = 4.sp,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // ── Player stats card ─────────────────────────────────────────
            state.profile?.let { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = CARD_BG),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("⚔  ${profile.playerName}", color = ORANGE,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem("RUNS",      profile.totalRuns.toString())
                            StatItem("BEST DEPTH", profile.bestDepth.toString())
                            StatItem("TOP SCORE",  profile.bestScore.toString())
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── CTA buttons ───────────────────────────────────────────────
            Button(
                onClick  = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A0040)
                )
            ) {
                Text(
                    text       = "ENTER THE VOID",
                    color      = PURPLE,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick  = onRunHistory,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = CYAN)
                ) { Text("HISTORY", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick  = onSettings,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = ORANGE)
                ) { Text("SETTINGS", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(24.dp))

            // ── Best run line ─────────────────────────────────────────────
            state.bestRun?.let { run ->
                Text(
                    "DEEPEST RUN: Depth ${run.depth}  •  Score ${run.score}",
                    color    = VOID_RED.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = CYAN, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color(0xFF607D8B), fontSize = 9.sp, letterSpacing = 1.sp)
    }
}
