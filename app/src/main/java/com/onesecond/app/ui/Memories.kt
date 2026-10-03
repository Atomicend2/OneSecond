package com.onesecond.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.Overlay
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun MemoriesScreen(vm: AppViewModel) {
    val moments by vm.moments.collectAsStateWithLifecycle()
    val current = YearMonth.from(vm.today)
    var ym by remember { mutableStateOf(current) }
    val monthMoments = remember(moments, ym) { moments.filter { YearMonth.from(it.date) == ym } }
    val byDay = remember(monthMoments) { monthMoments.associateBy { it.epochDay } }
    val locale = Locale.getDefault()
    val firstDow: DayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val offset = (ym.atDay(1).dayOfWeek.value - firstDow.value + 7) % 7
    val leading: List<Int?> = List(offset) { null }
    val days: List<Int?> = (1..ym.lengthOfMonth()).toList()
    val cells: List<Int?> = leading + days
    val padded: List<Int?> = cells + List((7 - cells.size % 7) % 7) { null }

    ScreenColumn {
        ScreenHeader("Your archive", "Memories")
        Spacer(Modifier.height(20.dp))
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { ym = ym.minusMonths(1) }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous month")
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ym.titleLabel(), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(
                            "${monthMoments.size} of ${ym.lengthOfMonth()} days",
                            color = Cyan, fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { ym = ym.plusMonths(1) }, enabled = ym.isBefore(current)) {
                        Icon(
                            Icons.Outlined.ChevronRight, contentDescription = "Next month",
                            tint = if (ym.isBefore(current)) TextMain else TextMuted.copy(alpha = 0.4f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0 until 7).forEach { i ->
                        Text(
                            firstDow.plus(i.toLong()).getDisplayName(TextStyle.NARROW, locale),
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            color = TextMuted, fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                padded.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            val epoch = day?.let { ym.atDay(it).toEpochDay() }
                            DayCell(
                                day = day,
                                moment = epoch?.let { byDay[it] },
                                isToday = epoch == vm.today.toEpochDay(),
                                isFuture = epoch != null && epoch > vm.today.toEpochDay(),
                                modifier = Modifier.weight(1f),
                                onClick = { m -> vm.push(Overlay.Detail(m.id)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        SectionLabel(if (ym == current) "This month" else ym.titleLabel())
        Spacer(Modifier.height(10.dp))
        if (monthMoments.isEmpty()) {
            EmptyState(
                "Nothing kept this month",
                "Missing a day is completely fine. Your next moment will appear here."
            )
        } else {
            monthMoments.forEach { m ->
                MemoryRow(m) { vm.push(Overlay.Detail(m.id)) }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun DayCell(
    day: Int?,
    moment: Moment?,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier,
    onClick: (Moment) -> Unit
) {
    if (day == null) {
        Box(modifier.aspectRatio(1f))
        return
    }
    val shape = RoundedCornerShape(10.dp)
    var cell = modifier.aspectRatio(1f).clip(shape)
    cell = if (isToday) cell.border(1.dp, Cyan, shape) else cell
    if (moment != null) {
        val bitmap by rememberMomentBitmap(moment, 200)
        Box(cell.clickable { onClick(moment) }) {
            if (moment.type == MomentType.NOTE) {
                Box(Modifier.fillMaxSize().background(NoteBrush))
            } else {
                BitmapImage(bitmap, Modifier.fillMaxSize())
            }
            Box(Modifier.fillMaxSize().background(Color(0x66000000)))
            Text(
                day.toString(), color = Color.White, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        Box(cell.background(Panel2), contentAlignment = Alignment.Center) {
            Text(
                day.toString(),
                color = if (isFuture) TextMuted.copy(alpha = 0.4f) else TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MemoryRow(m: Moment, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val bitmap by rememberMomentBitmap(m, 200)
            Box(Modifier.size(62.dp).clip(RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                if (m.type == MomentType.NOTE) {
                    Box(Modifier.fillMaxSize().background(NoteBrush), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.EditNote, contentDescription = "Note", tint = Cyan)
                    }
                } else {
                    BitmapImage(bitmap, Modifier.fillMaxSize())
                    if (m.type == MomentType.VIDEO) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = "Video", tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(m.date.shortLabel(), color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(3.dp))
                Text(
                    m.caption.ifBlank {
                        when (m.type) {
                            MomentType.PHOTO -> "Photo"
                            MomentType.VIDEO -> "Video"
                            MomentType.NOTE -> "Note"
                        }
                    },
                    fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
