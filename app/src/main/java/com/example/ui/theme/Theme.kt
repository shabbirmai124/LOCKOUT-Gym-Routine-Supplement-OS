package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class LockoutExtendedColors(
    val canvas: Color,
    val surface1: Color,
    val surface2: Color,
    val border: Color,
    val volt: Color,
    val onVolt: Color,
    val emerald: Color,
    val cyan: Color,
    val amber: Color,
    val crimson: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean
)

private val DarkExtended = LockoutExtendedColors(
    canvas = ObsidianCanvas,
    surface1 = ObsidianSurface1,
    surface2 = ObsidianSurface2,
    border = ObsidianBorder,
    volt = KineticVolt,
    onVolt = ObsidianCanvas,
    emerald = KineticEmerald,
    cyan = GlacierCyan,
    amber = AmberFlare,
    crimson = CrimsonPulse,
    textPrimary = OffWhitePrimary,
    textSecondary = CoolSteelSecondary,
    isDark = true
)

private val DaylightExtended = LockoutExtendedColors(
    canvas = DaylightCanvas,
    surface1 = DaylightSurface1,
    surface2 = DaylightSurface2,
    border = DaylightBorder,
    volt = DaylightAccent,
    onVolt = Color.White,
    emerald = DaylightEmerald,
    cyan = DaylightCyan,
    amber = DaylightAmber,
    crimson = DaylightCrimson,
    textPrimary = DaylightTextPrimary,
    textSecondary = DaylightTextSecondary,
    isDark = false
)

val LocalLockoutColors = staticCompositionLocalOf { DarkExtended }

private val LockoutDarkColorScheme = darkColorScheme(
    primary = KineticVolt,
    onPrimary = ObsidianCanvas,
    primaryContainer = ObsidianSurface2,
    onPrimaryContainer = KineticVolt,
    secondary = GlacierCyan,
    onSecondary = ObsidianCanvas,
    secondaryContainer = ObsidianSurface2,
    onSecondaryContainer = GlacierCyan,
    tertiary = AmberFlare,
    onTertiary = ObsidianCanvas,
    background = ObsidianCanvas,
    onBackground = OffWhitePrimary,
    surface = ObsidianSurface1,
    onSurface = OffWhitePrimary,
    surfaceVariant = ObsidianSurface2,
    onSurfaceVariant = CoolSteelSecondary,
    outline = ObsidianBorder,
    error = CrimsonPulse,
    onError = Color.White
)

private val LockoutLightColorScheme = lightColorScheme(
    primary = DaylightAccent,
    onPrimary = Color.White,
    primaryContainer = DaylightSurface2,
    onPrimaryContainer = DaylightAccent,
    secondary = DaylightCyan,
    onSecondary = Color.White,
    secondaryContainer = DaylightSurface2,
    onSecondaryContainer = DaylightCyan,
    tertiary = DaylightAmber,
    onTertiary = Color.White,
    background = DaylightCanvas,
    onBackground = DaylightTextPrimary,
    surface = DaylightSurface1,
    onSurface = DaylightTextPrimary,
    surfaceVariant = DaylightSurface2,
    onSurfaceVariant = DaylightTextSecondary,
    outline = DaylightBorder,
    error = DaylightCrimson,
    onError = Color.White
)

val LockoutShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun LockoutTheme(
    daylightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val extended = if (daylightMode) DaylightExtended else DarkExtended
    val colorScheme = if (daylightMode) LockoutLightColorScheme else LockoutDarkColorScheme

    CompositionLocalProvider(LocalLockoutColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LockoutTypography,
            shapes = LockoutShapes,
            content = content
        )
    }
}

object LockoutColors {
    val current: LockoutExtendedColors
        @Composable get() = LocalLockoutColors.current
}
