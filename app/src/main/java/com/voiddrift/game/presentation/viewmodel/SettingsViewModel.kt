package com.voiddrift.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiddrift.game.domain.model.GameSettings
import com.voiddrift.game.domain.usecase.ManageSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageSettingsUseCase: ManageSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<GameSettings> = manageSettingsUseCase.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameSettings()
        )

    fun updateSfxVolume(volume: Float) {
        viewModelScope.launch {
            manageSettingsUseCase.updateSfxVolume(volume)
        }
    }

    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch {
            manageSettingsUseCase.updateMusicVolume(volume)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            manageSettingsUseCase.setHapticsEnabled(enabled)
        }
    }
}
