package com.voiddrift.game.domain.model

import kotlin.math.min

/**
 * The authoritative game world state — a pure immutable data class.
 * All mutation returns a new GameWorld via extension functions.
 */
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
    val voidSpreadTimer: Float = 0f    // Accumulates before void spreads
)

// ──────────────────────────────────────────────────────────────────────────────
// GameWorld tick extensions — each returns a new GameWorld (functional update)
// ──────────────────────────────────────────────────────────────────────────────

/** Advances void corruption on FLOOR tiles adjacent to already-consumed tiles. */
fun GameWorld.tickVoidCorruption(deltaTime: Float): GameWorld {
    val spreadRate = 0.04f * deltaTime   // Progress units/sec per neighbor influence
    val updatedTiles = dungeon.tiles.map { tile ->
        if (tile.type == TileType.WALL || tile.type == TileType.EXIT || tile.type == TileType.VOID) {
            tile
        } else {
            // Sample neighbors: how much void pressure?
            val neighbors = listOf(
                dungeon.tileAt(tile.gridX - 1, tile.gridY),
                dungeon.tileAt(tile.gridX + 1, tile.gridY),
                dungeon.tileAt(tile.gridX, tile.gridY - 1),
                dungeon.tileAt(tile.gridX, tile.gridY + 1)
            )
            val voidPressure = neighbors.filterNotNull()
                .sumOf { it.voidProgress.toDouble() }.toFloat() * spreadRate

            val newProgress = (tile.voidProgress + voidPressure).coerceIn(0f, 1f)
            val newType = if (newProgress >= 1f) TileType.VOID else tile.type
            tile.copy(voidProgress = newProgress, type = newType)
        }
    }
    return copy(
        dungeon = dungeon.copy(tiles = updatedTiles),
        voidSpreadTimer = voidSpreadTimer + deltaTime
    )
}

/** Decays the player's torch radius over time. */
fun GameWorld.tickTorchDecay(deltaTime: Float): GameWorld {
    val newPlayer = player.decayTorch(deltaTime)
    return copy(player = newPlayer)
}

/** Ticks enemy AI: transitions aiState, steers velocity toward player when chasing. */
fun GameWorld.tickEnemyAI(deltaTime: Float): GameWorld {
    val updatedEnemies = enemies.map { enemy ->
        if (!enemy.isAlive) return@map enemy

        val distToPlayer = enemy.position.distanceTo(player.position)
        val detectionRange = enemy.type.detectionRadius

        val newAiState = when {
            enemy.aiState == AiState.SPAWNING -> AiState.IDLE
            distToPlayer <= enemy.type.attackDamage * 3f -> AiState.ATTACKING
            distToPlayer <= detectionRange -> AiState.CHASING
            else -> AiState.IDLE
        }

        val newVelocity = when (newAiState) {
            AiState.CHASING, AiState.ATTACKING -> {
                val dir = (player.position - enemy.position).normalized()
                dir * enemy.type.baseMoveSpeed
            }
            else -> Vector2D.Zero
        }

        val newPos = enemy.position + newVelocity * deltaTime
        enemy.copy(
            position = newPos,
            velocity = newVelocity,
            aiState = newAiState
        ).withSyncedBounds()
    }
    return copy(enemies = updatedEnemies)
}

/** AABB collision resolution between player↔enemies and player↔walls. */
fun GameWorld.resolveCollisions(): GameWorld {
    val tileSize = DungeonRoom.TILE_SIZE
    var p = player.withSyncedBounds()

    // Player vs walls (tile-based)
    dungeon.tiles.forEach { tile ->
        if (tile.type == TileType.WALL) {
            val wallBox = AABB(
                centerX = tile.gridX * tileSize + tileSize / 2f,
                centerY = tile.gridY * tileSize + tileSize / 2f,
                halfW = tileSize / 2f,
                halfH = tileSize / 2f
            )
            if (p.bounds.overlaps(wallBox)) {
                // Push player out along minimum penetration axis
                val overlapX = min(p.bounds.right - wallBox.left, wallBox.right - p.bounds.left)
                val overlapY = min(p.bounds.bottom - wallBox.top, wallBox.bottom - p.bounds.top)
                val newPos = if (overlapX < overlapY) {
                    if (p.position.x < wallBox.centerX) p.position.copy(x = wallBox.left - p.bounds.halfW)
                    else p.position.copy(x = wallBox.right + p.bounds.halfW)
                } else {
                    if (p.position.y < wallBox.centerY) p.position.copy(y = wallBox.top - p.bounds.halfH)
                    else p.position.copy(y = wallBox.bottom + p.bounds.halfH)
                }
                p = p.copy(position = newPos, velocity = Vector2D.Zero).withSyncedBounds()
            }
        }
    }

    // Player vs enemies — deal damage on contact
    var hp = p.hp
    val attackedEnemies = enemies.map { enemy ->
        if (enemy.isAlive && p.bounds.overlaps(enemy.bounds) && enemy.aiState == AiState.ATTACKING) {
            hp = (hp - 1).coerceAtLeast(0)  // Light contact damage per frame
        }
        enemy
    }

    return copy(
        player = p.copy(hp = hp),
        enemies = attackedEnemies
    )
}

/** Spawns new particles from active events (void tiles, torch, enemy deaths). */
fun GameWorld.spawnParticles(deltaTime: Float): GameWorld {
    val tickedParticles = particles.mapNotNull { p ->
        val updated = p.tick(deltaTime)
        if (updated.isAlive) updated else null
    }.toMutableList()

    // Spawn void spark particles from highly-corrupted floor tiles
    dungeon.tiles.filter { it.type != TileType.WALL && it.voidProgress > 0.6f }
        .take(4)
        .forEach { tile ->
            if (kotlin.random.Random.nextFloat() < 0.15f) {
                val center = tile.worldCenter(DungeonRoom.TILE_SIZE)
                tickedParticles.add(
                    Particle(
                        position = center,
                        velocity = Vector2D(
                            kotlin.random.Random.nextFloat() * 40f - 20f,
                            -kotlin.random.Random.nextFloat() * 60f
                        ),
                        type = ParticleType.VOID_SPARK,
                        lifetimeMs = 600f,
                        remainingMs = 600f
                    )
                )
            }
        }

    return copy(particles = tickedParticles)
}

/** Evaluates win/loss conditions and sets flags accordingly. */
fun GameWorld.checkWinLossConditions(): GameWorld {
    val playerDead = !player.isAlive || player.isTorchExtinguished
    val allEnemiesDead = enemies.none { it.isAlive }

    // Check if player is on the exit tile
    val tileSize = DungeonRoom.TILE_SIZE
    val playerGridX = (player.position.x / tileSize).toInt()
    val playerGridY = (player.position.y / tileSize).toInt()
    val onExit = dungeon.exitTile.first == playerGridX && dungeon.exitTile.second == playerGridY

    val roomCleared = allEnemiesDead || onExit
    val killed = enemies.count { !it.isAlive }

    return copy(
        isGameOver = playerDead,
        isRoomCleared = roomCleared && !playerDead,
        killCount = killCount + (killed - enemies.count { !it.isAlive }).coerceAtLeast(0)
    )
}
