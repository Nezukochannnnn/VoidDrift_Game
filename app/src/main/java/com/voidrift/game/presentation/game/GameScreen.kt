package com.voidrift.game.presentation.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.presentation.game.renderer.DungeonRenderer.drawDungeon
import com.voidrift.game.presentation.game.renderer.EnemyRenderer.drawEnemies
import com.voidrift.game.presentation.game.renderer.HudRenderer.drawHud
import com.voidrift.game.presentation.game.renderer.PlayerRenderer.drawPlayer
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonCyanContainer
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.NeonPinkContainer
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TextSecondary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt
import com.voidrift.game.ui.theme.voidColors

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
            .background(VoidBackground)
    ) {
        when (val state = uiState) {
            is GameUiState.Loading -> LoadingOverlay()

            is GameUiState.Playing, is GameUiState.Paused -> {
                GameCanvas(
                    world = world,
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
                GameOverOverlay(state = state, onRestart = { viewModel.onRestart() }, onHome = onNavigateHome)
            }

            is GameUiState.RoomCleared -> {
                GameCanvas(world = world, modifier = Modifier.fillMaxSize())
                RoomClearedOverlay(world = world, onNext = { viewModel.startNewRun() })
            }

            else -> Unit
        }
    }
}

// ── Game Canvas ───────────────────────────────────────────────────────────────

@Composable
private fun GameCanvas(world: GameWorld, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val camX = world.player.position.x - size.width  / 2f
        val camY = world.player.position.y - size.height / 2f

        drawDungeon(world.dungeon.tiles, camX, camY)
        drawEnemies(world.enemies, camX, camY, world.elapsedSeconds)
        drawPlayer(world.player, camX, camY, world.elapsedSeconds)
        drawHud(world, textMeasurer)
    }
}

// ── Overlays ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("◈", color = NeonCyan, fontSize = 36.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "GENERATING VOID…",
                color         = NeonCyan,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onHome: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDD000010)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "PAUSED",
                color         = NeonCyan,
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 8.sp
            )
            Spacer(Modifier.height(28.dp))
            VoidActionButton("RESUME",  NeonCyan,  onResume)
            VoidActionButton("RESTART", TorchAmber, onRestart)
            VoidActionButton("HOME",    TextSecondary, onHome)
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
            .background(
                Brush.radialGradient(
                    listOf(NeonPinkContainer.copy(alpha = 0.95f), VoidBackground.copy(alpha = 0.98f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VOID CLAIMS YOU",
                color         = NeonPink,
                fontSize      = 30.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 4.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(state.causeOfDeath, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            // Stats
            Text("◈ ${state.world.score}", color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(
                "DEPTH ${state.world.runDepth}  •  ☠ ${state.world.killCount}",
                color = TextSecondary, fontSize = 14.sp, letterSpacing = 2.sp
            )
            Spacer(Modifier.height(28.dp))
            VoidActionButton("TRY AGAIN", NeonCyan,  onRestart)
            VoidActionButton("HOME",      TextSecondary, onHome)
        }
    }
}

@Composable
private fun RoomClearedOverlay(world: GameWorld, onNext: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(NeonCyanContainer.copy(alpha = 0.9f), VoidCorrupt.copy(alpha = 0.95f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VOID CLEARED",
                color         = NeonCyan,
                fontSize      = 30.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(12.dp))
            Text("◈ ${world.score}", color = NeonCyan, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("☠ ${world.killCount} slain", color = NeonPink, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))
            VoidActionButton("DESCEND DEEPER", NeonCyan, onNext)
        }
    }
}

@Composable
private fun VoidActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.padding(vertical = 5.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text          = label,
            color         = color,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontSize      = 14.sp
        )
    }
}
