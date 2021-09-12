package com.mitsuki.armory.systemoverlay

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import androidx.core.view.isVisible
import java.lang.ref.WeakReference


@Suppress("MemberVisibilityCanBePrivate")
object OverlayManager {

    private lateinit var mWindowManager: WindowManager
    var screenWidth: Int = 0
    var screenHeight: Int = 0

    private var mCurrentOverlay: WeakReference<OverlayView>? = null

    private val mOverlayMap: MutableMap<Int, WeakReference<OverlayView>> by lazy { hashMapOf<Int, WeakReference<OverlayView>>() }

    fun init(context: Context) {
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Point().apply {
            mWindowManager.defaultDisplay.getSize(this)
            screenWidth = x
            screenHeight = y
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            with(mWindowManager.currentWindowMetrics.bounds) {
//                screenHeight = width()
//                screenHeight = height()
//            }
//        } else {
//            Point().apply {
//                @Suppress("DEPRECATION")
//                mWindowManager.defaultDisplay.getSize(this)
//                screenWidth = x
//                screenHeight = y
//            }
//
//        }
    }

    fun switch(overlayView: OverlayView?) {
        mCurrentOverlay?.get()?.apply { hide(this) }
        mCurrentOverlay = overlayView?.let {
            show(it)
            WeakReference(it)
        }
    }

    fun show(overlayView: OverlayView) {
        with(overlayView) {
            add(overlayView)
            appear()
        }
    }

    fun hide(overlayView: OverlayView) {
        with(overlayView) {
            if (isAdded) disappear()
        }
    }

    fun add(overlayView: OverlayView) {
        with(overlayView) {
            if (!isAdded) {
                isAdded = try {
                    with(view()) {
                        layoutParams = layoutParams()
                        isVisible = false
                        mWindowManager.addView(this, layoutParams())
                    }
                    if (mOverlayMap[overlayView.hashCode()]?.get() == null) {
                        mOverlayMap[overlayView.hashCode()] = WeakReference(overlayView)
                    }
                    true
                } catch (exception: IllegalStateException) {
                    if (exception.message?.contains("already been added") == true) {
                        true
                    } else {
                        throw exception
                    }
                }
            }
        }
    }

    fun remove(overlayView: OverlayView) {
        with(overlayView) {
            if (isAdded) {
                isAdded = try {
                    mWindowManager.removeView(view())
                    mOverlayMap.remove(overlayView.hashCode())
                    false
                } catch (exception: IllegalStateException) {
                    if (exception.message?.contains("is attached to") == true) {
                        false
                    } else {
                        throw exception
                    }
                }
            }
        }
    }

    fun update(overlayView: OverlayView) {
        with(overlayView) {
            if (isAdded)
                mWindowManager.updateViewLayout(view(), layoutParams())
        }
    }

    fun exit() {
        for (item in mOverlayMap) {
            item.value.get()?.apply {
                isAdded = false
                try {
                    mWindowManager.removeView(view())
                } catch (e: IllegalStateException) {

                }
            }
        }
        mOverlayMap.clear()
        mCurrentOverlay = null
    }
}