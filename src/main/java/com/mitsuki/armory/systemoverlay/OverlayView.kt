package com.mitsuki.armory.systemoverlay

import android.view.View
import android.view.WindowManager

interface OverlayView {
    var isAdded: Boolean

    fun appear()
    fun disappear()

    fun view(): View
    fun layoutParams(): WindowManager.LayoutParams
}

