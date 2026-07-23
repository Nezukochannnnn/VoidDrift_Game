package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * VoidSwirlShader — AGSL shader for background void swirling on SplashScreen and GameOverScreen.
 */
object VoidSwirlShader {

    const val AGSL_SOURCE = """
        uniform vec2  iResolution;
        uniform float iTime;

        float hash(vec2 p) {
            return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
        }

        float noise(vec2 p) {
            vec2 i = floor(p);
            vec2 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);
            return mix(
                mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
                mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x),
                f.y
            );
        }

        float fbm(vec2 p) {
            float v = 0.0;
            float a = 0.5;
            for (int i = 0; i < 4; i++) {
                v += a * noise(p);
                p *= 2.0;
                a *= 0.5;
            }
            return v;
        }

        half4 main(in vec2 fragCoord) {
            vec2 uv = (fragCoord - 0.5 * iResolution) / iResolution.y;
            float angle = iTime * 0.2;
            mat2 rot = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
            uv = rot * uv;

            float r = length(uv);
            float a = atan(uv.y, uv.x);

            float spiral = fbm(vec2(r * 4.0 - iTime * 0.3, a * 2.0 + r * 3.0));
            
            vec3 color = mix(
                vec3(0.04, 0.0, 0.1),
                vec3(0.0, 0.8, 0.7),
                smoothstep(0.3, 0.8, spiral * (1.0 - r))
            );
            color = mix(color, vec3(0.9, 0.2, 0.6), smoothstep(0.6, 0.9, spiral) * 0.5);

            return half4(color, 1.0);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader = RuntimeShader(AGSL_SOURCE)

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(shader: RuntimeShader, width: Float, height: Float, timeSec: Float) {
        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", timeSec)
    }
}
