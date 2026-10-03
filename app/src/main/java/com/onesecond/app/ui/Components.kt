package com.onesecond.app.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onesecond.app.data.Mood

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        content = content
    )
}

@Composable
fun ScreenHeader(kicker: String, title: String, trailing: String? = null) {
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(kicker.uppercase(), color = Cyan, fontSize = 11.sp, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 29.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
        }
        if (trailing != null) Text(trailing, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text.uppercase(), color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalPalette.current
    val border = BorderStroke(1.dp, palette.cardBorder)
    val elevation = if (palette.glass) 0.dp else 1.dp
    if (onClick == null) {
        Surface(
            modifier = modifier, shape = shape, color = palette.panel,
            shadowElevation = elevation, border = border, content = content
        )
    } else {
        Surface(
            onClick = onClick, modifier = modifier, shape = shape, color = palette.panel,
            shadowElevation = elevation, border = border, content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TextMain, contentColor = Bg,
            disabledContainerColor = Panel2, disabledContentColor = TextMuted
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MoodSelector(selected: Mood?, onSelect: (Mood?) -> Unit) {
    Column {
        Mood.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { m ->
                    val isSelected = m == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Cyan.copy(alpha = 0.16f) else Panel2)
                            .border(1.dp, if (isSelected) Cyan else Line, RoundedCornerShape(14.dp))
                            .clickable { onSelect(if (isSelected) null else m) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            m.label, fontSize = 13.sp,
                            color = if (isSelected) Cyan else TextMain,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Full-screen container used by every overlay screen. Handles system bars and the keyboard. */
@Composable
fun OverlayScreen(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
            .pointerInput(Unit) {}
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(),
        content = content
    )
}

@Composable
fun OverlayTopBar(
    title: String,
    onBack: () -> Unit,
    closeIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp).height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                if (closeIcon) Icons.Outlined.Close else Icons.Outlined.ChevronLeft,
                contentDescription = if (closeIcon) "Close" else "Back"
            )
        }
        Text(
            title, modifier = Modifier.weight(1f), fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        actions()
    }
}

@Composable
fun BitmapImage(bitmap: Bitmap?, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Crop) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = scale
        )
    } else {
        SkeletonBox(modifier)
    }
}

@Composable
fun NoteSurface(text: String, modifier: Modifier = Modifier, fontSize: Int = 22) {
    Box(
        modifier = modifier.background(NoteBrush).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.ifBlank { "A quiet day." },
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 8).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(color = Panel2, shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(17.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(label, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun BodyText(text: String) {
    Text(text, color = TextMuted, fontSize = 14.sp, lineHeight = 21.sp)
    Spacer(Modifier.height(14.dp))
}

@Composable
fun BodyHeading(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
}


// ---------- Skeleton loading ----------

/** Animated shimmer fill used while content is loading. */
@Composable
fun Modifier.shimmer(): Modifier {
    val p = LocalPalette.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val t by transition.animateFloat(
        initialValue = -700f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1300, easing = LinearEasing)),
        label = "shimmerOffset"
    )
    return this.background(
        Brush.linearGradient(
            colors = listOf(p.skeletonBase, p.skeletonHighlight, p.skeletonBase),
            start = Offset(t, 0f),
            end = Offset(t + 600f, 260f)
        )
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(0.dp)) {
    Box(modifier.clip(shape).shimmer())
}

/** Skeleton of the "compose a moment" step, shown while a photo/video is prepared or saved. */
@Composable
fun CaptureSkeleton(label: String) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        SkeletonBox(Modifier.fillMaxWidth().height(300.dp), RoundedCornerShape(22.dp))
        Spacer(Modifier.height(18.dp))
        SkeletonBox(Modifier.fillMaxWidth().height(84.dp), RoundedCornerShape(18.dp))
        Spacer(Modifier.height(18.dp))
        SkeletonBox(Modifier.width(180.dp).height(12.dp), RoundedCornerShape(6.dp))
        Spacer(Modifier.height(12.dp))
        repeat(2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { SkeletonBox(Modifier.weight(1f).height(42.dp), RoundedCornerShape(14.dp)) }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(label, color = TextMuted, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

/** A selectable look (Light / Dark) with a tiny live preview. Used in onboarding and Settings. */
@Composable
fun ThemeOption(
    title: String,
    subtitle: String,
    dark: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val pal = if (dark) GlassPalette else LightPalette
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Panel)
            .border(if (selected) 2.dp else 1.dp, if (selected) Cyan else Line, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(14.dp))
                .then(
                    if (dark) Modifier.background(
                        Brush.linearGradient(listOf(Color(0xFF0B0D1B), Color(0xFF2A2350)))
                    ) else Modifier.background(pal.bg)
                )
                .padding(12.dp)
        ) {
            Column(
                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                    .background(if (dark) Color(0x24FFFFFF) else pal.panel)
                    .border(1.dp, if (dark) Color(0x40FFFFFF) else pal.line, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Box(Modifier.width(34.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(pal.accent))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(pal.muted.copy(alpha = 0.5f)))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth(0.6f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(pal.muted.copy(alpha = 0.35f)))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(subtitle, color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
