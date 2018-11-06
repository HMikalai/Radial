package com.hembitski.radial.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.hembitski.radial.data.drawing.Line
import com.hembitski.radial.data.drawing.Point
import com.hembitski.radial.data.drawing.settings.DrawingSettings
import com.hembitski.radial.data.history.DrawingItem
import com.hembitski.radial.util.calculatePointOfSmoothShift
import com.hembitski.radial.util.getDistanceBetweenPoints

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val SHIFT_SMOOTH_ACTION = 200f
    }

    var settings = DrawingSettings(16, 10f, Color.BLUE, false, true)
        set(value) {
            field = value
            initPaints()
        }

    var listener: Listener = DefaultListener()

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private val line = Line()
    private val paint = Paint()
    private var path = Path()
    private var tmpSmoothX = 0f
    private var tmpSmoothY = 0f
    private val point = Point()

    private var needToSaveDrawingItem = false

    init {
        initPaints()
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
        needToSaveDrawingItem = false
        listener.onStartTouching()
        path = Path()
        line.x1 = event.x
        line.y1 = event.y
        path.moveTo(event.x, event.y)
    }

    private fun onActionMove(event: MotionEvent) {
        if (settings.smooth) {
            if (getDistanceBetweenPoints(line.x1, line.y1, event.x, event.y) > SHIFT_SMOOTH_ACTION) {
                val distance = getDistanceBetweenPoints(tmpSmoothX, tmpSmoothY, event.x, event.y)
                calculatePointOfSmoothShift(line.x1, line.y1, event.x, event.y, distance, point)
                path.lineTo(point.x, point.y)
                line.x2 = point.x
                line.y2 = point.y
                preDrawLine()
                invalidate()
                line.x1 = point.x
                line.y1 = point.y
                needToSaveDrawingItem = true
            }
            tmpSmoothX = event.x
            tmpSmoothY = event.y
        } else {
            path.lineTo(event.x, event.y)
            line.x2 = event.x
            line.y2 = event.y
            preDrawLine()
            invalidate()
            line.x1 = event.x
            line.y1 = event.y
            needToSaveDrawingItem = true
        }
    }

    private fun onActionUp() {
        listener.onEndTouching()
        if (needToSaveDrawingItem) {
            listener.onNewDrawingItem(DrawingItem(path))
        }
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

    private fun initPaints() {
        paint.color = settings.color
        paint.strokeWidth = settings.brushDiameter
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
    }

    interface Listener {
        fun onNewDrawingItem(item: DrawingItem)

        fun onStartTouching()

        fun onEndTouching()
    }

    private class DefaultListener : Listener {
        override fun onNewDrawingItem(item: DrawingItem) {
            throwException()
        }

        override fun onStartTouching() {
            throwException()
        }

        override fun onEndTouching() {
            throwException()
        }

        private fun throwException() {
            throw RuntimeException("DrawingView with DefaultListener. You  need to setup other DrawingView.Listener")
        }
    }
}