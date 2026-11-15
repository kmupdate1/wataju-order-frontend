package org.b3.wataju.shared.ui

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

val LightPalette = lightColorScheme(
    primary = Color(0xFF425C43),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDFEADB),
    onPrimaryContainer = Color(0xFF1C3422),
    secondary = Color(0xFF775437),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F5EF),
    onBackground = Color(0xFF262D26),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF262D26),
    surfaceVariant = Color(0xFFEFEEE6),
    onSurfaceVariant = Color(0xFF4E584F),
    outline = Color(0xFF747F74),
    error = Color(0xFFAA3131),
)

val DarkPalette = darkColorScheme(
    primary = Color(0xFFBAD5B6),
    onPrimary = Color(0xFF213B27),
    primaryContainer = Color(0xFF354E3A),
    onPrimaryContainer = Color(0xFFDDEEDD),
    secondary = Color(0xFFDEC0A0),
    onSecondary = Color(0xFF472D19),
    background = Color(0xFF171D19),
    onBackground = Color(0xFFE7EBE3),
    surface = Color(0xFF202822),
    onSurface = Color(0xFFE7EBE3),
    surfaceVariant = Color(0xFF354138),
    onSurfaceVariant = Color(0xFFC6D1C5),
    outline = Color(0xFFA1AEA0),
    error = Color(0xFFFFB4AB),
)


internal fun storeTypography(font: androidx.compose.ui.text.font.FontFamily?): Typography {
    val base = Typography()
    if (font == null) return base
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = font),
        displayMedium = base.displayMedium.copy(fontFamily = font),
        displaySmall = base.displaySmall.copy(fontFamily = font),
        headlineLarge = base.headlineLarge.copy(fontFamily = font),
        headlineMedium = base.headlineMedium.copy(fontFamily = font),
        headlineSmall = base.headlineSmall.copy(fontFamily = font),
        titleLarge = base.titleLarge.copy(fontFamily = font),
        titleMedium = base.titleMedium.copy(fontFamily = font),
        titleSmall = base.titleSmall.copy(fontFamily = font),
        bodyLarge = base.bodyLarge.copy(fontFamily = font),
        bodyMedium = base.bodyMedium.copy(fontFamily = font),
        bodySmall = base.bodySmall.copy(fontFamily = font),
        labelLarge = base.labelLarge.copy(fontFamily = font),
        labelMedium = base.labelMedium.copy(fontFamily = font),
        labelSmall = base.labelSmall.copy(fontFamily = font),
    )
}
