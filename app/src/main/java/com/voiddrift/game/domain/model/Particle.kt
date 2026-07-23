package com.voiddrift.game.domain.model

enum class ParticleType {
    VOID_SPARK,
    TORCH_EMBER,
    CRYSTAL_GLINT,
    BLOOD_SPLATTER,
    VOID_CONSUME
}

data class Particle(
    val position: Vector2D,
    val velocity: Vector2D,
    val type: ParticleType,
    val lifetimeMs: Float,     // Total lifetime in ms
    val remainingMs: Float,    // Remaining lifetime in ms
    val scale: Float = 1f,
    val alpha: Float = 1f
) {
    val isAlive: Boolean get() = remainingMs > 0f
    val progress: Float get() = 1f - (remainingMs / lifetimeMs).coerceIn(0f, 1f)

    fun tick(deltaTime: Float): Particle {
        val dt = deltaTime * 1000f
        return copy(
            position = position + velocity * deltaTime,
            remainingMs = (remainingMs - dt).coerceAtLeast(0f),
            alpha = (1f - progress).coerceIn(0f, 1f)
        )
    }
}
