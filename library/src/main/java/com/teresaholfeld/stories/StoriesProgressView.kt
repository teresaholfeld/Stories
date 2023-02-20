@file:Suppress("unused")

package com.teresaholfeld.stories

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

class StoriesProgressView : LinearLayout {

    private val progressBarLayoutParam = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    private val spaceLayoutParam = LinearLayout.LayoutParams(5, LinearLayout.LayoutParams.WRAP_CONTENT)
    private val defaultColor = ContextCompat.getColor(context, R.color.progress_primary)
    private val defaultBackgroundColor = ContextCompat.getColor(context, R.color.progress_secondary)

    private var progressColor = defaultColor
    private var progressBackgroundColor = defaultBackgroundColor

    private val storiesProgressBars = ArrayList<StoriesProgressBar>()

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
        orientation = LinearLayout.HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StoriesProgressView)
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0)
        progressColor = typedArray.getColor(R.styleable.StoriesProgressView_progressColor, defaultColor)
        progressBackgroundColor = typedArray.getColor(R.styleable.StoriesProgressView_progressBackgroundColor,
            defaultBackgroundColor)
        typedArray.recycle()
        bindViews()
    }

    private fun bindViews() {
        storiesProgressBars.clear()
        removeAllViews()

        for (i in 0 until storiesCount) {
            val p = createProgressBar()
            storiesProgressBars.add(p)
            if (p is PausableStoriesProgressBar) {
                addView(p)
            }
            if (i + 1 < storiesCount) {
                addView(createSpace())
            }
        }
    }

    private fun createProgressBar(): StoriesProgressBar {
        if (isRunningTest()) {
            return EspressoTestPausableStoriesProgressBar()
        } else {
            val p = PausableStoriesProgressBar(context, progressColor, progressBackgroundColor)
            p.layoutParams = progressBarLayoutParam
            return p
        }

    }

    private fun createSpace(): View {
        val v = View(context)
        v.layoutParams = spaceLayoutParam
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
        if (current >= storiesProgressBars.size || current < 0) return
        val p = storiesProgressBars[current]
        wasSkippedForward = true
        wasSkippedBackward = false
        p.setMax()
    }

    /**
     * Reverse current story
     */
    fun reverse() {
        if (current < 0) return
        val p = storiesProgressBars[current]
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
        for (i in storiesProgressBars.indices) {
            storiesProgressBars[i].setDuration(duration)
            storiesProgressBars[i].setCallback(callback(i))
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
        for (i in storiesProgressBars.indices) {
            storiesProgressBars[i].setDuration(durations[i])
            storiesProgressBars[i].setCallback(callback(i))
        }
    }

    private fun callback(index: Int): PausableStoriesProgressBar.Callback {
        return object : PausableStoriesProgressBar.Callback {

            override fun onStartProgress() {
                current = index
            }

            override fun onFinishProgress() {
                if (wasSkippedBackward) {
                    storiesListener?.onPrev()

                    if (current > 0) {
                        val p = storiesProgressBars[current - 1]
                        p.setMinWithoutCallback()
                        if (current == storiesProgressBars.size - 1) {
                            storiesProgressBars[current].setMinWithoutCallback()
                        }
                        storiesProgressBars[--current].startProgress()
                    } else {
                        storiesProgressBars[current].startProgress()
                    }
                    wasSkippedBackward = false
                    wasSkippedForward = false
                    return
                }

                val next = current + 1
                if (next <= storiesProgressBars.size - 1) {
                    storiesListener?.onNext()
                    storiesProgressBars[next].startProgress()
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
        storiesProgressBars.getOrNull(0)?.startProgress()
    }

    /**
     * Start progress animation from specific progress
     */
    fun startStories(from: Int) {
        if (storiesProgressBars.size == 0) return
        for (i in 0 until from) {
            storiesProgressBars[i].setMaxWithoutCallback()
        }
        storiesProgressBars[from].startProgress()
    }

    fun clear() {
        storiesProgressBars.clear()
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
        for (p in storiesProgressBars) {
            p.clear()
        }
    }

    /**
     * Pause story
     */
    fun pause() {
        if (current < 0) return
        storiesProgressBars.getOrNull(current)?.pauseProgress()
    }

    /**
     * Resume story
     */
    fun resume() {
        if (current < 0) return
        storiesProgressBars.getOrNull(current)?.resumeProgress()
    }

    companion object {
        private var isRunningTest: AtomicBoolean? = null

        @Synchronized
        fun isRunningTest(): Boolean {
            if (null == isRunningTest) {
                val istest: Boolean = try {
                    // "android.support.test.espresso.Espresso" if you haven't migrated to androidx yet
                    Class.forName("androidx.test.espresso.Espresso")
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }
                isRunningTest = AtomicBoolean(istest)
            }
            return isRunningTest!!.get()
        }
    }

}
