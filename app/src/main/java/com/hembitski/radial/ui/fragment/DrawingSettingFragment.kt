package com.hembitski.radial.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.hembitski.radial.R
import kotlinx.android.synthetic.main.fragment_drawing_settings.*


class DrawingSettingFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawing_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        iconNumberOfSectors.setNumberOfRepetitions(8)
        seekBarNumberOfSectors.setOnSeekBarChangeListener(SeekBarListener())
    }

    private inner class SeekBarListener: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if(fromUser) {
                iconNumberOfSectors.setNumberOfRepetitions(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
}