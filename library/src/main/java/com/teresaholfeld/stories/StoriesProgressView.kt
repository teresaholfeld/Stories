@file:Suppress("unused")

package com.teresaholfeld.stories

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class StoriesProgressView : LinearLayout {

    private val progressBarLayoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)

    private var progressGapInPixels: Int? = null
    private val defaultGap = 5
    private val gapLayoutParams by lazy {
        LayoutParams(progressGapInPixels ?: defaultGap, LayoutParams.MATCH_PARENT)
    }

    private val defaultColor = ContextCompat.getColor(context, R.color.progress_primary)
    private val defaultBackgroundColor = ContextCompat.getColor(context, R.color.progress_secondary)

    private var progressColor = defaultColor
    private var progressBackgroundColor = defaultBackgroundColor

    private val progressBars = mutableListOf<PausableProgressBar>()

    private var storiesCount = -1

    /**
     * pointer of running animation
     */
    private var current = -1
    private var storiesListener: StoriesListener? = null
    internal var isComplete: Boolean = false

    private var wasSkippedForward: Boolean = false
    private var wasSkippedBackward: Boolean = false

    interface StoriesListener {
        fun onNext()

        fun onPrev()

        fun onComplete()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
        : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StoriesProgressView)
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0)
        progressColor = typedArray.getColor(R.styleable.StoriesProgressView_progressColor, defaultColor)
        progressBackgroundColor = typedArray.getColor(R.styleable.StoriesProgressView_progressBackgroundColor, defaultBackgroundColor)
        progressGapInPixels = typedArray.getDimensionPixelSize(R.styleable.StoriesProgressView_progressGap, 0)
        typedArray.recycle()
        bindViews()
    }

    private fun bindViews() {
        progressBars.clear()
        removeAllViews()

        for (i in 0 until storiesCount) {
            val p = createProgressBar()
            progressBars.add(p)
            addView(p)
            if (i + 1 < storiesCount) {
                addView(createSpace())
            }
        }
    }

    private fun createProgressBar(): PausableProgressBar {
        val p = PausableProgressBar(context, progressColor, progressBackgroundColor)
        p.layoutParams = progressBarLayoutParams
        return p
    }

    private fun createSpace(): View {
        val v = View(context)
        v.layoutParams = gapLayoutParams
        return v
    }

    /**
     * Set story count and create views
     *
     * @param storiesCount story count
     */
    fun setStoriesCount(storiesCount: Int) {
        this.storiesCount = storiesCount
        bindViews()
    }

    /**
     * Set storiesListener
     *
     * @param storiesListener StoriesListener
     */
    fun setStoriesListener(storiesListener: StoriesListener) {
        this.storiesListener = storiesListener
    }

    /**
     * Skip current story
     */
    fun skip() {
        if (current >= progressBars.size || current < 0) return
        val p = progressBars[current]
        wasSkippedForward = true
        wasSkippedBackward = false
        p.setMax()
    }

    /**
     * Reverse current story
     */
    fun reverse() {
        if (current < 0) return
        val p = progressBars[current]
        wasSkippedBackward = true
        wasSkippedForward = false
        p.setMin()
    }

    /**
     * Set a story's duration
     *
     * @param duration millisecond
     */
    fun setStoryDuration(duration: Long) {
        for (i in progressBars.indices) {
            progressBars[i].setDuration(duration)
            progressBars[i].setCallback(callback(i))
        }
    }

    /**
     * Set stories count and each story duration
     *
     * @param durations milli
     */
    fun setStoriesCountWithDurations(durations: LongArray) {
        storiesCount = durations.size
        bindViews()
        for (i in progressBars.indices) {
            progressBars[i].setDuration(durations[i])
            progressBars[i].setCallback(callback(i))
        }
    }

    private fun callback(index: Int): PausableProgressBar.Callback {
        return object : PausableProgressBar.Callback {

            override fun onStartProgress() {
                current = index
            }

            override fun onFinishProgress() {
                if (wasSkippedBackward) {
                    storiesListener?.onPrev()

                    if (current > 0) {
                        val p = progressBars[current - 1]
                        p.setMinWithoutCallback()
                        if (current == progressBars.size - 1) {
                            progressBars[current].setMinWithoutCallback()
                        }
                        progressBars[--current].startProgress()
                    } else {
                        progressBars[current].startProgress()
                    }
                    wasSkippedBackward = false
                    wasSkippedForward = false
                    return
                }

                val next = current + 1
                if (next <= progressBars.size - 1) {
                    storiesListener?.onNext()
                    progressBars[next].startProgress()
                } else {
                    isComplete = true
                    storiesListener?.onComplete()
                }

                wasSkippedForward = false
                wasSkippedBackward = false
            }
        }
    }

    /**
     * Start progress animation
     */
    fun startStories() {
        progressBars.getOrNull(0)?.startProgress()
    }

    /**
     * Start progress animation from specific progress
     */
    fun startStories(from: Int) {
        if (progressBars.size == 0) return
        for (i in 0 until from) {
            progressBars[i].setMaxWithoutCallback()
        }
        progressBars[from].startProgress()
    }

    fun clear() {
        progressBars.clear()
        storiesCount = -1
        current = -1
        storiesListener = null
        isComplete = false
        wasSkippedForward = false
        wasSkippedBackward = false
    }

    /**
     * Need to call when Activity or Fragment destroy
     */
    fun destroy() {
        clear()
        for (p in progressBars) {
            p.clear()
        }
    }

    /**
     * Pause story
     */
    fun pause() {
        if (current < 0) return
        progressBars.getOrNull(current)?.pauseProgress()
    }

    /**
     * Resume story
     */
    fun resume() {
        if (current < 0) return
        progressBars.getOrNull(current)?.resumeProgress()
    }
}
