package com.mitsuki.armory.systemoverlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.animation.addListener
import androidx.core.view.isVisible

class SimpleOverlay @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OverlayView {

    override var isAdded: Boolean = false

    private var mAppearAnimation: ValueAnimator? = null
    private var mDisappearAnimation: ValueAnimator? = null

    private val mLayoutParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            type = windowType()
            format = PixelFormat.RGBA_8888
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
            x = 0
            y = 0
        }
    }

    override fun view(): View = this

    override fun layoutParams(): WindowManager.LayoutParams = mLayoutParams

    override fun appear() {
        isVisible = true
        update()
        post {
            if (!mAppearAnimation.isAnimationRunning()) {
                translationY = -height.toFloat()
                mAppearAnimation = ValueAnimator.ofFloat(translationY, 0f).apply {
                    duration = 300
                    addUpdateListener {
                        translationY = it.animatedValue as Float
                        update()
                    }
                    start()
                }
                if (mDisappearAnimation.isAnimationRunning())
                    mDisappearAnimation?.cancel()
            }
        }
    }

    override fun disappear() {
        if (!mDisappearAnimation.isAnimationRunning()) {
            mDisappearAnimation = ValueAnimator.ofFloat(translationY, (-height).toFloat()).apply {
                duration = 300
                addUpdateListener {
                    translationY = it.animatedValue as Float
                    update()
                }
                addListener(onEnd = {
                    isVisible = false
                    update()
                })
                start()
            }

            if (mAppearAnimation.isAnimationRunning())
                mAppearAnimation?.cancel()
        }
    }

    fun layout(@LayoutRes layoutRes: Int) {
        LayoutInflater.from(context).inflate(layoutRes, this)
    }

}