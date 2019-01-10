package com.hembitski.radial.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.hembitski.radial.data.drawing.DrawingItem
import com.hembitski.radial.data.drawing.HistoryDrawingItem
import com.hembitski.radial.data.drawing.Line
import com.hembitski.radial.data.drawing.Point
import com.hembitski.radial.data.drawing.settings.DrawingSettings
import com.hembitski.radial.util.calculatePointOfSmoothShift
import com.hembitski.radial.util.getDistanceBetweenPoints
import java.util.*

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val SHIFT_SMOOTH_ACTION = 200f
        private const val MAX_SHIFT_DRAWING = 50f

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
            drawingItemFactory = DrawingItemPool(value, lineFactory)
        }

    var listener: Listener = DefaultListener()

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var cx = 0f
    private var cy = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var tmpSmoothX = 0f
    private var tmpSmoothY = 0f
    private val point = Point()

    private var needToSaveDrawingItem = false
    private var isDrawing = false

    private val mainThreadHandler = Handler()
    private var calculationThreadHandler: Handler? = null
    private val lineFactory: LineFactory = LinePool()
    private var drawingItemFactory: DrawingItemFactory = DrawingItemPool(settings, lineFactory)

    private var historyDrawingItem: HistoryDrawingItem? = null

    fun startCalculationThread() {
        Thread(Runnable {
            Looper.prepare()
            calculationThreadHandler = Handler()
            Looper.loop()
        }).start()
    }

    fun stopCalculationThread() {
        calculationThreadHandler?.looper?.quit()
        calculationThreadHandler = null
    }

    fun drawHistory(history: List<HistoryDrawingItem>) {
        bitmap?.let { createNewBitmap(it.width, it.height) }
        for (item in history) {
            preDrawHistory(item)
        }
        invalidate()
    }

    fun clearAll() {
        bitmap?.let { createNewBitmap(it.width, it.height) }
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
                MotionEvent.ACTION_DOWN -> calculationThreadHandler?.post { onActionDown(it) }
                MotionEvent.ACTION_MOVE -> calculationThreadHandler?.post { onActionMove(it) }
                MotionEvent.ACTION_UP -> calculationThreadHandler?.post { onActionUp() }
                else -> {
                }
            }
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        needToSaveDrawingItem = false
        isDrawing = true
        mainThreadHandler.post { listener.onStartTouching() }
        lastX = event.x
        lastY = event.y
        historyDrawingItem = HistoryDrawingItem(LinkedList())
    }

    private fun onActionMove(event: MotionEvent) {
        if (!isDrawing) return

        if (settings.smooth) {
            if (getDistanceBetweenPoints(lastX, lastY, event.x, event.y) > SHIFT_SMOOTH_ACTION) {
                val smoothDistance = getDistanceBetweenPoints(tmpSmoothX, tmpSmoothY, event.x, event.y)
                calculatePointOfSmoothShift(lastX, lastY, event.x, event.y, smoothDistance, point)

                val drawDistance = getDistanceBetweenPoints(lastX, lastY, point.x, point.y)
                if (drawDistance >= MAX_SHIFT_DRAWING) {
                    isDrawing = false
                    onActionUp()
                    mainThreadHandler.post { listener.onToFastDrawing() }
                    return
                }

                val drawingItem = drawingItemFactory.getDrawingItem()
                calculatePointsInCircle(drawingItem, lastX, lastY, point.x, point.y)
                historyDrawingItem?.items?.add(drawingItem)
                mainThreadHandler.post {
                    preDraw(drawingItem)
                    invalidate()
                }
                lastX = point.x
                lastY = point.y
                needToSaveDrawingItem = true
            }
            tmpSmoothX = event.x
            tmpSmoothY = event.y
        } else {
            val drawDistance = getDistanceBetweenPoints(lastX, lastY, event.x, event.y)
            if (drawDistance >= MAX_SHIFT_DRAWING) {
                isDrawing = false
                onActionUp()
                mainThreadHandler.post { listener.onToFastDrawing() }
                return
            }

            val drawingItem = drawingItemFactory.getDrawingItem()
            calculatePointsInCircle(drawingItem, lastX, lastY, event.x, event.y)
            historyDrawingItem?.items?.add(drawingItem)
            mainThreadHandler.post {
                preDraw(drawingItem)
                invalidate()
            }
            lastX = event.x
            lastY = event.y
            needToSaveDrawingItem = true
        }
    }

    private fun onActionUp() {
        mainThreadHandler.post {
            listener.onEndTouching()
            if (needToSaveDrawingItem) {
                historyDrawingItem?.let { listener.onNewDrawingItem(it) }
            }
        }
    }

    private fun calculatePointsInCircle(item: DrawingItem, x1: Float, y1: Float, x2: Float, y2: Float) {
        item.lines?.let {
            val angle = 360.0 / it.size
            for (i in 0 until it.size) {
                val c: Float = Math.cos(Math.toRadians(angle * i)).toFloat()
                val s: Float = Math.sin(Math.toRadians(angle * i)).toFloat()

                var rx = x1 - cx
                var ry = y1 - cy
                it[i].x1 = cx + rx * c - ry * s
                it[i].y1 = cy + rx * s + ry * c

                rx = x2 - cx
                ry = y2 - cy
                it[i].x2 = cx + rx * c - ry * s
                it[i].y2 = cy + rx * s + ry * c
            }
        }
    }

    private fun preDraw(item: DrawingItem) {
        item.lines?.let {
            for (line in it) {
                canvas?.drawLine(line.x1, line.y1, line.x2, line.y2, item.paint)
            }
        }
    }

    private fun preDrawHistory(item: HistoryDrawingItem) {
        for (drawingItem in item.items) {
            preDraw(drawingItem)
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

    interface LineFactory {
        fun getLine(): Line
    }

    interface DrawingItemFactory {
        fun getDrawingItem(): DrawingItem
    }

    interface Listener {
        fun onNewDrawingItem(item: HistoryDrawingItem)

        fun onStartTouching()

        fun onEndTouching()

        fun onToFastDrawing()
    }

    private class DefaultListener : Listener {
        override fun onNewDrawingItem(item: HistoryDrawingItem) {
            throwException()
        }

        override fun onStartTouching() {
            throwException()
        }

        override fun onEndTouching() {
            throwException()
        }

        override fun onToFastDrawing() {
            throwException()
        }

        private fun throwException() {
            throw RuntimeException("DrawingView with DefaultListener. You  need to setup other DrawingView.Listener")
        }
    }

    private class LinePool : LineFactory {
        override fun getLine() = Line()
    }

    private class DrawingItemPool(val settings: DrawingSettings, val lineFactory: LineFactory) :
            DrawingItemFactory {
        override fun getDrawingItem() = createDrawingItem()

        private fun createDrawingItem(): DrawingItem {
            val drawingItem = DrawingItem(createLineList(settings.numberOfSectors))
            drawingItem.paint.color = settings.color
            drawingItem.paint.strokeWidth = settings.brushDiameter
            drawingItem.paint.style = Paint.Style.STROKE
            drawingItem.paint.strokeCap = Paint.Cap.ROUND
            drawingItem.paint.isAntiAlias = true
            return drawingItem
        }

        private fun createLineList(size: Int): List<Line> {
            val list: MutableList<Line> = ArrayList()
            for (i in 0 until size) {
                list.add(lineFactory.getLine())
            }
            return list
        }
    }
}