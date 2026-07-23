package com.voiddrift.game.presentation.shader

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgslShaderManager @Inject constructor() {

    val isAgslSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // API 33+

    private var warpShader: Any? = null
    private var damageShader: Any? = null

    init {
        if (isAgslSupported) {
            initShadersApi33()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initShadersApi33() {
        try {
            warpShader = RuntimeShader(VoidDriftShaders.WARP_GRID_AGSL)
            damageShader = RuntimeShader(VoidDriftShaders.DAMAGE_FLASH_AGSL)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateWarpUniforms(width: Float, height: Float, timeSec: Float, playerX: Float, playerY: Float) {
        if (isAgslSupported && warpShader is RuntimeShader) {
            val shader = warpShader as RuntimeShader
            shader.setFloatUniform("iResolution", width, height)
            shader.setFloatUniform("iTime", timeSec)
            shader.setFloatUniform("iPlayerPos", playerX, playerY)
        }
    }

    fun updateDamageUniforms(width: Float, height: Float, intensity: Float) {
        if (isAgslSupported && damageShader is RuntimeShader) {
            val shader = damageShader as RuntimeShader
            shader.setFloatUniform("iResolution", width, height)
            shader.setFloatUniform("iIntensity", intensity)
        }
    }

    fun getWarpRuntimeShader(): Any? = warpShader
    fun getDamageRuntimeShader(): Any? = damageShader
}
