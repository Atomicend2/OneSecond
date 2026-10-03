package com.onesecond.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF090B10)
private val Panel = Color(0xFF11141B)
private val Panel2 = Color(0xFF171B24)
private val TextMain = Color(0xFFF2F2F0)
private val TextMuted = Color(0xFF8E94A3)
private val Cyan = Color(0xFF72E7FF)
private val Violet = Color(0xFF9D8CFF)
private val Line = Color(0x22FFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OneSecond() }
    }
}

@Composable
fun OneSecond() {
    var tab by remember { mutableStateOf(0) }
    var captureOpen by remember { mutableStateOf(false) }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg, surface = Panel, primary = Cyan,
            onPrimary = Bg, onBackground = TextMain, onSurface = TextMain,
            outline = Line
        )
    ) {
        AnimatedContent(targetState = captureOpen, label = "capture") { open ->
            if (open) CaptureSheet(onClose = { captureOpen = false })
            else AppShell(tab, { tab = it }, { captureOpen = true })
        }
    }
}

@Composable
fun AppShell(tab: Int, setTab: (Int) -> Unit, capture: () -> Unit) {
    Scaffold(
        containerColor = Bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = capture,
                containerColor = TextMain,
                contentColor = Bg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(58.dp)
            ) { Icon(Icons.Outlined.Add, "Capture") }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xE6090B10), tonalElevation = 0.dp) {
                Nav("Today", Icons.Outlined.WbSunny, tab == 0) { setTab(0) }
                Nav("Memories", Icons.Outlined.AutoStories, tab == 1) { setTab(1) }
                Spacer(Modifier.width(54.dp))
                Nav("Montages", Icons.Outlined.Movie, tab == 2) { setTab(2) }
                Nav("Profile", Icons.Outlined.PersonOutline, tab == 3) { setTab(3) }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when(tab) {
                0 -> Today()
                1 -> Memories()
                2 -> Montages()
                else -> Profile()
            }
        }
    }
}

@Composable
fun RowScope.Nav(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, click: () -> Unit) {
    NavigationBarItem(
        selected = selected, onClick = click,
        icon = { Icon(icon, null) }, label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Cyan, selectedTextColor = TextMain,
            unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
            indicatorColor = Color.Transparent
        )
    )
}

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), content = content)
}

@Composable
fun Header(kicker: String, title: String, action: String? = null) {
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(kicker.uppercase(), color = Cyan, fontSize = 11.sp, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 29.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-.5).sp)
        }
        if (action != null) Text(action, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun Today() {
    ScreenColumn {
        Header("Friday · October 2", "Keep today.")
        Spacer(Modifier.height(25.dp))
        Surface(
            color = Panel, shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp)) {
                Text("TODAY'S PROMPT", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "What is one little thing\nworth remembering today?",
                    fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(23.dp))
                Box(
                    Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1D2630), Color(0xFF15111F)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(58.dp).clip(CircleShape).background(Color(0xFF202833)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CameraAlt, null, tint = Cyan, modifier = Modifier.size(25.dp))
                        }
                        Spacer(Modifier.height(13.dp))
                        Text("Capture your moment", fontWeight = FontWeight.SemiBold)
                        Text("One photo or a short video", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextMain, contentColor = Bg)
                ) { Text("Capture today's moment", fontWeight = FontWeight.SemiBold) }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("02", "moments", Modifier.weight(1f))
            StatCard("01", "month", Modifier.weight(1f))
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier) {
    Surface(color = Panel2, shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(17.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(label, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun Memories() {
    ScreenColumn {
        Header("Your archive", "Memories", "October 2026")
        Spacer(Modifier.height(24.dp))
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Line)) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("OCTOBER", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("2 / 31", color = Cyan, fontSize = 12.sp)
                }
                Spacer(Modifier.height(18.dp))
                val days = (1..31).toList()
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        week.forEach { d ->
                            Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (d <= 2) Color(0xFF24333A) else Panel2),
                                contentAlignment = Alignment.Center) {
                                Text(d.toString(), color = if (d <= 2) Cyan else TextMuted, fontSize = 11.sp)
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(7.dp))
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("RECENT", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        MemoryItem("02", "October 2", "Today's moment")
        MemoryItem("01", "October 1", "First day of October")
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun MemoryItem(day: String, date: String, caption: String) {
    Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(62.dp).clip(RoundedCornerShape(15.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF26343A), Color(0xFF211C2B)))),
                contentAlignment = Alignment.Center) {
                Text(day, color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(date, color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(3.dp))
                Text(caption, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = TextMuted)
        }
    }
}

@Composable
fun Montages() {
    ScreenColumn {
        Header("Your story", "Montages")
        Spacer(Modifier.height(24.dp))
        Surface(
            color = Panel, shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth().height(355.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF15272E), Color(0xFF191323)))
                ))
                Column(Modifier.align(Alignment.BottomStart).padding(23.dp)) {
                    Text("OCTOBER 2026", color = Cyan, fontSize = 11.sp, letterSpacing = 1.7.sp)
                    Spacer(Modifier.height(7.dp))
                    Text("A month in moments.", color = TextMain, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("2 moments captured so far", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(51.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextMain, contentColor = Bg)) {
            Icon(Icons.Outlined.PlayArrow, null)
            Spacer(Modifier.width(7.dp))
            Text("Preview montage", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun Profile() {
    ScreenColumn {
        Header("Your account", "Profile")
        Spacer(Modifier.height(24.dp))
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Line)) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Cyan, Violet))),
                    contentAlignment = Alignment.Center) {
                    Text("A", color = Bg, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(15.dp))
                Column {
                    Text("Atomic", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("2 moments · October 2026", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        listOf(
            "Account" to Icons.Outlined.PersonOutline,
            "Notifications" to Icons.Outlined.NotificationsNone,
            "Appearance" to Icons.Outlined.DarkMode,
            "Privacy" to Icons.Outlined.Lock,
            "About One Second" to Icons.Outlined.Info
        ).forEach { (name, icon) ->
            Row(Modifier.fillMaxWidth().clickable {}.padding(vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = TextMuted)
                Spacer(Modifier.width(15.dp))
                Text(name, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Icon(Icons.Outlined.ChevronRight, null, tint = TextMuted)
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)) { Text("Sign out") }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun CaptureSheet(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("New moment", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close") }
        }
        Spacer(Modifier.height(22.dp))
        Surface(color = Panel, shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth().weight(1f)) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(76.dp).clip(CircleShape).background(Color(0xFF1D2930)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CameraAlt, null, tint = Cyan, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Capture your second", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(7.dp))
                    Text("Take a photo or record a short video.", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.PhotoLibrary, null); Spacer(Modifier.width(7.dp)); Text("Library")
            }
            Button(onClick = {}, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextMain, contentColor = Bg)) {
                Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(7.dp)); Text("Camera")
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
