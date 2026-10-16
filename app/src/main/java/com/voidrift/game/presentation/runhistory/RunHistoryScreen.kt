package com.voidrift.game.presentation.runhistory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voidrift.game.data.local.entity.RunRecord
import com.voidrift.game.ui.theme.CrystalBlue
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.VoidSurface
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunHistoryScreen(
    viewModel: RunHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val runs       by viewModel.runs.collectAsState()
    val deepestRun by viewModel.deepestRun.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VoidBackground, VoidCorrupt, VoidBackground)))
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                Text(
                    "RUN HISTORY",
                    color         = NeonPink,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }

            deepestRun?.let { best ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidCorrupt),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 DEEPEST RUN", color = TorchAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Depth ${best.depth}  •  ${best.score} pts", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (runs.isEmpty()) {
                // Empty state with small void swirl animation
                EmptyHistoryView()
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    itemsIndexed(runs) { index, run ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInHorizontally(
                                animationSpec = tween(durationMillis = 300 + index * 50),
                                initialOffsetX = { fullWidth -> fullWidth }
                            ) + fadeIn(animationSpec = tween(300 + index * 50))
                        ) {
                            RunCard(rank = index + 1, run = run)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    var timeSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { frameNanos ->
                timeSec = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val radius = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.6f), Color.Transparent),
                        center = center,
                        radius = radius * (1f + 0.1f * kotlin.math.sin(timeSec * 3f))
                    ),
                    radius = radius,
                    center = center
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "NO RUNS YET. ENTER THE VOID.",
                color         = TextSecondary,
                fontSize      = 14.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun RunCard(rank: Int, run: RunRecord) {
    val date = remember(run.timestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(run.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonCyan, Color.Transparent)
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = VoidSurface),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("#$rank ", color = TextSecondary, fontSize = 12.sp)
                    Text("Depth ${run.depth}", color = NeonPink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text(run.causeOfDeath, color = TextPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                Text(date, color = TextSecondary, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${run.score}", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("pts", color = TextSecondary, fontSize = 10.sp)
                Text("☠ ${run.killCount}  ${run.durationSeconds.toInt()}s", color = CrystalBlue, fontSize = 11.sp)
            }
        }
    }
}
