package com.onesecond.app.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.onesecond.app.data.MomentStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

/** An error whose message is safe to show to the person using the app. */
class UserFacingException(message: String) : Exception(message)

class SavedMedia(val mediaFile: String, val thumbFile: String, val durationMs: Long)
class VideoInfo(val durationMs: Long, val frame: Bitmap?)

object MediaProcessor {
    const val MAX_PHOTO_EDGE = 2048
    const val PHOTO_THUMB_EDGE = 720
    const val VIDEO_POSTER_EDGE = 1280
    const val MAX_VIDEO_MS = 60_000L

    fun decodeUri(context: Context, uri: Uri, maxEdge: Int): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxEdge)
        }
        val raw = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return null
        val degrees = try {
            resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees } ?: 0
        } catch (e: Exception) {
            0
        }
        return fit(rotate(raw, degrees), maxEdge)
    }

    fun decodeFile(path: String, maxEdge: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxEdge)
        }
        val raw = BitmapFactory.decodeFile(path, opts) ?: return null
        return fit(raw, maxEdge)
    }

    fun videoInfo(context: Context, uri: Uri): VideoInfo? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val atUs = (minOf(duration / 4, 1000L)) * 1000L
            val frame = retriever.getFrameAtTime(atUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(-1L)
            VideoInfo(duration, frame?.let { fit(it, VIDEO_POSTER_EDGE) })
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    fun savePhoto(context: Context, store: MomentStore, uri: Uri, id: String): SavedMedia {
        val bmp = decodeUri(context, uri, MAX_PHOTO_EDGE)
            ?: throw UserFacingException("That photo couldn't be read.")
        val name = "$id.jpg"
        writeJpeg(bmp, File(store.mediaDir, name), 90)
        writeJpeg(fit(bmp, PHOTO_THUMB_EDGE), File(store.thumbDir, name), 82)
        return SavedMedia(name, name, 0L)
    }

    fun saveVideo(context: Context, store: MomentStore, uri: Uri, id: String): SavedMedia {
        val info = videoInfo(context, uri)
            ?: throw UserFacingException("That video couldn't be read.")
        if (info.durationMs > MAX_VIDEO_MS + 500) {
            throw UserFacingException("Videos can be up to 60 seconds long.")
        }
        val frame = info.frame ?: throw UserFacingException("That video couldn't be read.")
        val mediaName = "$id.mp4"
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open video")
        input.use { src ->
            FileOutputStream(File(store.mediaDir, mediaName)).use { out -> src.copyTo(out) }
        }
        val thumbName = "$id.jpg"
        writeJpeg(frame, File(store.thumbDir, thumbName), 85)
        return SavedMedia(mediaName, thumbName, info.durationMs)
    }

    private fun writeJpeg(bmp: Bitmap, file: File, quality: Int) {
        FileOutputStream(file).use { out ->
            if (!bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                throw IOException("Could not encode image")
            }
        }
    }

    private fun sampleSize(w: Int, h: Int, maxEdge: Int): Int {
        var s = 1
        while (max(w, h) / (s * 2) >= maxEdge) s *= 2
        return s
    }

    private fun rotate(bmp: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bmp
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        val out = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        if (out !== bmp) bmp.recycle()
        return out
    }

    /** Scales down so the longest edge is at most [maxEdge]; never scales up. */
    fun fit(bmp: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(bmp.width, bmp.height)
        if (longest <= maxEdge) return bmp
        val ratio = maxEdge.toFloat() / longest
        val w = (bmp.width * ratio).toInt().coerceAtLeast(1)
        val h = (bmp.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bmp, w, h, true)
    }
}
