package com.example.imageslice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Draws a bitmap (fit-center) with adjustable horizontal/vertical grid lines.
 * Tapping a cell selects it and highlights it.
 */
class GridImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var rows = 3
        private set
    var cols = 3
        private set
    /** Selected cell, or -1 when none. */
    var selectedRow = -1
        private set
    var selectedCol = -1
        private set

    val hasSelection get() = selectedRow >= 0 && selectedCol >= 0

    var onSelectionChanged: ((Boolean) -> Unit)? = null

    private var bitmap: Bitmap? = null
    private val imageRect = RectF()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
        setShadowLayer(3f, 0f, 0f, Color.BLACK)
    }
    private val fillPaint = Paint().apply {
        color = Color.argb(90, 33, 150, 243)
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(33, 150, 243)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // needed for shadow on lines
    }

    fun setImage(bmp: Bitmap?) {
        bitmap = bmp
        clearSelection()
        computeImageRect()
        invalidate()
    }

    fun setGrid(rows: Int, cols: Int) {
        this.rows = rows
        this.cols = cols
        clearSelection()
        invalidate()
    }

    private fun clearSelection() {
        val had = hasSelection
        selectedRow = -1
        selectedCol = -1
        if (had) onSelectionChanged?.invoke(false)
    }

    private fun computeImageRect() {
        val bmp = bitmap ?: return
        if (width == 0 || height == 0) return
        val scale = minOf(width.toFloat() / bmp.width, height.toFloat() / bmp.height)
        val w = bmp.width * scale
        val h = bmp.height * scale
        val left = (width - w) / 2f
        val top = (height - h) / 2f
        imageRect.set(left, top, left + w, top + h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeImageRect()
    }

    override fun onDraw(canvas: Canvas) {
        val bmp = bitmap ?: return
        canvas.drawBitmap(bmp, null, imageRect, null)

        val cellW = imageRect.width() / cols
        val cellH = imageRect.height() / rows

        if (hasSelection) {
            val l = imageRect.left + selectedCol * cellW
            val t = imageRect.top + selectedRow * cellH
            canvas.drawRect(l, t, l + cellW, t + cellH, fillPaint)
        }

        // Outer frame + inner lines
        canvas.drawRect(imageRect, gridPaint)
        for (c in 1 until cols) {
            val x = imageRect.left + c * cellW
            canvas.drawLine(x, imageRect.top, x, imageRect.bottom, gridPaint)
        }
        for (r in 1 until rows) {
            val y = imageRect.top + r * cellH
            canvas.drawLine(imageRect.left, y, imageRect.right, y, gridPaint)
        }

        if (hasSelection) {
            val l = imageRect.left + selectedCol * cellW
            val t = imageRect.top + selectedRow * cellH
            canvas.drawRect(l, t, l + cellW, t + cellH, borderPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (bitmap == null) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val x = event.x
                val y = event.y
                if (imageRect.contains(x, y)) {
                    selectedCol = ((x - imageRect.left) / imageRect.width() * cols)
                        .toInt().coerceIn(0, cols - 1)
                    selectedRow = ((y - imageRect.top) / imageRect.height() * rows)
                        .toInt().coerceIn(0, rows - 1)
                    onSelectionChanged?.invoke(true)
                    invalidate()
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()
}
