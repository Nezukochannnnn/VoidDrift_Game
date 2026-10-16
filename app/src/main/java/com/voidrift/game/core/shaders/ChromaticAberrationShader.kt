package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * ChromaticAberrationShader — AGSL shader for GameOverScreen title text glitch effect on API 33+.
 */
object ChromaticAberrationShader {

    const val AGSL_SOURCE = """
        uniform vec2   iResolution;
        uniform float  iTime;
        uniform float  iOffset;

        half4 main(in vec2 fragCoord) {
            vec2 uv = fragCoord / iResolution;
            float shift = (iOffset / iResolution.x) * sin(iTime * 25.0);
            float colShift = sin(iTime * 15.0 + (fragCoord.x + shift * 100.0) * 0.05);
            
            vec3 cyan = vec3(0.0, 1.0, 0.82);
            vec3 pink = vec3(1.0, 0.3, 0.65);
            vec3 color = mix(cyan, pink, colShift * 0.5 + 0.5);
            
            return half4(color, 1.0);
        }
    """

    private var _cachedShader: Any? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader {
        if (_cachedShader == null) {
            _cachedShader = RuntimeShader(AGSL_SOURCE)
        }
        return _cachedShader as RuntimeShader
    }

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(shader: RuntimeShader, width: Float, height: Float, timeSec: Float, offset: Float = 6f) {
        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", timeSec)
        shader.setFloatUniform("iOffset", offset)
    }
}

