package com.voiddrift.game.domain.repository

import com.voiddrift.game.presentation.model.AudioEvent

interface AudioRepository {
    fun playSoundEffect(event: AudioEvent)
    fun playBackgroundMusic()
    fun pauseBackgroundMusic()
    fun stopBackgroundMusic()
    fun release()
}
