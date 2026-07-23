package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * ChromaticAberrationShader — AGSL shader for GameOverScreen title text glitch effect on API 33+.
 */
object ChromaticAberrationShader {

    const val AGSL_SOURCE = """
        uniform shader contents;
        uniform vec2   iResolution;
        uniform float  iTime;
        uniform float  iOffset;

        half4 main(in vec2 fragCoord) {
            float shift = iOffset * sin(iTime * 25.0);
            half4 r = contents.eval(fragCoord + vec2(shift, 0.0));
            half4 g = contents.eval(fragCoord);
            half4 b = contents.eval(fragCoord - vec2(shift, 0.0));
            return half4(r.r, g.g, b.b, max(max(r.a, g.a), b.a));
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader = RuntimeShader(AGSL_SOURCE)

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(shader: RuntimeShader, width: Float, height: Float, timeSec: Float, offset: Float = 6f) {
        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", timeSec)
        shader.setFloatUniform("iOffset", offset)
    }
}
