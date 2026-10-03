package com.onesecond.app

import android.app.Application
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onesecond.app.data.Graph
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import com.onesecond.app.data.Mood
import com.onesecond.app.media.MediaProcessor
import com.onesecond.app.media.UserFacingException
import com.onesecond.app.notify.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

sealed interface Overlay {
    data object Capture : Overlay
    data class Detail(val id: String) : Overlay
    data class Month(val year: Int, val month: Int) : Overlay
    data class Story(val year: Int, val month: Int) : Overlay
    data object Privacy : Overlay
    data object About : Overlay
}

sealed interface Draft {
    data class Photo(val uri: Uri) : Draft
    data class Video(val uri: Uri, val durationMs: Long, val frame: Bitmap?) : Draft
    data object Note : Draft
}

class CaptureState {
    var draft by mutableStateOf<Draft?>(null)
    var caption by mutableStateOf("")
    var mood by mutableStateOf<Mood?>(null)
    var loading by mutableStateOf(false)
    var saving by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var pendingFile: File? = null
    var pendingUri: Uri? = null
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = Graph.store
    private val settings = Graph.settings

    val moments: StateFlow<List<Moment>> = store.moments

    var onboarded by mutableStateOf(settings.onboarded)
        private set
    var today by mutableStateOf(LocalDate.now())
        private set
    var tab by mutableIntStateOf(0)
    val stack = mutableStateListOf<Overlay>()
    var capture by mutableStateOf(CaptureState())
        private set

    var darkTheme by mutableStateOf(settings.darkMode ?: systemIsDark())
        private set

    var reminderEnabled by mutableStateOf(settings.reminderEnabled)
        private set
    var reminderHour by mutableIntStateOf(settings.reminderHour)
        private set
    var reminderMinute by mutableIntStateOf(settings.reminderMinute)
        private set

    // ----- navigation -----
    fun push(o: Overlay) { stack.add(o) }
    fun pop() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }

    private fun systemIsDark(): Boolean {
        val mode = getApplication<Application>().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun chooseDarkTheme(dark: Boolean) {
        settings.darkMode = dark
        darkTheme = dark
    }

    fun completeOnboarding() {
        settings.onboarded = true
        onboarded = true
    }

    fun refreshToday() {
        val now = LocalDate.now()
        if (now != today) today = now
    }

    // ----- capture -----
    fun openCapture() {
        cleanCaptureCache()
        capture = CaptureState()
        push(Overlay.Capture)
    }

    fun startNote() {
        capture.draft = Draft.Note
        capture.error = null
    }

    fun resetDraft() {
        capture.draft = null
        capture.caption = ""
        capture.error = null
    }

    fun loadDraft(uri: Uri, forced: MomentType?) {
        val c = capture
        c.loading = true
        c.error = null
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val isVideo = when (forced) {
                        MomentType.VIDEO -> true
                        MomentType.PHOTO -> false
                        else -> app.contentResolver.getType(uri)?.startsWith("video") == true
                    }
                    if (isVideo) {
                        val info = MediaProcessor.videoInfo(app, uri)
                            ?: throw UserFacingException("That video couldn't be read.")
                        if (info.durationMs > MediaProcessor.MAX_VIDEO_MS + 500) {
                            throw UserFacingException("Videos can be up to 60 seconds long.")
                        }
                        Draft.Video(uri, info.durationMs, info.frame) as Draft
                    } else {
                        val probe = MediaProcessor.decodeUri(app, uri, 256)
                            ?: throw UserFacingException("That photo couldn't be read.")
                        probe.recycle()
                        Draft.Photo(uri) as Draft
                    }
                }
            }
            c.loading = false
            result
                .onSuccess { c.draft = it }
                .onFailure {
                    c.error = (it as? UserFacingException)?.message
                        ?: "Something went wrong reading that. Please try again."
                }
        }
    }

    fun saveMoment() {
        val c = capture
        val draft = c.draft ?: return
        if (c.saving) return
        val caption = c.caption.trim()
        if (draft is Draft.Note && caption.isEmpty()) return
        c.saving = true
        c.error = null
        val id = UUID.randomUUID().toString()
        val day = LocalDate.now().toEpochDay()
        val mood = c.mood
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                try {
                    val saved = when (draft) {
                        is Draft.Photo -> MediaProcessor.savePhoto(app, store, draft.uri, id)
                        is Draft.Video -> MediaProcessor.saveVideo(app, store, draft.uri, id)
                        is Draft.Note -> null
                    }
                    val type = when (draft) {
                        is Draft.Photo -> MomentType.PHOTO
                        is Draft.Video -> MomentType.VIDEO
                        is Draft.Note -> MomentType.NOTE
                    }
                    store.put(
                        Moment(
                            id = id,
                            epochDay = day,
                            type = type,
                            mediaFile = saved?.mediaFile,
                            thumbFile = saved?.thumbFile,
                            caption = caption,
                            mood = mood,
                            createdAt = System.currentTimeMillis(),
                            durationMs = saved?.durationMs ?: 0L
                        )
                    )
                    null
                } catch (e: Exception) {
                    store.discardFiles(id)
                    (e as? UserFacingException)?.message
                        ?: "Couldn't save your moment. Please try again."
                }
            }
            c.saving = false
            if (result == null) {
                cleanCaptureCache()
                stack.clear()
                tab = 0
                today = LocalDate.now()
            } else {
                c.error = result
            }
        }
    }

    private fun cleanCaptureCache() {
        val dir = File(getApplication<Application>().cacheDir, "capture")
        viewModelScope.launch(Dispatchers.IO) { dir.deleteRecursively() }
    }

    // ----- data -----
    fun deleteMoment(id: String) {
        viewModelScope.launch(Dispatchers.IO) { store.delete(id) }
        pop()
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) { store.deleteAll() }
    }

    // ----- reminders -----
    fun changeReminderEnabled(enabled: Boolean) {
        val app = getApplication<Application>()
        settings.reminderEnabled = enabled
        reminderEnabled = enabled
        if (enabled) Reminder.schedule(app, reminderHour, reminderMinute) else Reminder.cancel(app)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        settings.reminderHour = hour
        settings.reminderMinute = minute
        reminderHour = hour
        reminderMinute = minute
        if (reminderEnabled) Reminder.schedule(getApplication<Application>(), hour, minute)
    }
}
