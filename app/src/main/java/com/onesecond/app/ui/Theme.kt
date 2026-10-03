package com.onesecond.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.drawBehind

/**
 * One Second has two looks:
 *  - Light: the clean "vanilla" interface (flat white cards, soft shadows).
 *  - Dark: the glassmorphic interface (deep gradient, translucent frosted cards).
 * Screens never hard-code theme colours; they read the getters below.
 */
class Palette(
    val glass: Boolean,
    val bg: Color,
    val panel: Color,
    val panel2: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val line: Color,
    val danger: Color,
    val tile: Color,
    val dialog: Color,
    val nav: Color,
    val skeletonBase: Color,
    val skeletonHighlight: Color,
    val cardBorder: Brush,
    val note: Brush
)

val GlassPalette = Palette(
    glass = true,
    bg = Color(0xFF0A0C15),
    panel = Color(0x14FFFFFF),
    panel2 = Color(0x0DFFFFFF),
    text = Color(0xFFF2F2F0),
    muted = Color(0xFF9AA0B4),
    accent = Color(0xFF72E7FF),
    line = Color(0x26FFFFFF),
    danger = Color(0xFFFF8A80),
    tile = Color(0x2472E7FF),
    dialog = Color(0xFF1B1E2E),
    nav = Color(0x14FFFFFF),
    skeletonBase = Color(0x0FFFFFFF),
    skeletonHighlight = Color(0x2EFFFFFF),
    cardBorder = Brush.linearGradient(listOf(Color(0x59FFFFFF), Color(0x14FFFFFF))),
    note = Brush.linearGradient(listOf(Color(0x447C6CFF), Color(0x3372E7FF)))
)

val LightPalette = Palette(
    glass = false,
    bg = Color(0xFFF5F6FA),
    panel = Color(0xFFFFFFFF),
    panel2 = Color(0xFFEEF0F6),
    text = Color(0xFF14161C),
    muted = Color(0xFF667085),
    accent = Color(0xFF0A7EA4),
    line = Color(0x2014161C),
    danger = Color(0xFFC62828),
    tile = Color(0x1F0A7EA4),
    dialog = Color(0xFFFFFFFF),
    nav = Color(0xFFFFFFFF),
    skeletonBase = Color(0xFFE6E9F0),
    skeletonHighlight = Color(0xFFF7F8FB),
    cardBorder = SolidColor(Color(0x1A14161C)),
    note = Brush.linearGradient(listOf(Color(0xFFE2F3F9), Color(0xFFECE9FF)))
)

val LocalPalette = staticCompositionLocalOf { GlassPalette }

val Bg: Color
    @Composable get() = LocalPalette.current.bg
val Panel: Color
    @Composable get() = LocalPalette.current.panel
val Panel2: Color
    @Composable get() = LocalPalette.current.panel2
val TextMain: Color
    @Composable get() = LocalPalette.current.text
val TextMuted: Color
    @Composable get() = LocalPalette.current.muted
val Cyan: Color
    @Composable get() = LocalPalette.current.accent
val Line: Color
    @Composable get() = LocalPalette.current.line
val Danger: Color
    @Composable get() = LocalPalette.current.danger
val Tile: Color
    @Composable get() = LocalPalette.current.tile
val DialogColor: Color
    @Composable get() = LocalPalette.current.dialog
val NavColor: Color
    @Composable get() = LocalPalette.current.nav
val NoteBrush: Brush
    @Composable get() = LocalPalette.current.note

@Composable
fun OneSecondTheme(dark: Boolean, content: @Composable () -> Unit) {
    val palette = if (dark) GlassPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            background = palette.bg, surface = palette.bg, primary = palette.accent,
            onPrimary = palette.bg, onBackground = palette.text, onSurface = palette.text,
            outline = palette.line
        )
    } else {
        lightColorScheme(
            background = palette.bg, surface = palette.bg, primary = palette.accent,
            onPrimary = Color.White, onBackground = palette.text, onSurface = palette.text,
            outline = palette.line
        )
    }
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** Draws the app background: flat in Light, a deep gradient with soft glows in Dark (glass). */
@Composable
fun androidx.compose.ui.Modifier.appBackground(): androidx.compose.ui.Modifier {
    val p = LocalPalette.current
    return this.then(
        androidx.compose.ui.Modifier.drawBehind {
            drawRect(p.bg)
            if (p.glass) {
                drawRect(
                    Brush.verticalGradient(listOf(Color(0xFF0B0D1B), Color(0xFF121429), Color(0xFF0A0C15)))
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0x557C6CFF), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.08f),
                        radius = size.width * 0.9f
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0x3372E7FF), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.85f),
                        radius = size.width * 0.9f
                    )
                )
            }
        }
    )
}
