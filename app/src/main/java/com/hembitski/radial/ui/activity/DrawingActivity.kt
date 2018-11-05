package com.hembitski.radial.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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
        drawingSetting.setOnClickListener { presenter.showDrawingSettings() }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    private inner class DrawingViewListener : DrawingView.Listener {
        override fun onNewDrawingItem(item: DrawingItem) {
            presenter.addDrawingItemToHistory(item)
        }

        override fun onStartTouching() {
            presenter.onStartTouching()
        }

        override fun onEndTouching() {
            presenter.onEndTouching()
        }
    }

    private inner class DrawingActivityPresenterListener : DrawingActivityPresenter.Listener {
        override fun showToolbar() {
            toolbar.visibility = View.VISIBLE
        }

        override fun hideToolbar() {
            toolbar.visibility = View.INVISIBLE
        }

        override fun drawHistory(history: List<DrawingItem>) {
            drawingView.drawHistory(history)
        }

        override fun enableHistoryButton(back: Boolean, forward: Boolean) {
            val historyBackResId = if (back) {
                R.drawable.ic_history_back_enable
            } else {
                R.drawable.ic_history_back_disable
            }
            historyBack.setImageResource(historyBackResId)
            val historyForwardResId = if (forward) {
                R.drawable.ic_history_forward_enable
            } else {
                R.drawable.ic_history_forward_disable
            }
            historyForward.setImageResource(historyForwardResId)
        }

        override fun showDrawingSettings() {

        }
    }
}
