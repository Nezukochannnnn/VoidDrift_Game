package com.voiddrift.game.presentation.engine

import com.voiddrift.game.domain.model.GameState
import com.voiddrift.game.domain.model.GameStatus
import com.voiddrift.game.domain.model.Projectile
import com.voiddrift.game.domain.model.Vector2D
import com.voiddrift.game.domain.usecase.CheckCollisionsUseCase
import com.voiddrift.game.domain.usecase.SpawnEntitiesUseCase
import com.voiddrift.game.domain.usecase.UpdatePhysicsUseCase
import com.voiddrift.game.presentation.model.AudioEvent
import com.voiddrift.game.presentation.model.InputCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameLoopEngine @Inject constructor(
    private val updatePhysicsUseCase: UpdatePhysicsUseCase,
    private val checkCollisionsUseCase: CheckCollisionsUseCase,
    private val spawnEntitiesUseCase: SpawnEntitiesUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var gameLoopJob: Job? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _audioEvents = MutableSharedFlow<AudioEvent>()
    val audioEvents: SharedFlow<AudioEvent> = _audioEvents.asSharedFlow()

    private var lastSpawnTimeMs: Long = 0L
    private var targetFps: Int = 60

    fun startGame(worldWidth: Float, worldHeight: Float, fps: Int = 60) {
        targetFps = fps
        _gameState.value = GameState(
            status = GameStatus.RUNNING,
            worldWidth = worldWidth,
            worldHeight = worldHeight
        )
        lastSpawnTimeMs = System.currentTimeMillis()
        runLoop()
    }

    fun pauseGame() {
        _gameState.value = _gameState.value.copy(status = GameStatus.PAUSED)
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.value = _gameState.value.copy(status = GameStatus.RUNNING)
            runLoop()
        }
    }

    fun stopGame() {
        gameLoopJob?.cancel()
        _gameState.value = _gameState.value.copy(status = GameStatus.GAME_OVER)
    }

    fun processInput(command: InputCommand) {
        val current = _gameState.value
        if (current.status != GameStatus.RUNNING) return

        when (command) {
            is InputCommand.Move -> {
                val newVel = (current.player.velocity + command.delta * 12f)
                    .clampMagnitude(PhysicsConstants.MAX_DRIFT_SPEED)
                val newRot = if (command.delta.magnitude() > 0.1f) {
                    Math.toDegrees(command.delta.angle().toDouble()).toFloat() + 90f
                } else current.player.rotationDegrees

                _gameState.value = current.copy(
                    player = current.player.copy(
                        velocity = newVel,
                        rotationDegrees = newRot
                    )
                )
            }
            is InputCommand.FireWeapon -> {
                firePlayerWeapon()
            }
            is InputCommand.ActivatePowerUp -> {
                // Trigger EMP or time freeze if active
            }
            InputCommand.PauseGame -> pauseGame()
            InputCommand.ResumeGame -> resumeGame()
            InputCommand.RestartGame -> startGame(current.worldWidth, current.worldHeight, targetFps)
            else -> {}
        }
    }

    private fun firePlayerWeapon() {
        val current = _gameState.value
        val player = current.player
        val now = System.currentTimeMillis()

        if (now - player.lastFiredTimestamp < player.currentWeapon.fireRateMs) return
        if (player.energy < player.currentWeapon.energyCost) return

        val projList = current.projectiles.toMutableList()
        val dir = Vector2D(0f, -1f).rotate(player.rotationDegrees)
        val projVel = dir * 1200f

        projList.add(
            Projectile(
                isPlayerOwner = true,
                position = player.position,
                velocity = projVel,
                damage = player.currentWeapon.baseDamage * player.weaponLevel,
                weaponType = player.currentWeapon
            )
        )

        _gameState.value = current.copy(
            player = player.copy(
                lastFiredTimestamp = now,
                energy = player.energy - player.currentWeapon.energyCost
            ),
            projectiles = projList
        )

        scope.launch {
            _audioEvents.emit(AudioEvent.PLAYER_LASER_FIRE)
        }
    }

    private fun runLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            val frameDelayMs = (1000f / targetFps).toLong()
            var lastTime = System.currentTimeMillis()

            while (_gameState.value.status == GameStatus.RUNNING) {
                val now = System.currentTimeMillis()
                val deltaTimeSec = (now - lastTime) / 1000f
                lastTime = now

                // 1. Physics update
                var state = updatePhysicsUseCase(_gameState.value, deltaTimeSec)

                // 2. Spawn entities
                val spawnResult = spawnEntitiesUseCase(state, lastSpawnTimeMs)
                state = spawnResult.first
                lastSpawnTimeMs = spawnResult.second

                // 3. Collision detection
                val collisionResult = checkCollisionsUseCase(state)
                state = collisionResult.updatedState

                // Audio events from collision
                if (collisionResult.playerHit) {
                    _audioEvents.emit(AudioEvent.PLAYER_HULL_DAMAGE)
                }
                if (collisionResult.destroyedEnemies.isNotEmpty()) {
                    _audioEvents.emit(AudioEvent.EXPLOSION_SMALL)
                }

                // Check Game Over condition
                if (!state.player.isAlive) {
                    state = state.copy(status = GameStatus.GAME_OVER)
                }

                _gameState.value = state
                delay(frameDelayMs)
            }
        }
    }
}
