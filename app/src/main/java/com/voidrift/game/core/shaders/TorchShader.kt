package com.voidrift.game.core.shaders

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * AGSL shader that renders a dynamic torch-light radial gradient
 * with flickering and penumbra softening.
 *
 * Requires API 33 (Android 13 / TIRAMISU) for [RuntimeShader].
 * Callers must guard with [Build.VERSION.SDK_INT] >= [Build.VERSION_CODES.TIRAMISU].
 */
object TorchShader {

    /**
     * AGSL source for the torch-light effect.
     *
     * Uniforms:
     *  - iResolution (vec2): canvas size in pixels
     *  - iPlayerPos  (vec2): player world-space position projected to screen
     *  - iTorchRadius(float): current torch visibility radius
     *  - iTime       (float): elapsed seconds (for flicker)
     */
    const val AGSL_SOURCE = """
        uniform vec2  iResolution;
        uniform vec2  iPlayerPos;
        uniform float iTorchRadius;
        uniform float iTime;

        // Pseudo-random value from seed
        float hash(float n) { return fract(sin(n) * 43758.5453); }

        // 1D fractional brownian motion — organic flicker
        float fbm(float t) {
            float v = 0.0;
            float a = 0.5;
            for (int i = 0; i < 4; i++) {
                v += a * (hash(floor(t)) + fract(t) * (hash(floor(t) + 1.0) - hash(floor(t))));
                t *= 2.0; a *= 0.5;
            }
            return v;
        }

        half4 main(in vec2 fragCoord) {
            // Convert fragCoord to match screen space (Y flipped)
            vec2 uv    = fragCoord;
            vec2 pPos  = iPlayerPos;
            pPos.y     = iResolution.y - pPos.y;   // Compose canvas Y-flip correction

            float dist = distance(uv, pPos);

            // Flicker: modulate radius ±6% using fbm
            float flicker = 1.0 + 0.06 * (fbm(iTime * 7.0) - 0.5) * 2.0;
            float radius  = iTorchRadius * flicker;

            // Smooth penumbra falloff in [0.6r … 1.0r]
            float edge    = 1.0 - smoothstep(radius * 0.6, radius, dist);

            // Warm torch colour: bright amber core → dim orange edge → near-black void
            vec3 warmCore  = vec3(1.0,  0.85, 0.45);
            vec3 outerGlow = vec3(0.55, 0.25, 0.05);
            vec3 voidDark  = vec3(0.01, 0.01, 0.02);

            float innerT  = 1.0 - smoothstep(0.0, radius * 0.4, dist);
            vec3  color   = mix(outerGlow, warmCore, innerT) * edge;
            color         = mix(voidDark, color, edge);

            // Alpha — fully opaque everywhere; compose via BlendMode in Compose
            return half4(color, 1.0);
        }
    """

    /** Create and return a ready-to-use [RuntimeShader] (API 33+). */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun create(): RuntimeShader = RuntimeShader(AGSL_SOURCE)

    /** True when the current device supports AGSL shaders. */
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** Update all uniforms on an already-created shader (API 33+). */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setUniforms(
        shader: RuntimeShader,
        screenW: Float,
        screenH: Float,
        playerScreenX: Float,
        playerScreenY: Float,
        torchRadius: Float,
        timeSec: Float
    ) {
        shader.setFloatUniform("iResolution",   screenW,        screenH)
        shader.setFloatUniform("iPlayerPos",    playerScreenX,  playerScreenY)
        shader.setFloatUniform("iTorchRadius",  torchRadius)
        shader.setFloatUniform("iTime",         timeSec)
    }
}
