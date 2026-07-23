package com.voidrift.game.domain.model

import com.voidrift.game.core.math.Vector2D

enum class ParticleType { VOID_SPARK, TORCH_EMBER, CRYSTAL_GLINT, BLOOD_SPLATTER, VOID_CONSUME }

data class Particle(
    val position: Vector2D,
    val velocity: Vector2D,
    val type: ParticleType,
    val lifetimeMs: Float,
    val remainingMs: Float,
    val scale: Float = 1f,
    val alpha: Float = 1f
) {
    val isAlive: Boolean get() = remainingMs > 0f
    val progress: Float get() = 1f - (remainingMs / lifetimeMs).coerceIn(0f, 1f)
}

/** Represents the complete, authoritative game state for one run. */
data class GameWorld(
    val player: Player = Player(),
    val enemies: List<Enemy> = emptyList(),
    val dungeon: DungeonRoom = DungeonRoom(),
    val particles: List<Particle> = emptyList(),
    val runDepth: Int = 1,
    val score: Long = 0L,
    val isGameOver: Boolean = false,
    val isRoomCleared: Boolean = false,
    val killCount: Int = 0,
    val elapsedSeconds: Float = 0f
)
