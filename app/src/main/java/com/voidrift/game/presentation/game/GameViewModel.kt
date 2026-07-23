package com.voidrift.game.presentation.game

import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidrift.game.core.engine.CollisionSystem
import com.voidrift.game.core.engine.ParticleSystem
import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.AiState
import com.voidrift.game.domain.model.Enemy
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.model.TileType
import com.voidrift.game.domain.usecase.SaveRunRecordUseCase
import com.voidrift.game.domain.usecase.StartNewRunUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    private val startNewRunUseCase: StartNewRunUseCase,
    private val saveRunRecordUseCase: SaveRunRecordUseCase,
    private val collisionSystem: CollisionSystem,
    private val particleSystem: ParticleSystem
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Idle)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _world = MutableStateFlow(GameWorld())
    val world: StateFlow<GameWorld> = _world.asStateFlow()

    private var runStartMs = 0L
    private var currentSeed = 0L

    // ── Public API ────────────────────────────────────────────────────────────

    fun startNewRun(depth: Int = 1, seed: Long = Random.nextLong()) {
        currentSeed = seed
        runStartMs = System.currentTimeMillis()
        _uiState.value = GameUiState.Loading
        viewModelScope.launch(Dispatchers.Default) {
            val initialWorld = startNewRunUseCase(depth = depth, seed = seed)
            _world.value = initialWorld
            _uiState.value = GameUiState.Playing(initialWorld)
            runGameLoop()
        }
    }

    fun onMove(delta: Vector2D) {
        if (_uiState.value !is GameUiState.Playing) return
        _world.update { w ->
            val newVel = delta * 200f
            val newPos = w.player.position + newVel * 0.016f
            w.copy(player = w.player.copy(position = newPos, velocity = newVel).syncBounds())
        }
    }

    fun onPause() {
        val current = _world.value
        _uiState.value = GameUiState.Paused(current)
    }

    fun onResume() {
        val current = _world.value
        _uiState.value = GameUiState.Playing(current)
        viewModelScope.launch(Dispatchers.Default) { runGameLoop() }
    }

    fun onRestart() = startNewRun()

    // ── Game Loop ─────────────────────────────────────────────────────────────

    private suspend fun runGameLoop() {
        var lastFrameNanos = System.nanoTime()
        while (_uiState.value is GameUiState.Playing) {
            val frameNanos = System.nanoTime()
            val deltaTime = if (lastFrameNanos == 0L) 0f
            else (frameNanos - lastFrameNanos) / 1_000_000_000f
            lastFrameNanos = frameNanos
            val dt = deltaTime.coerceIn(0f, 0.05f)

            _world.update { world ->
                val prevKilled = world.enemies.count { !it.isAlive }
                val updatedWorld = world
                    .tickVoidCorruption(dt)
                    .tickTorchDecay(dt)
                    .tickEnemyAI(dt)
                    .let { collisionSystem.resolve(it) }
                    .let { particleSystem.tick(it, dt) }
                    .let { particleSystem.emitVoidSparks(it) }
                    .let { particleSystem.emitTorchEmbers(it) }
                    .checkWinLoss()
                    .copy(elapsedSeconds = world.elapsedSeconds + dt)
                
                val currentKilled = updatedWorld.enemies.count { !it.isAlive }
                val newKills = currentKilled - prevKilled
                
                if (newKills > 0) {
                    updatedWorld.copy(
                        killCount = updatedWorld.killCount + newKills,
                        score = updatedWorld.score + (newKills * 100)
                    )
                } else {
                    updatedWorld
                }
            }

            evaluateOutcome()
            kotlinx.coroutines.delay(16)
        }
    }

    private fun evaluateOutcome() {
        val w = _world.value
        when {
            w.isGameOver -> {
                val cause = when {
                    w.player.isTorchExtinguished -> "Torch extinguished — darkness consumed you"
                    !w.player.isAlive -> "Slain in the void"
                    else -> "Unknown"
                }
                _uiState.value = GameUiState.GameOver(w, cause)
                viewModelScope.launch(Dispatchers.IO) {
                    saveRunRecordUseCase(
                        world           = w,
                        seed            = currentSeed,
                        durationSeconds = (System.currentTimeMillis() - runStartMs) / 1000f,
                        causeOfDeath    = cause
                    )
                }
            }
            w.isRoomCleared -> _uiState.value = GameUiState.RoomCleared(w)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value = GameUiState.Idle
    }
}

// ── Inline tick extensions ────────────────────────────────────────────────────

private fun GameWorld.tickVoidCorruption(dt: Float): GameWorld {
    val spread = 0.04f * dt
    val updated = dungeon.tiles.map { tile ->
        if (tile.type == TileType.WALL || tile.type == TileType.EXIT) return@map tile
        val pressure = listOf(
            dungeon.tileAt(tile.gridX - 1, tile.gridY),
            dungeon.tileAt(tile.gridX + 1, tile.gridY),
            dungeon.tileAt(tile.gridX, tile.gridY - 1),
            dungeon.tileAt(tile.gridX, tile.gridY + 1)
        ).filterNotNull().sumOf { it.voidProgress.toDouble() }.toFloat() * spread
        val prog = (tile.voidProgress + pressure).coerceIn(0f, 1f)
        tile.copy(voidProgress = prog, type = if (prog >= 1f) TileType.VOID else tile.type)
    }
    return copy(dungeon = dungeon.copy(tiles = updated))
}

private fun GameWorld.tickTorchDecay(dt: Float): GameWorld =
    copy(player = player.decayTorch(dt))

private fun GameWorld.tickEnemyAI(dt: Float): GameWorld {
    val updated = enemies.map { enemy ->
        if (!enemy.isAlive) return@map enemy
        val dist = enemy.position.distanceTo(player.position)
        val newState = when {
            dist <= enemy.type.halfSize * 3f -> AiState.ATTACKING
            dist <= enemy.type.detectionRadius -> AiState.CHASING
            else -> AiState.IDLE
        }
        val vel = when (newState) {
            AiState.CHASING, AiState.ATTACKING ->
                (player.position - enemy.position).normalized() * enemy.type.speed
            else -> Vector2D.Zero
        }
        enemy.copy(
            position = enemy.position + vel * dt,
            velocity = vel,
            aiState  = newState
        ).syncBounds()
    }
    return copy(enemies = updated)
}

private fun GameWorld.checkWinLoss(): GameWorld {
    val dead = !player.isAlive || player.isTorchExtinguished
    val tileSize = 64f
    val pgx = (player.position.x / tileSize).toInt()
    val pgy = (player.position.y / tileSize).toInt()
    val onExit = dungeon.exitTile.first == pgx && dungeon.exitTile.second == pgy
    val cleared = onExit // Only clear on exit reaching
    return copy(
        isGameOver    = dead,
        isRoomCleared = cleared && !dead
    )
}

private fun com.voidrift.game.domain.model.Player.syncBounds() =
    copy(bounds = bounds.moveTo(position.x, position.y))

private fun Enemy.syncBounds() =
    copy(bounds = bounds.moveTo(position.x, position.y))
