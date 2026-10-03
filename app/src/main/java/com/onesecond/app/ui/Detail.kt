package com.onesecond.app.ui

import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.data.Graph
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType

@Composable
fun DetailScreen(vm: AppViewModel, id: String) {
    val moments by vm.moments.collectAsStateWithLifecycle()
    val moment = moments.firstOrNull { it.id == id }
    if (moment == null) {
        // Deleted (or never existed): leave.
        LaunchedEffect(Unit) { vm.pop() }
        return
    }
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    val isToday = moment.epochDay == vm.today.toEpochDay()

    OverlayScreen {
        OverlayTopBar(moment.date.shortLabel(), onBack = vm::pop)
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp))) {
            when (moment.type) {
                MomentType.NOTE -> NoteSurface(moment.caption, Modifier.fillMaxSize(), 24)
                MomentType.PHOTO -> {
                    val bitmap by rememberMomentBitmap(moment, 1600)
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        BitmapImage(bitmap, Modifier.fillMaxSize(), ContentScale.Fit)
                    }
                }
                MomentType.VIDEO -> VideoPlayer(moment)
            }
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
            Text(moment.date.longLabel(), color = TextMuted, fontSize = 12.sp)
            val mood = moment.mood
            if (mood != null) {
                Spacer(Modifier.height(4.dp))
                Text(mood.label, color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            if (moment.caption.isNotBlank() && moment.type != MomentType.NOTE) {
                Spacer(Modifier.height(8.dp))
                Text(moment.caption, fontSize = 16.sp, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "Share", icon = Icons.Outlined.Share, modifier = Modifier.weight(1f),
                    onClick = { shareMoment(context, moment) }
                )
                SecondaryButton(
                    text = "Delete", icon = Icons.Outlined.Delete, modifier = Modifier.weight(1f),
                    onClick = { confirmDelete = true }
                )
            }
            if (isToday) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Today's moment can still be replaced from the Today tab.",
                    color = TextMuted, fontSize = 11.sp
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = DialogColor,
            titleContentColor = TextMain,
            textContentColor = TextMuted,
            title = { Text("Delete this moment?") },
            text = { Text("It will be removed from this phone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteMoment(moment.id)
                }) { Text("Delete", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = TextMain) }
            }
        )
    }
}

private fun shareMoment(context: android.content.Context, m: Moment) {
    try {
        when (m.type) {
            MomentType.NOTE -> shareText(context, m.caption)
            MomentType.PHOTO -> Graph.store.mediaFile(m.mediaFile)?.let { shareFile(context, it, "image/jpeg") }
            MomentType.VIDEO -> Graph.store.mediaFile(m.mediaFile)?.let { shareFile(context, it, "video/mp4") }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open sharing.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun VideoPlayer(m: Moment) {
    val file = Graph.store.mediaFile(m.mediaFile)
    var playing by remember { mutableStateOf(true) }
    var ready by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (file != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(file.absolutePath)
                        setOnPreparedListener { it.isLooping = false; ready = true; start() }
                        setOnCompletionListener { playing = false }
                        setOnClickListener {
                            if (isPlaying) {
                                pause()
                                playing = false
                            } else {
                                if (currentPosition >= duration - 50) seekTo(0)
                                start()
                                playing = true
                            }
                        }
                    }
                },
                onRelease = { it.stopPlayback() }
            )
        }
        if (file != null && !ready) SkeletonBox(Modifier.fillMaxSize())
        if (ready && !playing) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(34.dp)) }
        }
    }
}
