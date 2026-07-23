package com.voidrift.game.presentation.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidrift.game.domain.repository.IPlayerRepository
import com.voidrift.game.domain.repository.IRunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("voidrift_settings")

enum class GraphicsQuality { ULTRA, HIGH, NORMAL }

data class SettingsState(
    val playerName: String = "Drifter",
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val sfxVolume: Float = 0.8f,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.ULTRA
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val runRepo: IRunRepository,
    private val playerRepo: IPlayerRepository
) : ViewModel() {

    companion object {
        private val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        private val SFX_KEY         = booleanPreferencesKey("sfx_enabled")
        private val MUSIC_KEY       = booleanPreferencesKey("music_enabled")
        private val HAPTICS_KEY     = booleanPreferencesKey("haptics_enabled")
        private val SHAKE_KEY       = booleanPreferencesKey("shake_enabled")
        private val VOL_KEY         = floatPreferencesKey("sfx_volume")
        private val QUALITY_KEY     = stringPreferencesKey("graphics_quality")
    }

    val state: StateFlow<SettingsState> = ctx.dataStore.data
        .map { prefs ->
            val qualityStr = prefs[QUALITY_KEY] ?: GraphicsQuality.ULTRA.name
            val quality = try { GraphicsQuality.valueOf(qualityStr) } catch (_: Exception) { GraphicsQuality.ULTRA }
            SettingsState(
                playerName         = prefs[PLAYER_NAME_KEY] ?: "Drifter",
                sfxEnabled         = prefs[SFX_KEY]   ?: true,
                musicEnabled       = prefs[MUSIC_KEY] ?: true,
                hapticsEnabled     = prefs[HAPTICS_KEY] ?: true,
                screenShakeEnabled = prefs[SHAKE_KEY] ?: true,
                sfxVolume          = prefs[VOL_KEY]   ?: 0.8f,
                graphicsQuality    = quality
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun updatePlayerName(name: String) = viewModelScope.launch {
        ctx.dataStore.edit { it[PLAYER_NAME_KEY] = name }
        playerRepo.updateName(name)
    }

    fun toggleSfx()    = toggle(SFX_KEY, state.value.sfxEnabled)
    fun toggleMusic()  = toggle(MUSIC_KEY, state.value.musicEnabled)
    fun toggleHaptics() = toggle(HAPTICS_KEY, state.value.hapticsEnabled)
    fun toggleShake()  = toggle(SHAKE_KEY, state.value.screenShakeEnabled)

    fun setSfxVolume(v: Float) = viewModelScope.launch {
        ctx.dataStore.edit { it[VOL_KEY] = v.coerceIn(0f, 1f) }
    }

    fun setGraphicsQuality(quality: GraphicsQuality) = viewModelScope.launch {
        ctx.dataStore.edit { it[QUALITY_KEY] = quality.name }
    }

    fun wipeAllData() = viewModelScope.launch {
        runRepo.deleteAll()
        ctx.dataStore.edit { it.clear() }
    }

    private fun toggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, current: Boolean) =
        viewModelScope.launch { ctx.dataStore.edit { it[key] = !current } }
}
