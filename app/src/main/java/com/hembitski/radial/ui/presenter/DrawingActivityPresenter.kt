package com.hembitski.radial.ui.presenter

import com.hembitski.radial.data.drawing.DrawingItem

class DrawingActivityPresenter(private val listener: Listener) {

    private var history: MutableList<DrawingItem> = ArrayList()
    private var numberOfItemsShown = history.size

    fun onResume() {
        checkAvailabilityOfHistoryButtons()
    }

    fun onHistoryBack() {
        if (!history.isEmpty() && numberOfItemsShown - 1 >= 0) {
            numberOfItemsShown--
            val list = history.subList(0, numberOfItemsShown)
            listener.drawHistory(list)
        }
        checkAvailabilityOfHistoryButtons()
    }

    fun onHistoryForward() {
        if (numberOfItemsShown + 1 <= history.size) {
            numberOfItemsShown++
            val list = history.subList(0, numberOfItemsShown)
            listener.drawHistory(list)
        }
        checkAvailabilityOfHistoryButtons()
    }

    fun addDrawingItemToHistory(item: DrawingItem) {
        if (numberOfItemsShown != history.size) {
            history = history.subList(0, numberOfItemsShown)
        }
        history.add(item)
        numberOfItemsShown++
        checkAvailabilityOfHistoryButtons()
    }

    private fun checkAvailabilityOfHistoryButtons() {
        val back = numberOfItemsShown - 1 >= 0
        val forward = numberOfItemsShown + 1 <= history.size
        listener.enableHistoryButton(back, forward)
    }

    interface Listener {
        fun drawHistory(history: List<DrawingItem>)

        fun enableHistoryButton(back: Boolean, forward: Boolean)
    }
}