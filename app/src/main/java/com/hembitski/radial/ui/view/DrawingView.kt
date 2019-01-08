package com.hembitski.radial.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.hembitski.radial.data.drawing.Point
import com.hembitski.radial.data.drawing.settings.DrawingSettings
import com.hembitski.radial.data.history.DrawingItem
import com.hembitski.radial.util.calculatePointOfSmoothShift
import com.hembitski.radial.util.getDistanceBetweenPoints

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val SHIFT_SMOOTH_ACTION = 200f

        private const val DEFAULT_NUMBER_OF_SECTORS = 7
        private const val DEFAULT_BRUSH_DIAMETER = 3f
        private const val DEFAULT_COLOR = Color.BLUE
        private const val DEFAULT_SMOOTH = true
        private const val DEFAULT_MIRROR_DRAWING = true
    }

    var settings = DrawingSettings(
        DEFAULT_NUMBER_OF_SECTORS,
        DEFAULT_BRUSH_DIAMETER,
        DEFAULT_COLOR,
        DEFAULT_SMOOTH,
        DEFAULT_MIRROR_DRAWING
    )
        set(value) {
            field = value
            createAndSetupDrawingItem()
        }

    var listener: Listener = DefaultListener()

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var cx = 0f
    private var cy = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var drawingItem = DrawingItem(createPathsList(DEFAULT_NUMBER_OF_SECTORS))
    private var tmpSmoothX = 0f
    private var tmpSmoothY = 0f
    private val point = Point()

    private var needToSaveDrawingItem = false

    private var angleInDegree: Double = 0.0

    init {
        createAndSetupDrawingItem()
    }

    fun drawHistory(history: List<DrawingItem>) {
        bitmap?.let { createNewBitmap(it.width, it.height) }
        for (item in history) {
            preDraw(item)
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        createNewBitmap(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.takeIf { bitmap != null }?.drawBitmap(bitmap!!, 0f, 0f, null)
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
        lastX = event.x
        lastY = event.y
        setPathsMoveTo(drawingItem, event.x, event.y)
    }

    private fun onActionMove(event: MotionEvent) {
        if (settings.smooth) {
            if (getDistanceBetweenPoints(lastX, lastY, event.x, event.y) > SHIFT_SMOOTH_ACTION) {
                val distance = getDistanceBetweenPoints(tmpSmoothX, tmpSmoothY, event.x, event.y)
                calculatePointOfSmoothShift(lastX, lastY, event.x, event.y, distance, point)
                setPathsLineTo(drawingItem, point.x, point.y)
                preDraw(drawingItem)
                invalidate()
                lastX = point.x
                lastY = point.y
                needToSaveDrawingItem = true
            }
            tmpSmoothX = event.x
            tmpSmoothY = event.y
        } else {
            setPathsLineTo(drawingItem, event.x, event.y)
            preDraw(drawingItem)
            invalidate()
            lastX = event.x
            lastY = event.y
            needToSaveDrawingItem = true
        }
    }

    private fun onActionUp() {
        listener.onEndTouching()
        if (needToSaveDrawingItem) {
            listener.onNewDrawingItem(drawingItem)
            createAndSetupDrawingItem()
        }
    }

    private fun setPathsMoveTo(drawingItem: DrawingItem, x: Float, y: Float) {
        drawingItem.paths[0].moveTo(x, y)
        for (i in 1 until drawingItem.paths.size) {
            calculateNextPoint(angleInDegree * i, x, y, drawingItem.paths[i], true)
        }
    }

    private fun setPathsLineTo(drawingItem: DrawingItem, x: Float, y: Float) {
        drawingItem.paths[0].lineTo(x, y)
        for (i in 1 until drawingItem.paths.size) {
            calculateNextPoint(angleInDegree * i, x, y, drawingItem.paths[i], false)
        }
    }

    private fun calculateNextPoint(
        angleInDegree: Double,
        srcX: Float,
        srcY: Float,
        dstPath: Path,
        moveTo: Boolean
    ) {
        val rx: Float = srcX - cx
        val ry: Float = srcY - cy
        val c: Float = Math.cos(Math.toRadians(angleInDegree)).toFloat()
        val s: Float = Math.sin(Math.toRadians(angleInDegree)).toFloat()
        val x = cx + rx * c - ry * s
        val y = cy + rx * s + ry * c
        if (moveTo) {
            dstPath.moveTo(x, y)
        } else {
            dstPath.lineTo(x, y)
        }
    }

    private fun preDraw(item: DrawingItem) {
        for (p in item.paths) {
            canvas?.drawPath(p, item.paint)
        }
    }

    private fun createNewBitmap(width: Int, height: Int) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        drawMarkingLines()
    }

    private fun drawMarkingLines() {
        val paint = Paint()
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas?.let {
            it.drawLine(it.width / 2f, 0f, it.width / 2f, it.height.toFloat(), paint)
            it.drawLine(0f, it.height / 2f, it.width.toFloat(), it.height / 2f, paint)
        }
    }

    private fun createAndSetupDrawingItem() {
        drawingItem = DrawingItem(createPathsList(settings.numberOfSectors))
        drawingItem.paint.color = settings.color
        drawingItem.paint.strokeWidth = settings.brushDiameter
        drawingItem.paint.style = Paint.Style.STROKE
        drawingItem.paint.strokeCap = Paint.Cap.ROUND
        drawingItem.paint.isAntiAlias = true
    }

    private fun createPathsList(size: Int): List<Path> {
        val list: MutableList<Path> = ArrayList()
        for (i in 1..size) {
            list.add(Path())
        }
        angleInDegree = 360.0 / list.size
        return list
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