package com.voidrift.game.presentation.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("voidrift_settings")

data class SettingsState(
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val sfxVolume: Float = 0.8f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    companion object {
        private val SFX_KEY    = booleanPreferencesKey("sfx_enabled")
        private val MUSIC_KEY  = booleanPreferencesKey("music_enabled")
        private val SHAKE_KEY  = booleanPreferencesKey("shake_enabled")
        private val VOL_KEY    = floatPreferencesKey("sfx_volume")
    }

    val state: StateFlow<SettingsState> = ctx.dataStore.data
        .map { prefs ->
            SettingsState(
                sfxEnabled          = prefs[SFX_KEY]   ?: true,
                musicEnabled        = prefs[MUSIC_KEY] ?: true,
                screenShakeEnabled  = prefs[SHAKE_KEY] ?: true,
                sfxVolume           = prefs[VOL_KEY]   ?: 0.8f
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun toggleSfx()    = toggle(SFX_KEY, state.value.sfxEnabled)
    fun toggleMusic()  = toggle(MUSIC_KEY, state.value.musicEnabled)
    fun toggleShake()  = toggle(SHAKE_KEY, state.value.screenShakeEnabled)
    fun setSfxVolume(v: Float) = viewModelScope.launch {
        ctx.dataStore.edit { it[VOL_KEY] = v.coerceIn(0f, 1f) }
    }

    private fun toggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, current: Boolean) =
        viewModelScope.launch { ctx.dataStore.edit { it[key] = !current } }
}
