package com.voiddrift.game.domain.model

enum class GameStatus {
    IDLE,
    RUNNING,
    PAUSED,
    GAME_OVER
}

data class GameState(
    val status: GameStatus = GameStatus.IDLE,
    val score: Int = 0,
    val multiplier: Int = 1,
    val comboCount: Int = 0,
    val waveNumber: Int = 1,
    val lives: Int = 3,
    val player: PlayerShip = PlayerShip(),
    val enemies: List<Enemy> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val powerUps: List<PowerUp> = emptyList(),
    val activePowerUpType: PowerUpType? = null,
    val powerUpTimerMs: Long = 0L,
    val gameTimeMs: Long = 0L,
    val worldWidth: Float = 1080f,
    val worldHeight: Float = 2400f
)
