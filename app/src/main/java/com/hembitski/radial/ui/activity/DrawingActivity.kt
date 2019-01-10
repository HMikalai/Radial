package com.hembitski.radial.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.hembitski.radial.R
import com.hembitski.radial.data.drawing.DrawingItem
import com.hembitski.radial.data.drawing.HistoryDrawingItem
import com.hembitski.radial.data.drawing.settings.DrawingSettings
import com.hembitski.radial.ui.fragment.DrawingSettingFragment
import com.hembitski.radial.ui.presenter.DrawingActivityPresenter
import com.hembitski.radial.ui.view.DrawingView
import kotlinx.android.synthetic.main.activity_main.*

class DrawingActivity : AppCompatActivity(), DrawingSettingFragment.Listener {

    companion object {
        private const val TAG_DRAWING_SETTINGS_FRAGMENT = "TAG_DRAWING_SETTINGS_FRAGMENT"
    }

    private val presenter = DrawingActivityPresenter(DrawingActivityPresenterListener())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView.listener = DrawingViewListener()
        historyBack.setOnClickListener { presenter.onHistoryBack() }
        historyForward.setOnClickListener { presenter.onHistoryForward() }
        drawingSetting.setOnClickListener { presenter.showDrawingSettings() }
        clearAll.setOnClickListener { presenter.clearAll() }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        drawingView.startCalculationThread()
    }

    override fun onPause() {
        super.onPause()
        drawingView.stopCalculationThread()
    }

    override fun applyDrawingSettings(settings: DrawingSettings) {
        drawingView.settings = settings
    }

    override fun hideSettingsFragment() {
        setDrawingSettingFragmentVisibility(false)
    }

    private fun setDrawingSettingFragmentVisibility(visible: Boolean) {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_DRAWING_SETTINGS_FRAGMENT)
                ?: DrawingSettingFragment.newInstance(drawingView.settings)
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.show_fragment_animation, R.anim.hide_fagment_animation)
        if (visible) {
            ft.replace(R.id.fragmentContainer, fragment, TAG_DRAWING_SETTINGS_FRAGMENT)
        } else {
            ft.remove(fragment)
        }
        ft.commit()
    }

    private inner class DrawingViewListener : DrawingView.Listener {
        override fun onNewDrawingItem(item: HistoryDrawingItem) {
            presenter.addDrawingItemToHistory(item)
        }

        override fun onStartTouching() {
            presenter.onStartTouching()
        }

        override fun onEndTouching() {
            presenter.onEndTouching()
        }

        override fun onToFastDrawing() {
            Toast.makeText(this@DrawingActivity, "To fast drawing", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class DrawingActivityPresenterListener : DrawingActivityPresenter.Listener {
        override fun showToolbar() {
            toolbar.visibility = View.VISIBLE
        }

        override fun hideToolbar() {
            toolbar.visibility = View.INVISIBLE
        }

        override fun drawHistory(history: List<HistoryDrawingItem>) {
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
            setDrawingSettingFragmentVisibility(true)
        }

        override fun hideDrawingSettings() {
            setDrawingSettingFragmentVisibility(false)
        }

        override fun clearAll() {
            drawingView.clearAll()
        }
    }
}
