package com.hembitski.radial.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.hembitski.radial.data.drawing.DrawingItem
import com.hembitski.radial.data.drawing.Line

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var listener: Listener = DefaultListener()

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private val line = Line()
    private val paint = Paint()
    private var path = Path()

    init {
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    fun drawHistory(history: List<DrawingItem>) {
        bitmap?.let { createNewBitmap(it.width, it.height) }
        for (item in history) {
            preDrawDrawingItem(item)
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createNewBitmap(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.takeIf { bitmap != null }?.drawBitmap(bitmap, 0f, 0f, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(it)
                MotionEvent.ACTION_MOVE -> onActionMove(it)
                MotionEvent.ACTION_UP -> onActionUp()
                else -> {
                }
            }
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        path = Path()
        line.x1 = event.x
        line.y1 = event.y
        path.moveTo(event.x, event.y)
    }

    private fun onActionMove(event: MotionEvent) {
        path.lineTo(event.x, event.y)
        line.x2 = event.x
        line.y2 = event.y
        preDrawLine()
        invalidate()
        line.x1 = event.x
        line.y1 = event.y
    }

    private fun onActionUp() {
        listener.onNewDrawingItem(DrawingItem(path))
    }

    private fun preDrawLine() {
        canvas?.drawLine(line.x1, line.y1, line.x2, line.y2, paint)
    }

    private fun preDrawDrawingItem(item: DrawingItem) {
        canvas?.drawPath(item.path, paint)
    }

    private fun createNewBitmap(width: Int, height: Int) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    interface Listener {
        fun onNewDrawingItem(item: DrawingItem)
    }

    private class DefaultListener : Listener {
        override fun onNewDrawingItem(item: DrawingItem) {
            throwException()
        }

        private fun throwException() {
            throw RuntimeException("DrawingView with DefaultListener. You  need to setup other DrawingView.Listener")
        }
    }
}