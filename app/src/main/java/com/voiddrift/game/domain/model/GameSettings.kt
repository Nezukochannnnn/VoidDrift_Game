package com.voiddrift.game.domain.model

data class GameSettings(
    val sfxVolume: Float = 0.8f,
    val musicVolume: Float = 0.6f,
    val hapticFeedbackEnabled: Boolean = true,
    val touchSensitivity: Float = 1.0f,
    val useGyroscopeControl: Boolean = false,
    val enableAgslShaders: Boolean = true,
    val targetFps: Int = 60
)
