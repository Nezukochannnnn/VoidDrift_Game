package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * TorchShader — AGSL signature torch mask shader.
 */
object TorchShader {

    const val AGSL_SOURCE = """
        uniform float2 playerPos;
        uniform float  radius;
        uniform float  time;

        half4 main(float2 coord) {
            float dist = distance(coord, playerPos);
            float flicker = 0.93 + 0.07 * sin(time * 19.3);
            float alpha = smoothstep(radius * flicker, radius * 0.25 * flicker, dist);
            return half4(1.0, 0.87, 0.55, alpha);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader = RuntimeShader(AGSL_SOURCE)

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(
        shader: RuntimeShader,
        playerScreenX: Float,
        playerScreenY: Float,
        torchRadius: Float,
        timeSec: Float
    ) {
        shader.setFloatUniform("playerPos", playerScreenX, playerScreenY)
        shader.setFloatUniform("radius", torchRadius)
        shader.setFloatUniform("time", timeSec)
    }
}
