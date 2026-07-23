package com.voiddrift.game.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.voiddrift.game.domain.model.GameSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voiddrift_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferenceKeys {
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val TOUCH_SENSITIVITY = floatPreferencesKey("touch_sensitivity")
        val USE_GYROSCOPE = booleanPreferencesKey("use_gyroscope")
        val ENABLE_SHADERS = booleanPreferencesKey("enable_shaders")
        val TARGET_FPS = intPreferencesKey("target_fps")
    }

    val gameSettingsFlow: Flow<GameSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            GameSettings(
                sfxVolume = prefs[PreferenceKeys.SFX_VOLUME] ?: 0.8f,
                musicVolume = prefs[PreferenceKeys.MUSIC_VOLUME] ?: 0.6f,
                hapticFeedbackEnabled = prefs[PreferenceKeys.HAPTICS_ENABLED] ?: true,
                touchSensitivity = prefs[PreferenceKeys.TOUCH_SENSITIVITY] ?: 1.0f,
                useGyroscopeControl = prefs[PreferenceKeys.USE_GYROSCOPE] ?: false,
                enableAgslShaders = prefs[PreferenceKeys.ENABLE_SHADERS] ?: true,
                targetFps = prefs[PreferenceKeys.TARGET_FPS] ?: 60
            )
        }

    suspend fun updateSettings(settings: GameSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SFX_VOLUME] = settings.sfxVolume
            prefs[PreferenceKeys.MUSIC_VOLUME] = settings.musicVolume
            prefs[PreferenceKeys.HAPTICS_ENABLED] = settings.hapticFeedbackEnabled
            prefs[PreferenceKeys.TOUCH_SENSITIVITY] = settings.touchSensitivity
            prefs[PreferenceKeys.USE_GYROSCOPE] = settings.useGyroscopeControl
            prefs[PreferenceKeys.ENABLE_SHADERS] = settings.enableAgslShaders
            prefs[PreferenceKeys.TARGET_FPS] = settings.targetFps
        }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SFX_VOLUME] = volume
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.MUSIC_VOLUME] = volume
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAPTICS_ENABLED] = enabled
        }
    }
}
