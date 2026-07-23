package com.voiddrift.game.presentation.viewmodel

import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiddrift.game.data.local.db.PlayerProfileDao
import com.voiddrift.game.data.local.db.RunRecordDao
import com.voiddrift.game.data.local.db.RunRecordEntity
import com.voiddrift.game.domain.model.GameWorld
import com.voiddrift.game.domain.model.checkWinLossConditions
import com.voiddrift.game.domain.model.resolveCollisions
import com.voiddrift.game.domain.model.spawnParticles
import com.voiddrift.game.domain.model.tickEnemyAI
import com.voiddrift.game.domain.model.tickTorchDecay
import com.voiddrift.game.domain.model.tickVoidCorruption
import com.voiddrift.game.domain.usecase.GenerateDungeonUseCase
import com.voiddrift.game.presentation.model.GameUiState
import com.voiddrift.game.presentation.model.InputCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

/**
 * Dungeon GameViewModel
 *
 * Owns the [GameWorld] mutable state and drives the coroutine game loop
 * exactly as specified (withFrameNanos-based delta-time loop).
 */
@HiltViewModel
class DungeonGameViewModel @Inject constructor(
    private val generateDungeonUseCase: GenerateDungeonUseCase,
    private val runRecordDao: RunRecordDao,
    private val playerProfileDao: PlayerProfileDao
) : ViewModel() {

    // ── UI state ─────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Idle)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // ── World state (mutable, atomically updated per frame) ──────────────────
    private val _world = MutableStateFlow(GameWorld())
    val world: StateFlow<GameWorld> = _world.asStateFlow()

    private var gameLoopJob: Job? = null
    private var runStartTimeMs: Long = 0L
    private var currentSeed: Long = 0L

    // ── Start a new run ──────────────────────────────────────────────────────

    fun startNewRun(seed: Long = Random.nextLong()) {
        currentSeed = seed
        runStartTimeMs = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.Default) {
            // Ensure player profile row exists
            playerProfileDao.createIfAbsent(
                com.voiddrift.game.data.local.db.PlayerProfileEntity()
            )
            // Generate dungeon on Default dispatcher (CPU-intensive)
            val initialWorld = generateDungeonUseCase(seed = seed, depth = 1)
            _world.value = initialWorld
            _uiState.value = GameUiState.Playing
            runGameLoop()
        }
    }

    fun pauseGame() {
        _uiState.value = GameUiState.Paused
        gameLoopJob?.cancel()
    }

    fun resumeGame() {
        if (_uiState.value == GameUiState.Paused) {
            _uiState.value = GameUiState.Playing
            viewModelScope.launch(Dispatchers.Default) { runGameLoop() }
        }
    }

    fun handleInput(command: InputCommand) {
        if (_uiState.value != GameUiState.Playing) return
        _world.update { world ->
            when (command) {
                is InputCommand.Move -> {
                    val newVel = command.delta * 200f
                    val newPos = world.player.position + newVel * 0.016f
                    world.copy(
                        player = world.player.copy(
                            position = newPos,
                            velocity = newVel
                        ).withSyncedBounds()
                    )
                }
                else -> world
            }
        }
    }

    // ── Core game loop — exact specification ─────────────────────────────────

    /**
     * Runs on the Main dispatcher to access [withFrameNanos] (Choreographer-synced).
     * All world mutation is pure functional: each tick produces a new [GameWorld].
     */
    private suspend fun runGameLoop() {
        withContext(Dispatchers.Main) {
            var lastFrameNanos = 0L
            while (_uiState.value == GameUiState.Playing) {
                withFrameNanos { frameNanos ->
                    val deltaTime = if (lastFrameNanos == 0L) 0f
                    else (frameNanos - lastFrameNanos) / 1_000_000_000f
                    lastFrameNanos = frameNanos

                    _world.update { world ->
                        world
                            .tickVoidCorruption(deltaTime)
                            .tickTorchDecay(deltaTime)
                            .tickEnemyAI(deltaTime)
                            .resolveCollisions()
                            .spawnParticles(deltaTime)
                            .checkWinLossConditions()
                    }

                    // Detect game-over outside the atomic update to avoid nested suspension
                    if (_world.value.isGameOver) {
                        _uiState.value = GameUiState.GameOver
                        viewModelScope.launch(Dispatchers.IO) { persistRunRecord() }
                    }
                }
            }
        }
    }

    // ── Persist run stats to Room ─────────────────────────────────────────────

    private suspend fun persistRunRecord() {
        val world = _world.value
        val durationSeconds = (System.currentTimeMillis() - runStartTimeMs) / 1000f
        val causeOfDeath = when {
            world.player.isTorchExtinguished -> "Torch extinguished — darkness consumed"
            !world.player.isAlive            -> "Slain by ${world.enemies.firstOrNull { !it.isAlive }?.type?.name ?: "unknown"}"
            else                             -> "Unknown"
        }

        val runRecord = RunRecordEntity(
            seed = currentSeed,
            depth = world.runDepth,
            score = world.score,
            killCount = world.killCount,
            durationSeconds = durationSeconds,
            causeOfDeath = causeOfDeath
        )
        runRecordDao.insertRun(runRecord)
        playerProfileDao.recordRunCompletion(
            depth = world.runDepth,
            score = world.score,
            durationSeconds = durationSeconds
        )
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

// Private extension — keeps withSyncedBounds accessible within ViewModel scope
private fun com.voiddrift.game.domain.model.Player.withSyncedBounds() =
    copy(bounds = bounds.copy(centerX = position.x, centerY = position.y))
