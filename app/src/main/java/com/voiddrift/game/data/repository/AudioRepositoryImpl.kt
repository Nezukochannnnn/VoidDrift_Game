package com.voiddrift.game.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.voiddrift.game.domain.repository.AudioRepository
import com.voiddrift.game.presentation.model.AudioEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRepository {

    private val soundPool: SoundPool

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    override fun playSoundEffect(event: AudioEvent) {
        // SoundPool playback implementation contract ready for audio asset IDs in Phase 2
    }

    override fun playBackgroundMusic() {
        // Background music playback contract ready for MediaPlayer in Phase 2
    }

    override fun pauseBackgroundMusic() {
    }

    override fun stopBackgroundMusic() {
    }

    override fun release() {
        soundPool.release()
    }
}
