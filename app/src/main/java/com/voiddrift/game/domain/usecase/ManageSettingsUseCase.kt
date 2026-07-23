package com.voiddrift.game.domain.usecase

import com.voiddrift.game.domain.model.GameSettings
import com.voiddrift.game.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun getSettings(): Flow<GameSettings> = settingsRepository.getSettings()

    suspend fun updateSettings(settings: GameSettings) {
        settingsRepository.updateSettings(settings)
    }

    suspend fun updateSfxVolume(volume: Float) {
        settingsRepository.setSfxVolume(volume)
    }

    suspend fun updateMusicVolume(volume: Float) {
        settingsRepository.setMusicVolume(volume)
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        settingsRepository.setHapticsEnabled(enabled)
    }
}
