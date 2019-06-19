package com.teresaholfeld.stories

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.Transformation
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
internal class PausableProgressBar constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               progressColor: Int,
                                               progressBackgroundColor: Int)
    : FrameLayout(context, attrs) {

    private val frontProgressView: View?
    private val backProgressView: View?

    private var animation: PausableScaleAnimation? = null
    private var duration = DEFAULT_PROGRESS_DURATION.toLong()
    private var callback: Callback? = null

    internal interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

    constructor(context: Context,
                progressColor: Int,
                progressBackgroundColor: Int)
        : this(context, null, progressColor, progressBackgroundColor)

    init {
        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this)
        frontProgressView = findViewById(R.id.front_progress)
        backProgressView = findViewById(R.id.back_progress)
        backProgressView?.setBackgroundColor(progressBackgroundColor)
        frontProgressView?.setBackgroundColor(progressColor)
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setMax() {
        finishProgress()
        frontProgressView?.clearAnimation()
        frontProgressView?.visibility = View.VISIBLE
    }

    fun setMin() {
        finishProgress()
    }

    fun setMinWithoutCallback() {
        animation?.setAnimationListener(null)
        animation?.cancel()
    }

    fun setMaxWithoutCallback() {
        animation?.setAnimationListener(null)
        animation?.cancel()
        frontProgressView?.clearAnimation()
        frontProgressView?.visibility = View.VISIBLE
    }

    private fun finishProgress() {
        animation?.setAnimationListener(null)
        animation?.cancel()
        callback?.onFinishProgress()
    }

    fun startProgress() {
        animation = PausableScaleAnimation(0f, 1f, 1f, 1f, Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_SELF, 0f)
        animation?.duration = duration
        animation?.interpolator = LinearInterpolator()
        animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                frontProgressView?.visibility = View.VISIBLE
                callback?.onStartProgress()
            }

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                callback?.onFinishProgress()
            }
        })
        animation?.fillAfter = true
        frontProgressView?.startAnimation(animation)
    }

    fun pauseProgress() {
        animation?.pause()
    }

    fun resumeProgress() {
        animation?.resume()
    }

    fun clear() {
        animation?.setAnimationListener(null)
        animation?.cancel()
        animation = null
    }

    private inner class PausableScaleAnimation internal constructor(fromX: Float,
                                                                    toX: Float,
                                                                    fromY: Float,
                                                                    toY: Float,
                                                                    pivotXType: Int,
                                                                    pivotXValue: Float,
                                                                    pivotYType: Int,
                                                                    pivotYValue: Float)
        : ScaleAnimation(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue) {

        private var mElapsedAtPause: Long = 0
        private var mPaused = false

        override fun getTransformation(currentTime: Long, outTransformation: Transformation, scale: Float): Boolean {
            if (mPaused && mElapsedAtPause == 0L) {
                mElapsedAtPause = currentTime - startTime
            }
            if (mPaused) {
                startTime = currentTime - mElapsedAtPause
            }
            return super.getTransformation(currentTime, outTransformation, scale)
        }

        /***
         * pause animation
         */
        internal fun pause() {
            if (mPaused) return
            mElapsedAtPause = 0
            mPaused = true
        }

        /***
         * resume animation
         */
        internal fun resume() {
            mPaused = false
        }
    }

    companion object {
        private const val DEFAULT_PROGRESS_DURATION = 2000
    }
}
