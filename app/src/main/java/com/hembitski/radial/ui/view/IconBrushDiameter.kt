package com.hembitski.radial.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class IconBrushDiameter(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var cx = 0f
    private var cy = 0f
    private val paint = Paint()
    private var diameter = 10f

    init {
        paint.color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
    }

    fun setDiameter(diameter: Float) {
        this.diameter = if (diameter < 1) 1f else diameter
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(cx, cy, diameter / 2, paint)
    }
}