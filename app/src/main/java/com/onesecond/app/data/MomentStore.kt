package com.onesecond.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Local-only storage. Metadata lives in one small JSON index (written atomically),
 * media lives in app-private files. Nothing leaves the device.
 */
class MomentStore(private val context: Context) {

    val mediaDir: File = File(context.filesDir, "moments").also { it.mkdirs() }
    val thumbDir: File = File(context.filesDir, "thumbs").also { it.mkdirs() }
    private val indexFile = File(context.filesDir, "moments.json")

    private val _moments = MutableStateFlow(load())
    val moments: StateFlow<List<Moment>> = _moments.asStateFlow()

    fun mediaFile(name: String?): File? = name?.let { File(mediaDir, it) }
    fun thumbFile(name: String?): File? = name?.let { File(thumbDir, it) }

    /** Saves a moment, replacing any existing moment on the same day. Throws IOException on failure. */
    @Synchronized
    fun put(moment: Moment) {
        val current = _moments.value
        val replaced = current.filter { it.epochDay == moment.epochDay }
        val next = (current.filter { it.epochDay != moment.epochDay } + moment)
            .sortedByDescending { it.epochDay }
        persist(next)
        _moments.value = next
        replaced.forEach { deleteFiles(it) }
    }

    @Synchronized
    fun delete(id: String) {
        val current = _moments.value
        val target = current.firstOrNull { it.id == id } ?: return
        val next = current.filter { it.id != id }
        persist(next)
        _moments.value = next
        deleteFiles(target)
    }

    @Synchronized
    fun deleteAll() {
        persist(emptyList())
        _moments.value = emptyList()
        mediaDir.listFiles()?.forEach { it.delete() }
        thumbDir.listFiles()?.forEach { it.delete() }
    }

    /** Removes media written for a moment that was never committed to the index. */
    fun discardFiles(id: String) {
        mediaDir.listFiles()?.filter { it.name.startsWith(id) }?.forEach { it.delete() }
        thumbDir.listFiles()?.filter { it.name.startsWith(id) }?.forEach { it.delete() }
    }

    private fun deleteFiles(m: Moment) {
        mediaFile(m.mediaFile)?.delete()
        thumbFile(m.thumbFile)?.delete()
    }

    private fun persist(list: List<Moment>) {
        val arr = JSONArray()
        list.forEach { arr.put(toJson(it)) }
        val tmp = File(context.filesDir, "moments.json.tmp")
        tmp.writeText(arr.toString())
        if (!tmp.renameTo(indexFile)) {
            indexFile.delete()
            if (!tmp.renameTo(indexFile)) throw IOException("Could not write moments index")
        }
    }

    private fun load(): List<Moment> {
        if (!indexFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(indexFile.readText())
            (0 until arr.length())
                .mapNotNull { i -> runCatching { fromJson(arr.getJSONObject(i)) }.getOrNull() }
                .filter { m ->
                    val name = m.mediaFile
                    m.type == MomentType.NOTE || (name != null && File(mediaDir, name).exists())
                }
                .sortedByDescending { it.epochDay }
        } catch (e: Exception) {
            indexFile.renameTo(File(context.filesDir, "moments.corrupt.json"))
            emptyList()
        }
    }

    private fun toJson(m: Moment): JSONObject = JSONObject()
        .put("id", m.id)
        .put("day", m.epochDay)
        .put("type", m.type.name)
        .put("media", m.mediaFile ?: JSONObject.NULL)
        .put("thumb", m.thumbFile ?: JSONObject.NULL)
        .put("caption", m.caption)
        .put("mood", m.mood?.name ?: JSONObject.NULL)
        .put("created", m.createdAt)
        .put("duration", m.durationMs)

    private fun fromJson(o: JSONObject): Moment = Moment(
        id = o.getString("id"),
        epochDay = o.getLong("day"),
        type = MomentType.valueOf(o.getString("type")),
        mediaFile = if (o.isNull("media")) null else o.getString("media"),
        thumbFile = if (o.isNull("thumb")) null else o.getString("thumb"),
        caption = o.optString("caption", ""),
        mood = if (o.isNull("mood")) null else runCatching { Mood.valueOf(o.getString("mood")) }.getOrNull(),
        createdAt = o.optLong("created", 0L),
        durationMs = o.optLong("duration", 0L)
    )
}
