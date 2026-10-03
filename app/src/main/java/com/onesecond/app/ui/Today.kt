package com.onesecond.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.Overlay
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import java.time.YearMonth

@Composable
fun TodayScreen(vm: AppViewModel) {
    val moments by vm.moments.collectAsStateWithLifecycle()
    val today = vm.today
    val todayMoment = moments.firstOrNull { it.epochDay == today.toEpochDay() }
    val month = YearMonth.from(today)
    val monthCount = moments.count { YearMonth.from(it.date) == month }
    val streak = remember(moments, today) { computeStreak(moments, today) }

    ScreenColumn {
        ScreenHeader(today.kickerLabel(), "Keep today.")
        Spacer(Modifier.height(24.dp))
        AppCard(shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                SectionLabel("Today's prompt")
                Spacer(Modifier.height(12.dp))
                Text(
                    Prompts.forDay(today.toEpochDay()),
                    fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(22.dp))
                if (todayMoment == null) {
                    EmptyCapture(onClick = vm::openCapture)
                } else {
                    MomentPreview(todayMoment, onClick = { vm.push(Overlay.Detail(todayMoment.id)) })
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = if (todayMoment == null) "Capture today's moment" else "Replace today's moment",
                    onClick = vm::openCapture
                )
                if (todayMoment != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "You can replace it until the day ends. Then it becomes a memory.",
                        color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(monthCount.toString(), "this month", Modifier.weight(1f))
            StatCard(moments.size.toString(), "in total", Modifier.weight(1f))
            if (streak >= 2) StatCard(streak.toString(), "days in a row", Modifier.weight(1f))
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun EmptyCapture(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(22.dp))
            .background(NoteBrush).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(Tile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Cyan, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.height(13.dp))
            Text("Nothing kept yet today", fontWeight = FontWeight.SemiBold)
            Text("A photo, a short video or a note", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MomentPreview(m: Moment, onClick: () -> Unit) {
    val bitmap by rememberMomentBitmap(m, 1080)
    Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick)) {
        if (m.type == MomentType.NOTE) {
            NoteSurface(m.caption, Modifier.fillMaxSize())
        } else {
            BitmapImage(bitmap, Modifier.fillMaxSize())
            if (m.type == MomentType.VIDEO) {
                Box(
                    Modifier.align(Alignment.Center).size(52.dp).clip(CircleShape).background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.PlayArrow, contentDescription = "Video", tint = Color.White) }
            }
            if (m.caption.isNotBlank()) {
                Box(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                        .padding(16.dp)
                ) {
                    Text(m.caption, color = Color.White, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
