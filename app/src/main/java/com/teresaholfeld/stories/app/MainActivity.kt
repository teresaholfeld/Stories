package com.teresaholfeld.stories.app

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.teresaholfeld.stories.StoriesProgressView

class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private lateinit var storiesProgressView: StoriesProgressView
    private var image: ImageView? = null

    private var counter = 0
    private val resources = intArrayOf(
        R.drawable.sample1,
        R.drawable.sample2,
        R.drawable.sample3,
        R.drawable.sample4,
        R.drawable.sample5,
        R.drawable.sample6
    )

//    private val durations = longArrayOf(500L, 1000L, 1500L, 4000L, 5000L, 1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        storiesProgressView = findViewById(R.id.stories)
        storiesProgressView.setStoriesCount(PROGRESS_COUNT)
        storiesProgressView.setStoryDuration(3000L)
        // or
        // storiesProgressView.setStoriesCountWithDurations(durations);

        storiesProgressView.setStoriesListener(this)

        counter = 2
        storiesProgressView.startStories(counter)

        image = findViewById<View>(R.id.image) as ImageView
        image?.setImageResource(resources[counter])

        // bind reverse view
        val reverse = findViewById<View>(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView.reverse() }

        // bind skip view
        val skip = findViewById<View>(R.id.skip)
        skip.setOnClickListener { storiesProgressView.skip() }
    }

    override fun onNext() {
        image?.setImageResource(resources[++counter])
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        image?.setImageResource(resources[--counter])
    }

    override fun onComplete() {}

    override fun onDestroy() {
        // Very important !
        storiesProgressView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val PROGRESS_COUNT = 6
    }
}
