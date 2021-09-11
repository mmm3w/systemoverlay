package com.mitsuki.armory.systemoverlay

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

inline fun Context.overlayPermission(after: (Intent?) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Settings.canDrawOverlays(this)) {
            after(null)
        } else {
            after(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            })
        }
    } else {
        after(null)
    }
}

fun windowType(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
    }
}

fun OverlayView.update() {
    OverlayManager.update(this)
}

internal fun ValueAnimator?.isAnimationRunning(): Boolean {
    if (this == null) return false
    return isStarted && isRunning
}
