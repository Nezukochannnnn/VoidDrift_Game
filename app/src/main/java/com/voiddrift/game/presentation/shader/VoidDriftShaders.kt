package com.voiddrift.game.presentation.shader

object VoidDriftShaders {

    /**
     * AGSL Shader string for Warp Drive distortion and cosmic grid wave
     * Available on Android 13+ (API 33+) via Android RuntimeShader
     */
    const val WARP_GRID_AGSL = """
        uniform vec2 iResolution;
        uniform float iTime;
        uniform vec2 iPlayerPos;
        
        half4 main(in vec2 fragCoord) {
            vec2 uv = fragCoord / iResolution.xy;
            vec2 playerUv = iPlayerPos / iResolution.xy;
            
            // Calculate distance to player for gravitational void distortion
            float dist = distance(uv, playerUv);
            vec2 distortedUv = uv + (uv - playerUv) * sin(dist * 20.0 - iTime * 4.0) * 0.02;
            
            // Cosmic Grid pattern
            vec2 grid = abs(fract(distortedUv * vec2(20.0, 40.0) - vec2(0.0, iTime * 0.5)) - 0.5);
            float linePattern = smoothstep(0.48, 0.5, max(grid.x, grid.y));
            
            // Deep space void color gradient
            vec3 spaceBg = mix(vec3(0.02, 0.01, 0.06), vec3(0.08, 0.02, 0.18), distortedUv.y);
            vec3 cyanGlow = vec3(0.0, 0.8, 1.0) * linePattern * 0.6;
            vec3 purpleCore = vec3(0.6, 0.0, 0.9) * (1.0 - dist * 1.5);
            
            vec3 finalColor = spaceBg + cyanGlow + max(vec3(0.0), purpleCore);
            return half4(finalColor, 1.0);
        }
    """

    const val DAMAGE_FLASH_AGSL = """
        uniform vec2 iResolution;
        uniform float iIntensity;
        
        half4 main(in vec2 fragCoord) {
            vec2 uv = fragCoord / iResolution.xy;
            float vignette = length(uv - 0.5) * 1.4;
            vec3 redAlert = vec3(0.9, 0.05, 0.1) * vignette * iIntensity;
            return half4(redAlert, redAlert.r * 0.7);
        }
    """
}
