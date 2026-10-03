package com.onesecond.app.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.onesecond.app.data.Graph
import com.onesecond.app.data.Moment
import com.onesecond.app.data.MomentType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/** Renders a shareable "month card" image from a month's moments. */
object Collage {
    private const val W = 1080
    private const val H = 1350
    private const val MARGIN = 60f
    private const val GAP = 14f
    private const val BG = 0xFF090B10.toInt()
    private const val PANEL = 0xFF171B24.toInt()
    private const val TEXT = 0xFFF2F2F0.toInt()
    private const val MUTED = 0xFF8E94A3.toInt()
    private const val ACCENT = 0xFF72E7FF.toInt()

    fun render(context: Context, moments: List<Moment>, title: String, fileKey: String): File {
        val store = Graph.store
        val sorted = moments.sortedBy { it.epochDay }
        val picks = sample(sorted, 12)

        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        try {
            val c = Canvas(bmp)
            c.drawColor(BG)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            paint.color = ACCENT
            paint.textSize = 28f
            paint.letterSpacing = 0.18f
            c.drawText("ONE SECOND", MARGIN, 112f, paint)

            paint.color = TEXT
            paint.letterSpacing = 0f
            paint.textSize = 84f
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            c.drawText(title, MARGIN, 214f, paint)

            paint.color = MUTED
            paint.textSize = 32f
            paint.typeface = Typeface.DEFAULT
            val count = sorted.size
            c.drawText(if (count == 1) "1 moment" else "$count moments", MARGIN, 268f, paint)

            val n = picks.size
            val cols = when {
                n <= 1 -> 1
                n <= 4 -> 2
                n <= 9 -> 3
                else -> 4
            }
            val cell = (W - 2 * MARGIN - GAP * (cols - 1)) / cols
            val top = 340f
            for (i in 0 until n) {
                val r = i / cols
                val col = i % cols
                val x = MARGIN + col * (cell + GAP)
                val y = top + r * (cell + GAP)
                drawCell(c, picks[i], RectF(x, y, x + cell, y + cell), store.thumbFile(picks[i].thumbFile))
            }

            paint.color = MUTED
            paint.textSize = 30f
            c.drawText("Keep a little piece of today.", MARGIN, H - 64f, paint)

            val dir = File(context.cacheDir, "share").also { it.mkdirs() }
            val file = File(dir, "onesecond-$fileKey.jpg")
            FileOutputStream(file).use { out ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)) throw IOException("encode failed")
            }
            return file
        } finally {
            bmp.recycle()
        }
    }

    private fun drawCell(c: Canvas, m: Moment, rect: RectF, thumb: File?) {
        val path = Path().apply { addRoundRect(rect, 28f, 28f, Path.Direction.CW) }
        c.save()
        c.clipPath(path)
        val src = if (m.type != MomentType.NOTE && thumb != null) {
            MediaProcessor.decodeFile(thumb.absolutePath, MediaProcessor.PHOTO_THUMB_EDGE)
        } else null
        if (src != null) {
            val s = min(src.width, src.height)
            val sx = (src.width - s) / 2
            val sy = (src.height - s) / 2
            c.drawBitmap(src, Rect(sx, sy, sx + s, sy + s), rect, Paint(Paint.FILTER_BITMAP_FLAG))
            src.recycle()
        } else {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                PANEL, 0xFF211C2B.toInt(), Shader.TileMode.CLAMP
            )
            c.drawRect(rect, p)
            val text = m.caption.ifBlank { "A quiet day" }
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = TEXT
                textSize = rect.width() * 0.085f
            }
            val pad = rect.width() * 0.09f
            val width = (rect.width() - 2 * pad).toInt().coerceAtLeast(1)
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(6)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
            c.save()
            c.translate(rect.left + pad, rect.top + pad)
            layout.draw(c)
            c.restore()
        }
        c.restore()
    }

    private fun sample(list: List<Moment>, max: Int): List<Moment> {
        if (list.size <= max) return list
        return (0 until max).map { list[it * list.size / max] }
    }
}
