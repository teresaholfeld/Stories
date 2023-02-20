package com.teresaholfeld.stories

import android.util.Log
import com.teresaholfeld.stories.PausableStoriesProgressBar.Callback

class EspressoTestPausableStoriesProgressBar : StoriesProgressBar {
    private var duration = PausableStoriesProgressBar.DEFAULT_PROGRESS_DURATION.toLong()
    private var callback: Callback? = null

    var animation: CountDownTimerWithPause? = null

    override fun setDuration(duration: Long) {
        this.duration = duration
    }

    override fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun setMax() {
        finishProgress()
    }

    override fun setMin() {
        finishProgress()
    }

    override fun setMinWithoutCallback() {
        animation?.cancel()
    }

    override fun setMaxWithoutCallback() {
        animation?.cancel()
    }

    private fun finishProgress() {
        animation?.cancel()
        callback?.onFinishProgress()
    }

    override fun startProgress() {
        animation = object : CountDownTimerWithPause(duration,1000L, true) {
            override fun onTick(p0: Long) {
                Log.d("this","okay tick $p0")
            }
            override fun onFinish() {
                callback?.onFinishProgress()
            }
        }.create()
        callback?.onStartProgress()
    }

    override fun pauseProgress() {
        animation?.pause()
    }

    override fun resumeProgress() {
        animation?.resume()
    }

    override fun clear() {
        animation?.cancel()
        animation = null
    }
}
