package com.voidrift.game.core.engine

import androidx.compose.runtime.withFrameNanos
import com.voidrift.game.domain.model.GameWorld
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Callback type: receives deltaTime in seconds, returns the updated world. */
typealias TickFn = (world: GameWorld, deltaTime: Float) -> GameWorld

/**
 * [GameLoop] drives the frame-synchronized game tick on the Main dispatcher
 * using [withFrameNanos] (Choreographer-synced, no busy-spin).
 *
 * It delegates every tick to a caller-supplied [TickFn] that transforms a
 * [GameWorld] → [GameWorld], keeping the loop itself pure and testable.
 */
@Singleton
class GameLoop @Inject constructor() {


    private var loopJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /**
     * Start the game loop in [scope].
     *
     * @param worldFlow  A [MutableStateFlow] of the authoritative [GameWorld].
     * @param running    Predicate — loop continues while this returns `true`.
     * @param onTick     Pure function called each frame with (world, deltaTime).
     */
    fun start(
        scope: CoroutineScope,
        worldFlow: MutableStateFlow<GameWorld>,
        running: () -> Boolean,
        onTick: TickFn
    ) {
        if (loopJob?.isActive == true) return
        _isRunning.value = true

        loopJob = scope.launch {
            withContext(Dispatchers.Main) {
                var lastFrameNanos = 0L
                while (running()) {
                    withFrameNanos { frameNanos ->
                        val deltaTime = if (lastFrameNanos == 0L) 0f
                        else (frameNanos - lastFrameNanos) / 1_000_000_000f
                        lastFrameNanos = frameNanos

                        // Clamp delta to avoid spiral-of-death on debug pauses
                        val clampedDt = deltaTime.coerceIn(0f, 0.05f)

                        worldFlow.value = onTick(worldFlow.value, clampedDt)
                    }
                }
            }
            _isRunning.value = false
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        _isRunning.value = false
    }
}
