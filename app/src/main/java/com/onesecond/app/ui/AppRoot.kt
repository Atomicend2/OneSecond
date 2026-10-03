package com.onesecond.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onesecond.app.AppViewModel
import com.onesecond.app.Overlay

@Composable
fun AppRoot(vm: AppViewModel) {
    OneSecondTheme(dark = vm.darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize().appBackground(),
            color = Color.Transparent,
            contentColor = TextMain
        ) {
            if (!vm.onboarded) {
                OnboardingScreen(vm)
            } else {
                MainContent(vm)
            }
        }
    }
}

@Composable
private fun MainContent(vm: AppViewModel) {
    BackHandler(enabled = vm.stack.isEmpty() && vm.tab != 0) { vm.tab = 0 }
    BackHandler(enabled = vm.stack.isNotEmpty()) { vm.pop() }

    Box(Modifier.fillMaxSize()) {
        MainShell(vm)
        val top = vm.stack.lastOrNull()
        AnimatedContent(
            targetState = top,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "overlay"
        ) { screen ->
            when (screen) {
                null -> Box(Modifier)
                is Overlay.Capture -> CaptureScreen(vm)
                is Overlay.Detail -> DetailScreen(vm, screen.id)
                is Overlay.Month -> MonthScreen(vm, screen.year, screen.month)
                is Overlay.Story -> StoryScreen(vm, screen.year, screen.month)
                is Overlay.Privacy -> PrivacyScreen(vm)
                is Overlay.About -> AboutScreen(vm)
            }
        }
    }
}

@Composable
private fun MainShell(vm: AppViewModel) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomBar(vm.tab, { vm.tab = it }, vm::openCapture) }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (vm.tab) {
                0 -> TodayScreen(vm)
                1 -> MemoriesScreen(vm)
                2 -> MontagesScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}

@Composable
private fun BottomBar(tab: Int, setTab: (Int) -> Unit, onCapture: () -> Unit) {
    NavigationBar(containerColor = NavColor, tonalElevation = 0.dp) {
        NavItem("Today", Icons.Outlined.WbSunny, tab == 0) { setTab(0) }
        NavItem("Memories", Icons.Outlined.AutoStories, tab == 1) { setTab(1) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TextMain)
                    .clickable(onClickLabel = "New moment", role = Role.Button, onClick = onCapture),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "New moment", tint = Bg)
            }
        }
        NavItem("Montages", Icons.Outlined.Movie, tab == 2) { setTab(2) }
        NavItem("Settings", Icons.Outlined.Settings, tab == 3) { setTab(3) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontSize = 11.sp, maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Cyan, selectedTextColor = TextMain,
            unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
            indicatorColor = Color.Transparent
        )
    )
}
