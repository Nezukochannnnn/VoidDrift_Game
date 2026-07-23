package com.voidrift.game.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── VoidDrift Typography — uses system sans-serif which maps to Roboto on Android
// For production: swap font resources with a Google Font (e.g. Inter, Outfit, Space Grotesk)
val VoidFontFamily = FontFamily.Default

val VoidTypography = Typography(

    // ── Display — Main titles, game over banners
    displayLarge = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Black,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing = (-0.25).sp,
        color        = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 45.sp,
        lineHeight   = 52.sp,
        letterSpacing = 0.sp,
        color        = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = 0.sp,
        color        = TextPrimary
    ),

    // ── Headline — Section headers, screen titles
    headlineLarge = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing = 2.sp,
        color        = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing = 1.sp,
        color        = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 32.sp,
        letterSpacing = 0.5.sp,
        color        = TextPrimary
    ),

    // ── Title — Card headers, dialog titles
    titleLarge = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.5.sp,
        color        = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp,
        color        = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
        color        = TextSecondary
    ),

    // ── Body — Regular text, descriptions
    bodyLarge = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.5.sp,
        color        = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.25.sp,
        color        = TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.4.sp,
        color        = TextSecondary
    ),

    // ── Label — Buttons, chips, HUD labels
    labelLarge = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 2.sp,
        color        = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 1.5.sp,
        color        = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily   = VoidFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 1.sp,
        color        = TextSecondary
    )
)
