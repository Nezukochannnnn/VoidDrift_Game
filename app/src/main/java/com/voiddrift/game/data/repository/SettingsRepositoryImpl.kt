package com.voiddrift.game.data.repository

import com.voiddrift.game.data.local.datastore.UserPreferencesDataStore
import com.voiddrift.game.domain.model.GameSettings
import com.voiddrift.game.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<GameSettings> = dataStore.gameSettingsFlow

    override suspend fun updateSettings(settings: GameSettings) {
        dataStore.updateSettings(settings)
    }

    override suspend fun setSfxVolume(volume: Float) {
        dataStore.setSfxVolume(volume)
    }

    override suspend fun setMusicVolume(volume: Float) {
        dataStore.setMusicVolume(volume)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.setHapticsEnabled(enabled)
    }
}
