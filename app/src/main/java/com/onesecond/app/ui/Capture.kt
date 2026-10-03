package com.onesecond.app.ui

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onesecond.app.AppViewModel
import com.onesecond.app.Draft
import com.onesecond.app.data.MomentType
import java.io.File

private const val CAPTION_MAX = 280
private const val NOTE_MAX = 500

@Composable
fun CaptureScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val moments by vm.moments.collectAsStateWithLifecycle()
    val todayKept = moments.any { it.epochDay == vm.today.toEpochDay() }
    val state = vm.capture

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = vm.capture.pendingUri
        if (ok && uri != null) vm.loadDraft(uri, MomentType.PHOTO) else vm.capture.pendingFile?.delete()
    }
    val takeVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val uri = vm.capture.pendingUri
        if (ok && uri != null) vm.loadDraft(uri, MomentType.VIDEO) else vm.capture.pendingFile?.delete()
    }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.loadDraft(uri, null)
    }

    fun launchCamera(video: Boolean) {
        try {
            val dir = File(context.cacheDir, "capture").apply { mkdirs() }
            val file = File.createTempFile(if (video) "video_" else "photo_", if (video) ".mp4" else ".jpg", dir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            vm.capture.pendingFile = file
            vm.capture.pendingUri = uri
            vm.capture.error = null
            if (video) takeVideo.launch(uri) else takePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            vm.capture.error = "No camera app was found on this device."
        } catch (e: Exception) {
            vm.capture.error = "Couldn't open the camera. Please try again."
        }
    }

    OverlayScreen {
        OverlayTopBar("New moment", onBack = vm::pop, closeIcon = true)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val draft = state.draft
            when {
                state.loading || state.saving ->
                    CaptureSkeleton(if (state.saving) "Keeping your moment…" else "Getting it ready…")
                draft == null -> ChooseSource(
                    todayKept = todayKept,
                    error = state.error,
                    onPhoto = { launchCamera(false) },
                    onVideo = { launchCamera(true) },
                    onLibrary = {
                        try {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        } catch (e: Exception) {
                            vm.capture.error = "Couldn't open your library."
                        }
                    },
                    onNote = vm::startNote
                )
                else -> ComposeStep(vm, draft, todayKept)
            }
        }
    }
}

@Composable
private fun ChooseSource(
    todayKept: Boolean,
    error: String?,
    onPhoto: () -> Unit,
    onVideo: () -> Unit,
    onLibrary: () -> Unit,
    onNote: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Text("Capture your second", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            if (todayKept) "Saving a new moment replaces today's moment." else "One small piece of today.",
            color = TextMuted, fontSize = 14.sp
        )
        Spacer(Modifier.height(22.dp))
        SourceRow(Icons.Outlined.CameraAlt, "Take a photo", "Open the camera", onPhoto)
        SourceRow(Icons.Outlined.Videocam, "Record a video", "Up to 60 seconds", onVideo)
        SourceRow(Icons.Outlined.PhotoLibrary, "Choose from library", "A photo or a short video", onLibrary)
        SourceRow(Icons.Outlined.EditNote, "Write a note", "Just a few words", onNote)
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = Danger, fontSize = 13.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SourceRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(Tile),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = Cyan) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun ComposeStep(vm: AppViewModel, draft: Draft, todayKept: Boolean) {
    val state = vm.capture
    val isNote = draft is Draft.Note
    val limit = if (isNote) NOTE_MAX else CAPTION_MAX
    val canSave = !state.saving && (!isNote || state.caption.isNotBlank())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        when (draft) {
            is Draft.Photo -> {
                val bitmap by rememberUriBitmap(draft.uri, 1080)
                BitmapImage(bitmap, Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(22.dp)))
            }
            is Draft.Video -> {
                Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(22.dp))) {
                    BitmapImage(draft.frame, Modifier.fillMaxSize())
                    Box(
                        Modifier.align(Alignment.Center).size(56.dp).clip(CircleShape).background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White) }
                    Text(
                        formatDuration(draft.durationMs), color = Color.White, fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp)
                            .clip(RoundedCornerShape(8.dp)).background(Color(0x99000000))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            is Draft.Note -> Unit
        }
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = state.caption,
            onValueChange = { if (it.length <= limit) state.caption = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = if (isNote) 6 else 2,
            maxLines = if (isNote) 10 else 4,
            shape = RoundedCornerShape(18.dp),
            placeholder = {
                Text(if (isNote) "Write what you want to remember." else "Add a caption (optional)", color = TextMuted)
            },
            supportingText = { Text("${state.caption.length}/$limit", color = TextMuted, fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                cursorColor = Cyan,
                focusedBorderColor = Cyan,
                unfocusedBorderColor = Line
            )
        )
        Spacer(Modifier.height(14.dp))
        SectionLabel("How does today feel? (optional)")
        Spacer(Modifier.height(10.dp))
        MoodSelector(state.mood) { state.mood = it }
        if (state.error != null) {
            Text(state.error ?: "", color = Danger, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
        }
        if (todayKept) {
            Text("This will replace today's moment.", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
        }
        PrimaryButton(text = "Keep this moment", onClick = vm::saveMoment, enabled = canSave)
        Spacer(Modifier.height(10.dp))
        SecondaryButton(text = "Choose something else", onClick = vm::resetDraft)
        Spacer(Modifier.height(24.dp))
    }
}
