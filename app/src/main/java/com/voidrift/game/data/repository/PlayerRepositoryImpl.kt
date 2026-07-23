package com.voidrift.game.data.repository

import com.voidrift.game.data.local.dao.PlayerDao
import com.voidrift.game.data.local.entity.PlayerProfile
import com.voidrift.game.domain.repository.IPlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val dao: PlayerDao
) : IPlayerRepository {

    override fun getProfile(): Flow<PlayerProfile?> = dao.observe()

    override suspend fun ensureProfileExists(name: String) {
        dao.createIfAbsent(PlayerProfile(playerName = name))
    }

    override suspend fun recordRunCompletion(depth: Int, score: Long, durationSeconds: Float) {
        val current = dao.getOnce() ?: PlayerProfile()
        dao.update(
            current.copy(
                totalRuns            = current.totalRuns + 1,
                bestDepth            = maxOf(current.bestDepth, depth),
                bestScore            = maxOf(current.bestScore, score),
                totalPlaytimeSeconds = current.totalPlaytimeSeconds + durationSeconds
            )
        )
    }

    override suspend fun updateName(name: String) = dao.updateName(name)
}
