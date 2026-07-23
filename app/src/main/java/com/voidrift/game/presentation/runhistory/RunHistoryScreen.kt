package com.voidrift.game.presentation.runhistory

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No runs yet. Enter the void.",
                        color     = TextSecondary,
                        fontSize  = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    itemsIndexed(runs) { index, run ->
                        RunCard(rank = index + 1, run = run)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RunCard(rank: Int, run: RunRecord) {
    val date = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        .format(Date(run.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = VoidSurface),
        shape    = RoundedCornerShape(12.dp)
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
