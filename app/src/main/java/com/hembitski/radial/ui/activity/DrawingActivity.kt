package com.hembitski.radial.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hembitski.radial.R
import com.hembitski.radial.data.history.DrawingItem
import com.hembitski.radial.ui.presenter.DrawingActivityPresenter
import com.hembitski.radial.ui.view.DrawingView
import kotlinx.android.synthetic.main.activity_main.*

class DrawingActivity : AppCompatActivity() {

    private val presenter = DrawingActivityPresenter(DrawingActivityPresenterListener())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView.listener = DrawingViewListener()
        historyBack.setOnClickListener { presenter.onHistoryBack() }
        historyForward.setOnClickListener { presenter.onHistoryForward() }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    private inner class DrawingViewListener : DrawingView.Listener {
        override fun onNewDrawingItem(item: DrawingItem) {
            presenter.addDrawingItemToHistory(item)
        }
    }

    private inner class DrawingActivityPresenterListener : DrawingActivityPresenter.Listener {
        override fun drawHistory(history: List<DrawingItem>) {
            drawingView.drawHistory(history)
        }

        override fun enableHistoryButton(back: Boolean, forward: Boolean) {
            historyBack.isEnabled = back
            historyForward.isEnabled = forward
        }
    }
}
