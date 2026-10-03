package com.onesecond.app.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.Overlay
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import com.onesecond.app.media.Collage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

private fun momentsOf(all: List<Moment>, year: Int, month: Int): List<Moment> {
    val ym = YearMonth.of(year, month)
    return all.filter { YearMonth.from(it.date) == ym }.sortedBy { it.epochDay }
}

@Composable
fun MontagesScreen(vm: AppViewModel) {
    val moments by vm.moments.collectAsStateWithLifecycle()
    val months = remember(moments) {
        moments.groupBy { YearMonth.from(it.date) }.toSortedMap(compareByDescending<YearMonth> { it })
    }
    ScreenColumn {
        ScreenHeader("Your story", "Montages")
        Spacer(Modifier.height(24.dp))
        if (months.isEmpty()) {
            EmptyState(
                "Your first story starts with one moment",
                "Keep a moment today and it will appear here as part of this month's story."
            )
        } else {
            for ((ym, list) in months) {
                MonthCard(ym, list) { vm.push(Overlay.Month(ym.year, ym.monthValue)) }
                Spacer(Modifier.height(16.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun MonthCard(ym: YearMonth, list: List<Moment>, onClick: () -> Unit) {
    val cover = list.maxByOrNull { it.epochDay } ?: return
    val bitmap by rememberMomentBitmap(cover, 720)
    AppCard(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        shape = RoundedCornerShape(28.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            if (cover.type == MomentType.NOTE) {
                NoteSurface(cover.caption, Modifier.fillMaxSize(), 20)
            } else {
                BitmapImage(bitmap, Modifier.fillMaxSize())
            }
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD090B10))))
            )
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text("MONTAGE", color = Color(0xFF72E7FF), fontSize = 11.sp, letterSpacing = 1.7.sp)
                Spacer(Modifier.height(6.dp))
                Text(ym.titleLabel(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(countLabel(list.size), color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MonthScreen(vm: AppViewModel, year: Int, month: Int) {
    val all by vm.moments.collectAsStateWithLifecycle()
    val ym = YearMonth.of(year, month)
    val items = remember(all, year, month) { momentsOf(all, year, month) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }

    OverlayScreen {
        OverlayTopBar(ym.titleLabel(), onBack = vm::pop)
        if (items.isEmpty()) {
            EmptyState("Nothing here", "This month has no moments.")
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Text(countLabel(items.size), color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                items.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { m ->
                            val bitmap by rememberMomentBitmap(m, 300)
                            Box(
                                Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                            ) {
                                if (m.type == MomentType.NOTE) {
                                    NoteSurface(m.caption, Modifier.fillMaxSize(), 11)
                                } else {
                                    BitmapImage(bitmap, Modifier.fillMaxSize())
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                PrimaryButton(
                    text = "Play story",
                    icon = Icons.Outlined.PlayArrow,
                    onClick = { vm.push(Overlay.Story(year, month)) }
                )
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = if (sharing) "Preparing…" else "Share month card",
                    icon = Icons.Outlined.Share,
                    enabled = !sharing,
                    onClick = {
                        sharing = true
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    Collage.render(context, items, ym.titleLabel(), "%04d-%02d".format(year, month))
                                }
                            }
                            sharing = false
                            result
                                .onSuccess { file ->
                                    try {
                                        shareFile(context, file, "image/jpeg")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Couldn't open sharing.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .onFailure {
                                    Toast.makeText(context, "Couldn't prepare the month card.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }
        }
    }
}

private const val STORY_STEP_MS = 3500

@Composable
fun StoryScreen(vm: AppViewModel, year: Int, month: Int) {
    val all by vm.moments.collectAsStateWithLifecycle()
    val items = remember(all, year, month) { momentsOf(all, year, month) }
    if (items.isEmpty()) {
        LaunchedEffect(Unit) { vm.pop() }
        return
    }
    var index by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(index) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = STORY_STEP_MS, easing = LinearEasing))
        if (index < items.lastIndex) index += 1 else vm.pop()
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black).pointerInput(items.size) {
            detectTapGestures { offset ->
                if (offset.x < size.width / 3f) {
                    if (index > 0) index -= 1
                } else {
                    if (index < items.lastIndex) index += 1 else vm.pop()
                }
            }
        }
    ) {
        Crossfade(targetState = index.coerceIn(0, items.lastIndex), label = "story") { i ->
            StoryPage(items[i])
        }
        Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.systemBars).padding(horizontal = 12.dp)) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                items.indices.forEach { i ->
                    val fraction = when {
                        i < index -> 1f
                        i == index -> progress.value
                        else -> 0f
                    }
                    Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(Color(0x44FFFFFF))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Color.White))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = vm::pop) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close story", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StoryPage(m: Moment) {
    val bitmap by rememberMomentBitmap(m, 1080)
    Box(Modifier.fillMaxSize()) {
        if (m.type == MomentType.NOTE) {
            NoteSurface(m.caption, Modifier.fillMaxSize(), 26)
        } else {
            BitmapImage(bitmap, Modifier.fillMaxSize(), ContentScale.Crop)
        }
        Box(
            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text(m.date.longLabel(), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                if (m.caption.isNotBlank() && m.type != MomentType.NOTE) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        m.caption, color = Color.White, fontSize = 20.sp, lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium, maxLines = 4, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
