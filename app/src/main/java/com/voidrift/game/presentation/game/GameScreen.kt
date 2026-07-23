package com.voidrift.game.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.presentation.game.renderer.DungeonRenderer.drawDungeon
import com.voidrift.game.presentation.game.renderer.EnemyRenderer.drawEnemies
import com.voidrift.game.presentation.game.renderer.ParticleRenderer.drawParticles
import com.voidrift.game.presentation.game.renderer.PlayerRenderer.drawPlayer
import com.voidrift.game.presentation.game.renderer.TorchMaskRenderer.drawTorchMask
import com.voidrift.game.presentation.gameover.GameOverScreen
import com.voidrift.game.ui.theme.NeonCyan
import com.voidrift.game.ui.theme.NeonPink
import com.voidrift.game.ui.theme.TextPrimary
import com.voidrift.game.ui.theme.TorchAmber
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidCorrupt

private data class FloatingScoreText(
    val id: Long,
    val text: String,
    val offsetY: Animatable<Float, *>
)

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val world   by viewModel.world.collectAsState()

    val floatingScores = remember { mutableStateListOf<FloatingScoreText>() }
    var prevScore by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    LaunchedEffect(world.score) {
        if (prevScore > 0L && world.score > prevScore) {
            val delta = world.score - prevScore
            val item = FloatingScoreText(
                id = System.currentTimeMillis(),
                text = "+$delta",
                offsetY = Animatable(0f)
            )
            floatingScores.add(item)
            item.offsetY.animateTo(-60f, animationSpec = tween(800))
            floatingScores.remove(item)
        }
        prevScore = world.score
    }

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
                // ── Pure Canvas Game Area (Layers 1 to 5) ─────────────────────
                // Using graphicsLayer(alpha = 0.99f) to force offscreen compositing for Torch Mask BlendMode
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.99f)
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                viewModel.onMove(
                                    Vector2D(dragAmount.x / 200f, dragAmount.y / 200f)
                                )
                            }
                        }
                ) {
                    val camX = world.player.position.x - size.width / 2f
                    val camY = world.player.position.y - size.height / 2f

                    // Layer 1 — Dungeon Base
                    drawDungeon(world.dungeon.tiles, camX, camY, world.elapsedSeconds)

                    // Layer 2 — Enemies
                    drawEnemies(world.enemies, world.player, camX, camY, world.elapsedSeconds)

                    // Layer 3 — Particles
                    drawParticles(world.particles, camX, camY)

                    // Layer 4 — Player
                    drawPlayer(world.player, camX, camY)

                    // Layer 5 — Torch Lighting Mask (SIGNATURE EFFECT)
                    drawTorchMask(world.player, camX, camY, world.elapsedSeconds)
                }

                // ── HUD Overlay ───────────────────────────────────────────────
                GameHudOverlay(
                    world = world,
                    onMove = { dir -> viewModel.onMove(dir) },
                    onPause = { viewModel.onPause() }
                )

                // Floating score popups
                floatingScores.forEach { fs ->
                    Text(
                        text = fs.text,
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = fs.offsetY.value.dp)
                    )
                }

                if (uiState is GameUiState.Paused) {
                    PauseOverlay(
                        onResume  = { viewModel.onResume() },
                        onRestart = { viewModel.onRestart() },
                        onHome    = onNavigateHome
                    )
                }
            }

            is GameUiState.GameOver -> {
                GameOverScreen(
                    world = state.world,
                    causeOfDeath = state.causeOfDeath,
                    onDriftAgain = { viewModel.onRestart() },
                    onViewHistory = onNavigateHome
                )
            }

            is GameUiState.RoomCleared -> {
                RoomClearedOverlay(
                    world  = world,
                    onNext = { viewModel.startNewRun() }
                )
            }

            else -> Unit
        }
    }
}

// ── Compose UI HUD Overlay ───────────────────────────────────────────────────

@Composable
private fun GameHudOverlay(
    world: GameWorld,
    onMove: (Vector2D) -> Unit,
    onPause: () -> Unit
) {
    val hpPct    = (world.player.hp / 100f).coerceIn(0f, 1f)
    val torchPct = (world.player.torchRadius / 300f).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Bar Row
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top-left: HP bar (red, 4dp height rounded)
            Column {
                Text("HP", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .background(Color(0xFF2D0000), shape = RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width((120 * hpPct).dp)
                            .height(4.dp)
                            .background(Color.Red, shape = RoundedCornerShape(2.dp))
                    )
                }
            }

            // Top-center: Depth indicator "DEPTH 3"
            Text(
                text = "DEPTH ${world.runDepth}",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            // Top-right: Torch meter (amber gradient bar, pulses red when below 20%)
            Column(horizontalAlignment = Alignment.End) {
                Text("TORCH", color = TorchAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                val torchBarColor = if (torchPct < 0.2f) Color.Red else TorchAmber
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(4.dp)
                        .background(Color(0xFF332400), shape = RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width((100 * torchPct).dp)
                            .height(4.dp)
                            .background(torchBarColor, shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // Bottom-center: D-pad style touch controls (4 directional buttons, semi-transparent)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onMove(Vector2D.Up) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x661A0030)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Up", tint = NeonCyan)
                }
                Row {
                    IconButton(
                        onClick = { onMove(Vector2D.Left) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x661A0030)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Left", tint = NeonCyan)
                    }
                    Spacer(Modifier.width(48.dp))
                    IconButton(
                        onClick = { onMove(Vector2D.Right) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x661A0030)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Right", tint = NeonCyan)
                    }
                }
                IconButton(
                    onClick = { onMove(Vector2D.Down) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x661A0030)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Down", tint = NeonCyan)
                }
            }
        }
    }
}

// ── Overlays ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("GENERATING VOID…", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onHome: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color(0xDD000010)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PAUSED", color = NeonCyan, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            VoidButton("RESUME",  onResume)
            VoidButton("RESTART", onRestart)
            VoidButton("HOME",    onHome)
        }
    }
}

@Composable
private fun RoomClearedOverlay(world: GameWorld, onNext: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(VoidCorrupt.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VOID CLEARED", color = NeonCyan, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Text("Score: ${world.score}", color = NeonCyan, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            VoidButton("NEXT DEPTH", onNext)
        }
    }
}

@Composable
private fun VoidButton(label: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0030))
    ) {
        Text(label, color = NeonCyan, fontWeight = FontWeight.Bold)
    }
}
