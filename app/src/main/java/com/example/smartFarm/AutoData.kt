package com.example.smartFarm

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutoData(
    var device: String,
    var time: String
): Parcelable {
    override fun toString(): String {
        return "device:$device, time:$time"
    }
}
