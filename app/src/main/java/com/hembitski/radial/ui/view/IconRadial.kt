package com.hembitski.radial.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.hembitski.radial.data.drawing.Line


class IconRadial(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        private const val DEGREE_IN_CIRCLE = 360.0
        private const val STROKE_WIDTH = 10f
        private const val STROKE_WIDTH_FOR_BIG_VALUE = 3f
        private const val TEXT_SIZE_COEFF = 0.55f
    }

    var defValue = 7
        set(value) {
            field = value
            setNumberOfSectors(value)
        }

    private var cx = 0
    private var cy = 0
    private var radius = 0
    private val paint = Paint()
    private val lines: MutableList<Line> = ArrayList()

    private var text = ""
    private var textX = 0f
    private var textY = 0f
    private val textPaint = Paint()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        init(w, h)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        createLinesList(defValue)
        calculateCoordinatesOfText(defValue)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            //            it.drawColor(Color.WHITE)
//            it.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), paint)
            for (line in lines) {
                it.drawLine(line.x1, line.y1, line.x2, line.y2, paint)
            }
//            it.drawText(text, textX, textY, textPaint)
        }
    }

    fun setNumberOfSectors(number: Int) {
        val width = (100 - number) / 10f
        paint.strokeWidth = if(width > 1) width else 1f
        createLinesList(number)
        calculateCoordinatesOfText(number)
        invalidate()
    }

    private fun init(w: Int, h: Int) {
        val size = Math.min(w, h)
        cx = w / 2
        cy = h / 2
        radius = (size / 2 - STROKE_WIDTH).toInt()

        paint.color = Color.WHITE
        paint.strokeWidth = STROKE_WIDTH
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true

        textPaint.color = Color.BLUE
        textPaint.textSize = size * TEXT_SIZE_COEFF
    }

    private fun createLinesList(number: Int) {
        if (number > 0) {
            lines.clear()
            val angleBetweenLines = DEGREE_IN_CIRCLE / number
            for (i in 0 until number) {
                lines.add(getLine(cx.toFloat(), cy.toFloat(), radius.toFloat(), angleBetweenLines * i))
            }
        }
    }

    private fun getLine(cx: Float, cy: Float, circleRadius: Float, angle: Double): Line {
        val line = Line()
        line.x1 = cx
        line.y1 = cy
        val rx = 0
        val ry = circleRadius
        val c1 = Math.cos(Math.toRadians(angle))
        val s1 = Math.sin(Math.toRadians(angle))
        line.x2 = (cx + rx * c1 - ry * s1).toFloat()
        line.y2 = (cy + rx * s1 + ry * c1).toFloat()
        return line
    }

    private fun calculateCoordinatesOfText(number: Int) {
        if (number > 0) {
            text = "" + number
            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            textX = cx - textBounds.width().toFloat() / 2
            textY = cy + textBounds.height().toFloat() / 2
        }
    }
}