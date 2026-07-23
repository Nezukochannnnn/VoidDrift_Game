package com.voidrift.game.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SoundManager — Lightweight SoundPool & MediaPlayer audio controller.
 *
 * Handles ambient void hum, pickup chimes, enemy hit thuds, torch warnings,
 * and game over stings.
 *
 * Drop `.mp3` or `.ogg` sound files into `res/raw/`:
 *  - `res/raw/ambient_void_hum.mp3`
 *  - `res/raw/crystal_pickup.mp3`
 *  - `res/raw/enemy_hit.mp3`
 *  - `res/raw/torch_warning.mp3`
 *  - `res/raw/game_over_sting.mp3`
 *
 * If sound files are absent, calls fail silently without crashing.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var sfxEnabled = true
    private var sfxVolume = 0.8f

    // Sound IDs (0 if not loaded)
    private var crystalPickupId = 0
    private var enemyHitId = 0
    private var torchWarningId = 0
    private var gameOverStingId = 0

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        loadSoundPoolResources()
    }

    private fun loadSoundPoolResources() {
        val sp = soundPool ?: return
        crystalPickupId = loadRawResource(sp, "crystal_pickup")
        enemyHitId      = loadRawResource(sp, "enemy_hit")
        torchWarningId  = loadRawResource(sp, "torch_warning")
        gameOverStingId = loadRawResource(sp, "game_over_sting")
    }

    private fun loadRawResource(sp: SoundPool, name: String): Int {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (resId != 0) {
            sp.load(context, resId, 1)
        } else {
            0
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        sfxEnabled = enabled
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun playCrystalPickup() {
        if (!sfxEnabled || crystalPickupId == 0) return
        soundPool?.play(crystalPickupId, sfxVolume, sfxVolume, 1, 0, 1.0f)
    }

    fun playEnemyHit() {
        if (!sfxEnabled || enemyHitId == 0) return
        soundPool?.play(enemyHitId, sfxVolume, sfxVolume, 1, 0, 1.0f)
    }

    fun playTorchWarning() {
        if (!sfxEnabled || torchWarningId == 0) return
        soundPool?.play(torchWarningId, sfxVolume, sfxVolume, 1, 0, 1.0f)
    }

    fun playGameOverSting() {
        if (!sfxEnabled || gameOverStingId == 0) return
        soundPool?.play(gameOverStingId, sfxVolume, sfxVolume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
