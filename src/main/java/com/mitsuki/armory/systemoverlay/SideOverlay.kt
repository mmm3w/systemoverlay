package com.mitsuki.armory.systemoverlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.animation.addListener
import androidx.core.view.isVisible

class SideOverlay @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OverlayView {

    init {
        alpha = 0f
        scaleX = 0f
        scaleY = 0f
    }

    override var isAdded: Boolean = false

    private var mAppearAnimation: ValueAnimator? = null
    private var mDisappearAnimation: ValueAnimator? = null
    private var mMoveToSideAnimation: ValueAnimator? = null

    private var dragMark: Boolean = false
    private var lastTouchX: Int = 0
    private var lastTouchY: Int = 0
    private val maxX: Int
        get() = OverlayManager.screenWidth - width
    private val maxY: Int
        get() = OverlayManager.screenHeight - height
    private var lastPositionX: Int = maxX
    private var lastPositionY: Int = maxY / 3

    private val mLayoutParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            type = windowType()
            format = PixelFormat.RGBA_8888
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            x = lastPositionX
            y = lastPositionY
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (lastPositionX > maxX) lastPositionX = maxX
        if (lastPositionY > maxY) lastPositionY = maxY
        event?.apply { onDrag(this) }
        if (dragMark) return true
        return super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply { onDrag(this) }
        if (dragMark) return true
        return super.onTouchEvent(event)
    }

    private fun onDrag(event: MotionEvent) {
        if (mMoveToSideAnimation.isAnimationRunning()) return

        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                dragMark = false
                lastTouchX = rawX
                lastTouchY = rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = rawX - lastTouchX
                val dy = rawY - lastTouchY
                if (!dragMark && dx * dx + dy * dy < 100) return
                dragMark = true
                with(mLayoutParams) {
                    x = lastPositionX + dx
                    y = lastPositionY + dy
                }
                update()
            }
            MotionEvent.ACTION_UP -> {
                if (dragMark) {
                    lastPositionX = (lastPositionX + rawX - lastTouchX).coerceIn(0, maxX)
                    lastPositionY = (lastPositionY + rawY - lastTouchY).coerceIn(0, maxY)
                    closeToSide()
                }
            }
        }
    }

    private fun closeToSide() {
        if (mMoveToSideAnimation.isAnimationRunning()) mMoveToSideAnimation?.cancel()
        mMoveToSideAnimation = ValueAnimator.ofInt(
                lastPositionX,
                if (lastPositionX > OverlayManager.screenWidth / 2) maxX else 0
        ).apply {
            duration = 400
            addUpdateListener {
                lastPositionX = it.animatedValue as Int
                mLayoutParams.x = lastPositionX
                update()
            }
            start()
        }
    }

    fun layout(@LayoutRes layoutRes: Int) {
        LayoutInflater.from(context).inflate(layoutRes, this)
    }

    override fun view(): View = this

    override fun layoutParams(): WindowManager.LayoutParams = mLayoutParams

    override fun appear() {
        isVisible = true
        update()
        post {
            if (!mAppearAnimation.isAnimationRunning()) {
                mAppearAnimation = ValueAnimator.ofFloat(alpha, 1f).apply {
                    duration = 300
                    addUpdateListener {
                        with(it.animatedValue as Float) {
                            alpha = this
                            scaleY = this
                            scaleX = this
                        }
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
            mDisappearAnimation = ValueAnimator.ofFloat(alpha, 0f).apply {
                duration = 300
                addUpdateListener {
                    with(it.animatedValue as Float) {
                        alpha = this
                        scaleY = this
                        scaleX = this
                    }
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
}