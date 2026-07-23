package com.voidrift.game.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.DungeonRoom.Companion.TILE_SIZE
import com.voidrift.game.presentation.game.renderer.DungeonRenderer.drawDungeon
import com.voidrift.game.presentation.game.renderer.EnemyRenderer.drawEnemies
import com.voidrift.game.presentation.game.renderer.HudRenderer.drawHud
import com.voidrift.game.presentation.game.renderer.PlayerRenderer.drawPlayer
import kotlin.math.max

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val world   by viewModel.world.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState is GameUiState.Idle) viewModel.startNewRun()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05050F))
    ) {
        when (val state = uiState) {
            is GameUiState.Loading -> LoadingOverlay()

            is GameUiState.Playing, is GameUiState.Paused -> {
                GameCanvas(
                    world    = world,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                viewModel.onMove(
                                    Vector2D(dragAmount.x / 200f, dragAmount.y / 200f)
                                )
                            }
                        }
                )

                if (uiState is GameUiState.Paused) {
                    PauseOverlay(
                        onResume  = { viewModel.onResume() },
                        onRestart = { viewModel.onRestart() },
                        onHome    = onNavigateHome
                    )
                }
            }

            is GameUiState.GameOver -> {
                GameCanvas(world = world, modifier = Modifier.fillMaxSize())
                GameOverOverlay(
                    state     = state,
                    onRestart = { viewModel.onRestart() },
                    onHome    = onNavigateHome
                )
            }

            is GameUiState.RoomCleared -> {
                GameCanvas(world = world, modifier = Modifier.fillMaxSize())
                RoomClearedOverlay(
                    world  = world,
                    onNext = { viewModel.startNewRun() }
                )
            }

            else -> Unit
        }
    }
}

// ── Game Canvas ───────────────────────────────────────────────────────────────

@Composable
private fun GameCanvas(
    world: com.voidrift.game.domain.model.GameWorld,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var elapsedSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(world.elapsedSeconds) { elapsedSec = world.elapsedSeconds }

    Canvas(modifier = modifier) {
        // Camera: center on player
        val camX = world.player.position.x - size.width  / 2f
        val camY = world.player.position.y - size.height / 2f

        drawDungeon(world.dungeon.tiles, camX, camY)
        drawEnemies(world.enemies, camX, camY)
        drawPlayer(world.player, camX, camY, elapsedSec)
        drawHud(world, textMeasurer)
    }
}

// ── Overlay composables ───────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Generating dungeon…", color = Color(0xFF00E5FF), fontSize = 20.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onHome: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000011)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", color = Color(0xFFEA80FC), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            VoidButton("RESUME",  onResume)
            VoidButton("RESTART", onRestart)
            VoidButton("HOME",    onHome)
        }
    }
}

@Composable
private fun GameOverOverlay(
    state: GameUiState.GameOver,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDD05000A)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VOID CLAIMS YOU", color = Color(0xFFFF1744), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(state.causeOfDeath,  color = Color(0xFF9E9E9E), fontSize = 13.sp)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            Text("Score: ${state.world.score}", color = Color(0xFF00E5FF), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Depth: ${state.world.runDepth}   ☠ ${state.world.killCount}", color = Color(0xFFEA80FC), fontSize = 16.sp)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))
            VoidButton("TRY AGAIN", onRestart)
            VoidButton("HOME",      onHome)
        }
    }
}

@Composable
private fun RoomClearedOverlay(world: com.voidrift.game.domain.model.GameWorld, onNext: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000020)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VOID CLEARED", color = Color(0xFF39FF14), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Score: ${world.score}", color = Color(0xFF00E5FF), fontSize = 20.sp)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))
            VoidButton("NEXT DEPTH", onNext)
        }
    }
}

@Composable
private fun VoidButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0030))
    ) {
        Text(label, color = Color(0xFFEA80FC), fontWeight = FontWeight.Bold)
    }
}
