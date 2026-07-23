package com.voidrift.game.core.engine

import com.voidrift.game.core.math.Vector2D
import com.voidrift.game.domain.model.GameWorld
import com.voidrift.game.domain.model.Particle
import com.voidrift.game.domain.model.ParticleType
import com.voidrift.game.domain.model.TileType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Stateless particle system.
 *
 * [tick] — advances all existing particles and prunes dead ones.
 * [emitVoidSparks] — generates sparks from corrupted tiles.
 * [emitTorchEmbers] — generates embers around the player's torch.
 * [emitBloodSplatter] — generates splatter at a given position.
 * [emitCrystalGlint] — generates pickup glint at a given position.
 */
@Singleton
class ParticleSystem @Inject constructor() {

    companion object {
        const val MAX_PARTICLES = 400
        private const val TILE_SIZE = 64f
    }

    /** Advance all particles by [deltaTime] seconds and remove dead ones. */
    fun tick(world: GameWorld, deltaTime: Float): GameWorld {
        val ticked = world.particles
            .map { p ->
                p.copy(
                    position    = p.position + p.velocity * deltaTime,
                    remainingMs = (p.remainingMs - deltaTime * 1000f).coerceAtLeast(0f),
                    alpha       = ((p.remainingMs / p.lifetimeMs)).coerceIn(0f, 1f),
                    velocity    = p.velocity * 0.96f   // friction
                )
            }
            .filter { it.isAlive }
        return world.copy(particles = ticked)
    }

    /** Emit void-corruption sparks from tiles with voidProgress > 0.55f. */
    fun emitVoidSparks(world: GameWorld, rng: Random = Random): GameWorld {
        if (world.particles.size >= MAX_PARTICLES) return world
        val newParticles = world.dungeon.tiles
            .filter { it.type != TileType.WALL && it.voidProgress > 0.55f }
            .take(8)
            .flatMap { tile ->
                if (rng.nextFloat() > 0.12f) return@flatMap emptyList()
                val cx = tile.gridX * TILE_SIZE + TILE_SIZE / 2f
                val cy = tile.gridY * TILE_SIZE + TILE_SIZE / 2f
                List(rng.nextInt(1, 4)) {
                    Particle(
                        position    = Vector2D(cx + rng.nextFloat() * 20f - 10f,
                                              cy + rng.nextFloat() * 20f - 10f),
                        velocity    = Vector2D(rng.nextFloat() * 50f - 25f,
                                              -rng.nextFloat() * 80f - 20f),
                        type        = ParticleType.VOID_SPARK,
                        lifetimeMs  = 500f + rng.nextFloat() * 300f,
                        remainingMs = 500f + rng.nextFloat() * 300f,
                        scale       = rng.nextFloat() * 0.5f + 0.5f
                    )
                }
            }
        val merged = (world.particles + newParticles).takeLast(MAX_PARTICLES)
        return world.copy(particles = merged)
    }

    /** Emit torch embers around the player's current position. */
    fun emitTorchEmbers(world: GameWorld, rng: Random = Random): GameWorld {
        if (world.particles.size >= MAX_PARTICLES) return world
        if (rng.nextFloat() > 0.35f) return world
        val p = world.player
        val count = rng.nextInt(1, 3)
        val embers = List(count) {
            Particle(
                position    = p.position + Vector2D(
                    rng.nextFloat() * 24f - 12f,
                    rng.nextFloat() * 24f - 12f
                ),
                velocity    = Vector2D(rng.nextFloat() * 30f - 15f, -rng.nextFloat() * 60f - 10f),
                type        = ParticleType.TORCH_EMBER,
                lifetimeMs  = 700f,
                remainingMs = 700f,
                scale       = rng.nextFloat() * 0.4f + 0.3f
            )
        }
        return world.copy(particles = (world.particles + embers).takeLast(MAX_PARTICLES))
    }

    /** Emit blood splatter at [pos] (e.g. player taking damage). */
    fun emitBloodSplatter(world: GameWorld, pos: Vector2D, rng: Random = Random): GameWorld {
        val splatter = List(8) {
            Particle(
                position    = pos,
                velocity    = Vector2D(rng.nextFloat() * 140f - 70f, rng.nextFloat() * 140f - 70f),
                type        = ParticleType.BLOOD_SPLATTER,
                lifetimeMs  = 400f,
                remainingMs = 400f,
                scale       = rng.nextFloat() * 0.6f + 0.4f
            )
        }
        return world.copy(particles = (world.particles + splatter).takeLast(MAX_PARTICLES))
    }

    /** Emit crystal pickup glint at [pos]. */
    fun emitCrystalGlint(world: GameWorld, pos: Vector2D, rng: Random = Random): GameWorld {
        val glints = List(6) {
            val angle = rng.nextFloat() * 360f
            val speed = rng.nextFloat() * 80f + 40f
            val rad   = Math.toRadians(angle.toDouble())
            Particle(
                position    = pos,
                velocity    = Vector2D(kotlin.math.cos(rad).toFloat() * speed,
                                      kotlin.math.sin(rad).toFloat() * speed),
                type        = ParticleType.CRYSTAL_GLINT,
                lifetimeMs  = 600f,
                remainingMs = 600f,
                scale       = rng.nextFloat() * 0.5f + 0.5f
            )
        }
        return world.copy(particles = (world.particles + glints).takeLast(MAX_PARTICLES))
    }
}
