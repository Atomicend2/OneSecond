package com.onesecond.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.onesecond.app.data.Graph
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import com.onesecond.app.media.MediaProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Dates ----------

private fun fmt(pattern: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

fun LocalDate.kickerLabel(): String = format(fmt("EEEE · MMMM d"))
fun LocalDate.longLabel(): String = format(fmt("EEEE, MMMM d, yyyy"))
fun LocalDate.shortLabel(): String = format(fmt("EEE, MMM d"))
fun YearMonth.titleLabel(): String = format(fmt("MMMM yyyy"))

fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

fun countLabel(n: Int): String = if (n == 1) "1 moment" else "$n moments"

/** Consecutive days with a kept moment, ending today (or yesterday if today isn't kept yet). */
fun computeStreak(moments: List<Moment>, today: LocalDate): Int {
    val days = moments.map { it.epochDay }.toHashSet()
    var d = today.toEpochDay()
    if (d !in days) d -= 1
    var n = 0
    while (d in days) {
        n++
        d--
    }
    return n
}

// ---------- Bitmaps ----------

private object BitmapCache {
    private val lru = object : LruCache<String, Bitmap>(
        ((Runtime.getRuntime().maxMemory() / 1024) / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun get(key: String): Bitmap? = lru.get(key)
    fun put(key: String, value: Bitmap) {
        lru.put(key, value)
    }
}

@Composable
fun rememberFileBitmap(file: File?, maxEdge: Int): State<Bitmap?> {
    val key = file?.let { "${it.absolutePath}@$maxEdge" }
    return produceState<Bitmap?>(key?.let { BitmapCache.get(it) }, key) {
        if (key == null || file == null) {
            value = null
            return@produceState
        }
        val cached = BitmapCache.get(key)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val bmp = withContext(Dispatchers.IO) {
            try { MediaProcessor.decodeFile(file.absolutePath, maxEdge) } catch (e: Exception) { null }
        }
        if (bmp != null) BitmapCache.put(key, bmp)
        value = bmp
    }
}

@Composable
fun rememberMomentBitmap(m: Moment, maxEdge: Int): State<Bitmap?> {
    val store = Graph.store
    val file = when {
        m.type == MomentType.NOTE -> null
        m.type == MomentType.PHOTO && maxEdge > MediaProcessor.PHOTO_THUMB_EDGE -> store.mediaFile(m.mediaFile)
        else -> store.thumbFile(m.thumbFile)
    }
    return rememberFileBitmap(file, maxEdge)
}

@Composable
fun rememberUriBitmap(uri: Uri, maxEdge: Int): State<Bitmap?> {
    val context: Context = LocalContext.current
    return produceState<Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            try { MediaProcessor.decodeUri(context, uri, maxEdge) } catch (e: Exception) { null }
        }
    }
}

// ---------- Sharing ----------

fun shareFile(context: Context, file: File, mime: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, null))
}

fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null))
}

// ---------- Daily prompts ----------

object Prompts {
    private val list = listOf(
        "What is one little thing\nworth remembering today?",
        "What made you pause\nfor a second today?",
        "What does today look like\nfrom where you are?",
        "Who or what are you glad\nto have around today?",
        "What is the lightest moment\nof your day so far?",
        "What would you want to\nremember about this week?",
        "What did your hands do\ntoday?",
        "What is the view from\nyour window right now?",
        "What small thing went\nright today?",
        "What are you looking\nforward to?",
        "What sound belongs to\ntoday?",
        "What did you eat that\nwas worth noticing?",
        "Where did you walk\ntoday?",
        "What felt ordinary today\nthat you'll miss one day?",
        "What made you smile,\neven briefly?",
        "What is on your desk\nor table right now?",
        "What is the light like\nwhere you are?",
        "Whose company did you\nenjoy today?",
        "What are you carrying\nwith you today?",
        "What is one thing you\nfinished today?",
        "What surprised you\ntoday?",
        "What is quiet about\ntoday?",
        "What would today's\ncover photo be?",
        "What did you learn\nor notice today?",
        "What is the best part\nof right now?"
    )

    fun forDay(epochDay: Long): String = list[(epochDay % list.size).toInt()]
}
