package com.hembitski.radial.data.drawing.settings

import android.os.Parcel
import android.os.Parcelable


class DrawingSettings(val numberOfSectors: Int,
                      val brushDiameter: Float,
                      val color: Int,
                      val smooth: Boolean,
                      val mirrorDrawing: Boolean): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readFloat(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(numberOfSectors)
        parcel.writeFloat(brushDiameter)
        parcel.writeInt(color)
        parcel.writeByte(if (smooth) 1 else 0)
        parcel.writeByte(if (mirrorDrawing) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DrawingSettings> {
        override fun createFromParcel(parcel: Parcel): DrawingSettings {
            return DrawingSettings(parcel)
        }

        override fun newArray(size: Int): Array<DrawingSettings?> {
            return arrayOfNulls(size)
        }
    }
}