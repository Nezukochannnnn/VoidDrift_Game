package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * AGSL shader that renders animated void corruption spreading across tiles.
 *
 * For each fragment the shader samples voidProgress at nearby tile centers
 * using a noise-warped UV, producing organic creeping edges.
 *
 * Uniforms:
 *  - iResolution  (vec2):  canvas size in pixels
 *  - iTileSize    (float): world-space pixels per tile
 *  - iTime        (float): elapsed seconds
 *
 * The shader is composited at BlendMode.SrcOver over the tile layer with
 * the caller masking only corrupted tile regions before drawing.
 */
object VoidCorruptShader {

    const val AGSL_SOURCE = """
        uniform vec2  iResolution;
        uniform float iTileSize;
        uniform float iTime;

        // ── Noise helpers ─────────────────────────────────────────────────────
        float hash2(vec2 p) {
            p = fract(p * vec2(127.1, 311.7));
            p += dot(p, p + 19.19);
            return fract(p.x * p.y);
        }

        float smoothNoise(vec2 uv) {
            vec2 i = floor(uv);
            vec2 f = fract(uv);
            vec2 u = f * f * (3.0 - 2.0 * f);
            return mix(
                mix(hash2(i),             hash2(i + vec2(1,0)), u.x),
                mix(hash2(i + vec2(0,1)), hash2(i + vec2(1,1)), u.x),
                u.y
            );
        }

        float fbm(vec2 p) {
            float v = 0.0; float a = 0.5;
            for (int i = 0; i < 5; i++) {
                v += a * smoothNoise(p); p *= 2.1; a *= 0.5;
            }
            return v;
        }
        // ─────────────────────────────────────────────────────────────────────

        half4 main(in vec2 fragCoord) {
            vec2 uv = fragCoord / iTileSize;

            // Warp UV with animated FBM for organic edge
            vec2 warpOffset = vec2(
                fbm(uv + vec2(iTime * 0.18, 0.0)),
                fbm(uv + vec2(0.0, iTime * 0.14))
            ) * 0.6;

            float noise = fbm(uv + warpOffset + iTime * 0.05);

            // Pulsing dark-purple void core with cyan void-veins
            float pulse = 0.5 + 0.5 * sin(iTime * 2.4 + noise * 6.28);
            vec3 voidCore  = vec3(0.08, 0.0,  0.18);
            vec3 voidVein  = vec3(0.0,  0.55, 0.85);
            vec3 voidEdge  = vec3(0.3,  0.0,  0.5);

            float veinMask = smoothstep(0.62, 0.68, noise);
            vec3  color    = mix(voidCore, voidEdge, pulse);
            color          = mix(color, voidVein, veinMask);

            // Alpha driven by noise so edges are organic, not rectangular
            float alpha = smoothstep(0.35, 0.65, noise) * 0.92;

            return half4(color * alpha, alpha);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader = RuntimeShader(AGSL_SOURCE)

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(
        shader: RuntimeShader,
        screenW: Float,
        screenH: Float,
        tileSize: Float,
        timeSec: Float
    ) {
        shader.setFloatUniform("iResolution", screenW, screenH)
        shader.setFloatUniform("iTileSize",   tileSize)
        shader.setFloatUniform("iTime",       timeSec)
    }
}
