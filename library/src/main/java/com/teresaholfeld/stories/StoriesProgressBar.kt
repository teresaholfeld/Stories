package com.teresaholfeld.stories

import com.teresaholfeld.stories.PausableStoriesProgressBar.Callback

interface StoriesProgressBar {
    fun setDuration(duration: Long)
    fun setCallback(callback: Callback)
    fun setMax()
    fun setMin()
    fun setMinWithoutCallback()
    fun setMaxWithoutCallback()
    fun startProgress()
    fun pauseProgress()
    fun resumeProgress()
    fun clear()
}
