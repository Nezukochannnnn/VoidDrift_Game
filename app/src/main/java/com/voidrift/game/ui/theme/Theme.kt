package com.voidrift.game.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── VoidDrift Extended Color System ──────────────────────────────────────────
// Material 3 only exposes a subset of colors; we carry the rest in a custom
// CompositionLocal so every composable can reach brand-specific tokens.

@Immutable
data class VoidExtendedColors(
    /** Void corruption overlay on tiles */
    val voidCorrupt: Color,
    val voidCorruptLight: Color,
    val voidCorruptGlow: Color,

    /** Crystal collectible color */
    val crystalBlue: Color,
    val crystalBlueDim: Color,

    /** Enemy / danger accent */
    val neonPink: Color,
    val neonPinkContainer: Color,

    /** Torch light */
    val torchAmber: Color,
    val torchAmberContainer: Color,

    /** Dividers and outlines */
    val divider: Color,
    val outline: Color,
    val scrim: Color
)

val LocalVoidColors = staticCompositionLocalOf {
    VoidExtendedColors(
        voidCorrupt        = VoidCorrupt,
        voidCorruptLight   = VoidCorruptLight,
        voidCorruptGlow    = VoidCorruptGlow,
        crystalBlue        = CrystalBlue,
        crystalBlueDim     = CrystalBlueDim,
        neonPink           = NeonPink,
        neonPinkContainer  = NeonPinkContainer,
        torchAmber         = TorchAmber,
        torchAmberContainer = TorchAmberContainer,
        divider            = DividerColor,
        outline            = OutlineColor,
        scrim              = ScrimColor
    )
}

// ── Material 3 Dark Color Scheme ──────────────────────────────────────────────

private val VoidDarkColorScheme = darkColorScheme(
    // ── Primary — Player/Accent (Neon Cyan)
    primary             = NeonCyan,
    onPrimary           = TextOnPrimary,
    primaryContainer    = NeonCyanContainer,
    onPrimaryContainer  = NeonCyan,

    // ── Secondary — Enemies/Danger (Neon Pink)
    secondary           = NeonPink,
    onSecondary         = TextOnSecondary,
    secondaryContainer  = NeonPinkContainer,
    onSecondaryContainer = NeonPink,

    // ── Tertiary — Torch/Amber
    tertiary            = TorchAmber,
    onTertiary          = Color(0xFF1A1000),
    tertiaryContainer   = TorchAmberContainer,
    onTertiaryContainer = TorchAmber,

    // ── Background & Surface
    background          = VoidBackground,
    onBackground        = TextPrimary,
    surface             = VoidSurface,
    onSurface           = TextPrimary,
    surfaceVariant      = VoidSurfaceVariant,
    onSurfaceVariant    = TextSecondary,

    // ── Error
    error               = VoidError,
    onError             = Color(0xFF1A0000),
    errorContainer      = VoidErrorContainer,
    onErrorContainer    = VoidError,

    // ── Outline / Scrim
    outline             = OutlineColor,
    outlineVariant      = DividerColor,
    scrim               = ScrimColor,

    // ── Inverse (for snackbars etc.)
    inverseSurface      = TextPrimary,
    inverseOnSurface    = VoidBackground,
    inversePrimary      = NeonCyanDim
)

// ── Convenience extension — typed access to extended tokens ──────────────────

/** Access VoidDrift-specific extended colors anywhere in Compose. */
val MaterialTheme.voidColors: VoidExtendedColors
    @Composable get() = LocalVoidColors.current

// ── Root theme composable ─────────────────────────────────────────────────────

/**
 * Apply the VoidDrift Material 3 dark theme to the entire app.
 * Always forces dark mode — no light variant exists by design.
 */
@Composable
fun VoidDriftTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalVoidColors provides VoidExtendedColors(
            voidCorrupt         = VoidCorrupt,
            voidCorruptLight    = VoidCorruptLight,
            voidCorruptGlow     = VoidCorruptGlow,
            crystalBlue         = CrystalBlue,
            crystalBlueDim      = CrystalBlueDim,
            neonPink            = NeonPink,
            neonPinkContainer   = NeonPinkContainer,
            torchAmber          = TorchAmber,
            torchAmberContainer = TorchAmberContainer,
            divider             = DividerColor,
            outline             = OutlineColor,
            scrim               = ScrimColor
        )
    ) {
        MaterialTheme(
            colorScheme = VoidDarkColorScheme,
            typography  = VoidTypography,
            content     = content
        )
    }
}
