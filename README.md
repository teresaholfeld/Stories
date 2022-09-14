# Stories

Stories is a library that shows a horizontal progress like Instagram stories.

This project has been forked and extended from [shts/StoriesProgressView](https://github.com/shts/StoriesProgressView).

[![](https://jitpack.io/v/teresaholfeld/Stories.svg)](https://jitpack.io/#teresaholfeld/Stories)

<img src="image/capture.png" width="200" />

<img src="image/image.gif" width="200" /> 

The person in these pictures and the pictures in the sample app is [Yui Kobayashi](http://www.keyakizaka46.com/s/k46o/artist/07).

## Install

Add jitpack to your repositories in your root `build.gradle`:

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}

```

Add the dependency in your app `build.gradle`:

```
dependencies {
    implementation 'com.github.teresaholfeld:Stories:1.1.4'
}

```

## How to Use

To see how a StoriesProgressView can be added to your xml layouts, check the sample project.

```xml
    <com.teresaholfeld.stories.StoriesProgressView
        android:id="@+id/stories"
        android:layout_width="match_parent"
        android:layout_height="3dp"
        android:layout_gravity="top"
        android:layout_marginTop="8dp" />
```
Overview

```kotlin
class MainActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    
    private var storiesProgressView: StoriesProgressView? = null
    private var counter = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storiesProgressView?.setStoriesCount(PROGRESS_COUNT) // <- set stories
        storiesProgressView?.setStoryDuration(3000L) // <- set a story duration
        storiesProgressView?.setStoriesListener(this) // <- set listener
        counter = 2
        storiesProgressView?.startStories(counter) // <- start progress
    }
    
    override fun onNext() {
        Toast.makeText(this, "onNext", Toast.LENGTH_SHORT).show()
    }
    
    override fun onPrev() {
        // Called when skipping to the previous screen
        Toast.makeText(this, "onPrev", Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        // Very important !
        storiesProgressView?.destroy()
        super.onDestroy()
    }
}
```

### Skip and reverse the story progress

<img src="image/skip-reverse.gif" width="200" />

```kotlin
  storiesProgressView?.skip()
  storiesProgressView?.reverse()
```

### Pause and resume the story progress

<img src="image/pause-resume.gif" width="200" />

```kotlin
  storiesProgressView?.pause()
  storiesProgressView?.resume()
```

### Change the color of the progress bar

You can change the foreground color and background color of the 
progress bar.

<img src="image/progress-color.png" width="200" />

To do this, you can add the attributes to your layout xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
   <com.teresaholfeld.stories.StoriesProgressView
           android:id="@+id/stories"
           android:layout_width="match_parent"
           android:layout_height="3dp"
           android:layout_gravity="top"
           android:layout_marginTop="8dp"
           android:paddingLeft="8dp"
           android:paddingRight="8dp"
           app:progressBackgroundColor="@color/purple"
           app:progressColor="@color/colorAccent"/>
           
   <!-- ... -->
</LinearLayout>
```
## Compose support
None planned. PRs welcome!

## License

```
Copyright (C) 2019 Teresa Holfeld (teresaholfeld), 2017 Shota Saito (shts)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
