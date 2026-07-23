package com.voidrift.game.domain.repository

import com.voidrift.game.data.local.entity.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface IPlayerRepository {
    fun getProfile(): Flow<PlayerProfile?>
    suspend fun ensureProfileExists(name: String = "Drifter")
    suspend fun recordRunCompletion(depth: Int, score: Long, durationSeconds: Float)
    suspend fun updateName(name: String)
}
