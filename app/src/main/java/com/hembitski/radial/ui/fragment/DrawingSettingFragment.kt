package com.hembitski.radial.ui.fragment

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.hembitski.radial.R
import com.hembitski.radial.data.drawing.settings.DrawingSettings
import kotlinx.android.synthetic.main.fragment_drawing_settings.*


class DrawingSettingFragment : Fragment() {

    companion object {
        private const val TYPE_NUMBER_OF_REPETITIONS = 0
        private const val TYPE_BRUSH_DIAMETER = 1

        private const val KEY_SETTINGS = "KEY_SETTINGS"

        fun newInstance(settings: DrawingSettings): DrawingSettingFragment {
            val args = Bundle()
            args.putParcelable(KEY_SETTINGS, settings)
            val fragment = DrawingSettingFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is Listener) {
            listener = activity as Listener
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawing_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val settings = arguments?.getParcelable<DrawingSettings>(KEY_SETTINGS)
        settings?.let {
            seekBarNumberOfSectors.progress = it.numberOfSectors
            iconNumberOfSectors.defValue = it.numberOfSectors
            seekBarBrushDiameter.progress = it.brushDiameter.toInt()
            iconBrushDiameter.setDiameter(it.brushDiameter)
            colorLayout.setBackgroundColor(it.color)
            switchSmooth.isChecked = it.smooth
            switchMirrorDrawing.isChecked = it.mirrorDrawing
        }

        seekBarNumberOfSectors.setOnSeekBarChangeListener(SeekBarListener(TYPE_NUMBER_OF_REPETITIONS))
        seekBarBrushDiameter.setOnSeekBarChangeListener(SeekBarListener(TYPE_BRUSH_DIAMETER))
        apply.setOnClickListener { applyDrawingSettings() }
    }

    private fun applyDrawingSettings() {
        val numberOfSectors = seekBarNumberOfSectors.progress
        val brushDiameter = seekBarBrushDiameter.progress.toFloat()
        val color = (colorLayout.background as ColorDrawable).color
        val smooth = switchSmooth.isChecked
        val mirrorDrawing = switchMirrorDrawing.isChecked
        listener?.applyDrawingSettings(DrawingSettings(numberOfSectors, brushDiameter, color, smooth, mirrorDrawing))
        listener?.hideSettingsFragment()
    }

    private inner class SeekBarListener(val type: Int) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                when (type) {
                    TYPE_NUMBER_OF_REPETITIONS -> iconNumberOfSectors.setNumberOfSectors(progress)
                    TYPE_BRUSH_DIAMETER -> iconBrushDiameter.setDiameter(progress.toFloat())
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    interface Listener {
        fun applyDrawingSettings(settings: DrawingSettings)

        fun hideSettingsFragment()
    }
}