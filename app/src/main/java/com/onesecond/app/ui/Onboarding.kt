package com.onesecond.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onesecond.app.AppViewModel
import com.onesecond.app.R

private data class Page(val title: String, val body: String)

private val pages = listOf(
    Page(
        "One moment a day.",
        "Keep a photo, a short video or a few words. Just one small piece of today."
    ),
    Page(
        "Private by default.",
        "Your moments stay on this phone. No account, no feed, no followers. Missing a day is fine."
    ),
    Page(
        "A story every month.",
        "At the end of the month your moments become a story you can play back and share, if you choose."
    )
)

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    val onDone = vm::completeOnboarding
    var index by rememberSaveable { mutableIntStateOf(0) }
    val lookStep = pages.size
    val last = index == lookStep
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).padding(horizontal = 28.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (!last) TextButton(onClick = onDone) { Text("Skip", color = TextMuted) }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.brand_mark),
                contentDescription = "One Second",
                modifier = Modifier.size(if (last) 96.dp else 132.dp).clip(RoundedCornerShape(26.dp))
            )
            Spacer(Modifier.height(40.dp))
            Crossfade(targetState = index, label = "onboarding") { i ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val title = if (i < lookStep) pages[i].title else "Choose your look"
                    val body = if (i < lookStep) pages[i].body else "You can change this any time in Settings."
                    Text(
                        title, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center, letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        body, color = TextMuted, fontSize = 16.sp,
                        lineHeight = 24.sp, textAlign = TextAlign.Center
                    )
                    if (i == lookStep) {
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ThemeOption(
                                title = "Light", subtitle = "Clean and bright", dark = false,
                                selected = !vm.darkTheme, modifier = Modifier.weight(1f)
                            ) { vm.chooseDarkTheme(false) }
                            ThemeOption(
                                title = "Dark", subtitle = "Frosted glass", dark = true,
                                selected = vm.darkTheme, modifier = Modifier.weight(1f)
                            ) { vm.chooseDarkTheme(true) }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            (0..lookStep).forEach { i ->
                Box(
                    Modifier.padding(4.dp).size(if (i == index) 18.dp else 7.dp, 7.dp)
                        .clip(CircleShape).background(if (i == index) Cyan else Line)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = if (last) "Get started" else "Continue",
            onClick = { if (last) onDone() else index += 1 }
        )
        Spacer(Modifier.height(24.dp))
    }
}
