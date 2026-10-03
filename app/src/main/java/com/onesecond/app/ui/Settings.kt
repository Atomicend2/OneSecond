package com.onesecond.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.BuildConfig
import com.onesecond.app.Overlay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val moments by vm.moments.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            vm.setReminderEnabled(true)
        } else {
            Toast.makeText(
                context,
                "Allow notifications for One Second in your phone's settings to get reminders.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val timeLabel = remember(vm.reminderHour, vm.reminderMinute) {
        LocalTime.of(vm.reminderHour, vm.reminderMinute)
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()))
    }

    ScreenColumn {
        ScreenHeader("Your space", "Settings")
        Spacer(Modifier.height(20.dp))
        AppCard {
            Column(Modifier.padding(20.dp)) {
                Text("One Second", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${countLabel(moments.size)} kept on this phone",
                    color = TextMuted, fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        SectionLabel("Appearance")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOption(
                title = "Light", subtitle = "Clean and bright", dark = false,
                selected = !vm.darkTheme, modifier = Modifier.weight(1f)
            ) { vm.setDarkTheme(false) }
            ThemeOption(
                title = "Dark", subtitle = "Frosted glass", dark = true,
                selected = vm.darkTheme, modifier = Modifier.weight(1f)
            ) { vm.setDarkTheme(true) }
        }
        Spacer(Modifier.height(24.dp))
        SectionLabel("Daily reminder")
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = TextMuted)
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text("Remind me", fontWeight = FontWeight.Medium)
                Text("A gentle nudge if you haven't kept today yet", color = TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = vm.reminderEnabled,
                onCheckedChange = { on ->
                    if (!on) {
                        vm.setReminderEnabled(false)
                    } else if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.setReminderEnabled(true)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Bg, checkedTrackColor = Cyan,
                    uncheckedThumbColor = TextMuted, uncheckedTrackColor = Panel2,
                    uncheckedBorderColor = Line
                )
            )
        }
        if (vm.reminderEnabled) {
            SettingsRow(Icons.Outlined.Schedule, "Reminder time", timeLabel) {
                TimePickerDialog(
                    context,
                    if (vm.darkTheme) android.R.style.Theme_Material_Dialog else android.R.style.Theme_Material_Light_Dialog,
                    { _, h, m -> vm.setReminderTime(h, m) },
                    vm.reminderHour,
                    vm.reminderMinute,
                    DateFormat.is24HourFormat(context)
                ).show()
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionLabel("About your data")
        Spacer(Modifier.height(6.dp))
        SettingsRow(Icons.Outlined.Lock, "Privacy", null) { vm.push(Overlay.Privacy) }
        SettingsRow(Icons.Outlined.Info, "About One Second", null) { vm.push(Overlay.About) }
        SettingsRow(Icons.Outlined.DeleteForever, "Delete all my data", null, danger = true) { confirmDelete = true }
        Spacer(Modifier.height(30.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = DialogColor,
            titleContentColor = TextMain,
            textContentColor = TextMuted,
            title = { Text("Delete all your moments?") },
            text = { Text("Every photo, video and note will be permanently removed from this phone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAllData()
                }) { Text("Delete everything", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = TextMain) }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String?,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) Danger else TextMuted)
        Spacer(Modifier.width(15.dp))
        Text(
            title, Modifier.weight(1f), fontWeight = FontWeight.Medium,
            color = if (danger) Danger else Color.Unspecified
        )
        if (value != null) {
            Text(value, color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}

@Composable
fun PrivacyScreen(vm: AppViewModel) {
    OverlayScreen {
        OverlayTopBar("Privacy", onBack = vm::pop)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp)) {
            BodyHeading("Your moments stay on your phone")
            BodyText(
                "One Second doesn't have accounts, servers or cloud storage. The photos, videos, notes and moods you keep " +
                    "are stored privately inside the app on this device, and nowhere else."
            )
            BodyHeading("No tracking")
            BodyText(
                "There are no ads, no analytics and no third-party trackers in the app. We don't collect or sell any data about you."
            )
            BodyHeading("Permissions")
            BodyText(
                "Photos and videos are taken with your phone's camera app or chosen with the system photo picker, " +
                    "so One Second never needs access to your camera or your whole library. " +
                    "Notifications are only used for the daily reminder, and only if you turn it on."
            )
            BodyHeading("Sharing is always your choice")
            BodyText(
                "Nothing is shared unless you tap Share. When you do, your phone's share sheet decides where it goes."
            )
            BodyHeading("Deleting your data")
            BodyText(
                "You can delete a single moment from its page, or everything from Settings. " +
                    "Uninstalling the app also removes everything it stored."
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun AboutScreen(vm: AppViewModel) {
    OverlayScreen {
        OverlayTopBar("About One Second", onBack = vm::pop)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("One Second", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Keep a little piece of today.", color = Cyan, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            BodyText(
                "Each day, keep one small moment: a photo, a short video or a few words. " +
                    "At the end of the month, your moments come together as a story you can play back and share."
            )
            BodyText("Missing a day is allowed. There are no streak penalties and no pressure.")
            Spacer(Modifier.height(10.dp))
            Text("Version ${BuildConfig.VERSION_NAME}", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
        }
    }
}
