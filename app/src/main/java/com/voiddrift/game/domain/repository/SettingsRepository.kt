package com.voiddrift.game.domain.repository

import com.voiddrift.game.domain.model.GameSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<GameSettings>
    suspend fun updateSettings(settings: GameSettings)
    suspend fun setSfxVolume(volume: Float)
    suspend fun setMusicVolume(volume: Float)
    suspend fun setHapticsEnabled(enabled: Boolean)
}
