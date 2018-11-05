package com.hembitski.radial.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.hembitski.radial.R
import kotlinx.android.synthetic.main.fragment_drawing_settings.*


class DrawingSettingFragment: Fragment() {

    companion object {
        private const val TYPE_NUMBER_OF_REPETITIONS = 0
        private const val TYPE_BRUSH_DIAMETER = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawing_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        seekBarNumberOfSectors.setOnSeekBarChangeListener(SeekBarListener(TYPE_NUMBER_OF_REPETITIONS))
        seekBarBrushDiameter.setOnSeekBarChangeListener(SeekBarListener(TYPE_BRUSH_DIAMETER))
    }

    private inner class SeekBarListener(val type: Int): SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if(fromUser) {
                when(type) {
                    TYPE_NUMBER_OF_REPETITIONS -> iconNumberOfSectors.setNumberOfRepetitions(progress)
                    TYPE_BRUSH_DIAMETER -> iconBrushDiameter.setDiameter(progress.toFloat())
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
}