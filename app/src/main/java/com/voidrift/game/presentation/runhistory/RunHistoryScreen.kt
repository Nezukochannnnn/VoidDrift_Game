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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.data.local.entity.RunRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BG_TOP  = Color(0xFF05050F)
private val BG_BOT  = Color(0xFF0A0015)
private val CYAN    = Color(0xFF00E5FF)
private val PURPLE  = Color(0xFFEA80FC)
private val ORANGE  = Color(0xFFFF9800)
private val CARD_BG = Color(0xFF0F0F22)
private val GRAY    = Color(0xFF607D8B)

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
            .background(Brush.verticalGradient(listOf(BG_TOP, BG_BOT)))
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = CYAN)
                }
                Text(
                    "RUN HISTORY",
                    color = PURPLE, fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }

            deepestRun?.let { best ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0030)),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 DEEPEST RUN", color = ORANGE,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Depth ${best.depth}  •  ${best.score} pts",
                            color = CYAN, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (runs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No runs yet. Enter the void.", color = GRAY, fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        colors   = CardDefaults.cardColors(containerColor = CARD_BG),
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
                    Text("#$rank ", color = GRAY, fontSize = 12.sp)
                    Text("Depth ${run.depth}", color = PURPLE,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text(run.causeOfDeath, color = Color(0xFF9E9E9E), fontSize = 11.sp)
                Text(date, color = GRAY, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${run.score}", color = CYAN,
                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("pts", color = GRAY, fontSize = 10.sp)
                Text("☠ ${run.killCount}  ${run.durationSeconds.toInt()}s",
                    color = ORANGE, fontSize = 11.sp)
            }
        }
    }
}
