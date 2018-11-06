package com.hembitski.radial.util

import com.hembitski.radial.data.drawing.Point

fun calculatePointOfSmoothShift(x1: Float, y1: Float, x2: Float, y2: Float, distance: Float, point: Point) {
    val dx = x2 - x1
    val dy = y2 - y1
    val l = Math.sqrt((dx * dx + dy * dy).toDouble())
    var dirX = dx / l
    var dirY = dy / l
    dirX *= distance
    dirY *= distance
    point.x = (dirX + x1).toFloat()
    point.y = (dirY + y1).toFloat()
}

fun getDistanceBetweenPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}