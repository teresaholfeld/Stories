/*
 * Copyright (C) 2010 Andrew Gainer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Adapted from Android's CountDownTimer class

package com.teresaholfeld.stories;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Schedule a countdown until a time in the future, with
 * regular notifications on intervals along the way.
 *
  * The calls to {@link #onTick(long)} are synchronized to this object so that
 * one call to {@link #onTick(long)} won't ever occur before the previous
 * callback is complete.  This is only relevant when the implementation of
 * {@link #onTick(long)} takes an amount of time to execute that is significant
 * compared to the countdown interval.
 */
public abstract class CountDownTimerWithPause {

    /**
     * Millis since boot when alarm should stop.
     */
  private long mStopTimeInFuture;

  /**
   * Real time remaining until timer completes
   */
    private long mMillisInFuture;

    /**
     * Total time on timer at start
     */
    private final long mTotalCountdown;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    /**
     * The time remaining on the timer when it was paused, if it is currently paused; 0 otherwise.
     */
    private long mPauseTimeRemaining;

    /**
     * True if timer was started running, false if not.
     */
    private boolean mRunAtStart;

    /**
     * @param millisInFuture The number of millis in the future from the call
     *   to {@link #start} until the countdown is done and {@link #onFinish()}
     *   is called
     * @param countDownInterval The interval in millis at which to execute
     *   {@link #onTick(millisUntilFinished)} callbacks
     * @param runAtStart True if timer should start running, false if not
     */
    public CountDownTimerWithPause(long millisOnTimer, long countDownInterval, boolean runAtStart) {
        mMillisInFuture = millisOnTimer;
        mTotalCountdown = mMillisInFuture;
        mCountdownInterval = countDownInterval;
        mRunAtStart = runAtStart;
    }

    /**
     * Cancel the countdown and clears all remaining messages
     */
    public final void cancel() {
        mHandler.removeMessages(MSG);
    }

    /**
     * Create the timer object.
     */
    public synchronized final CountDownTimerWithPause create() {
        if (mMillisInFuture <= 0) {
            onFinish();
        } else {
          mPauseTimeRemaining = mMillisInFuture;
        }

        if (mRunAtStart) {
          resume();
        }

        return this;
    }

    /**
     * Pauses the counter.
     */
  public void pause () {
    if (isRunning()) {
      mPauseTimeRemaining = timeLeft();
      cancel();
    }
  }

  /**
   * Resumes the counter.
   */
  public void resume () {
    if (isPaused()) {
      mMillisInFuture = mPauseTimeRemaining;
      mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
          mHandler.sendMessage(mHandler.obtainMessage(MSG));
      mPauseTimeRemaining = 0;
    }
  }

  /**
   * Tests whether the timer is paused.
   * @return true if the timer is currently paused, false otherwise.
   */
  public boolean isPaused () {
    return (mPauseTimeRemaining > 0);
  }

  /**
   * Tests whether the timer is running. (Performs logical negation on {@link #isPaused()})
   * @return true if the timer is currently running, false otherwise.
   */
  public boolean isRunning() {
    return (! isPaused());
  }

  /**
   * Returns the number of milliseconds remaining until the timer is finished
   * @return number of milliseconds remaining until the timer is finished
   */
  public long timeLeft() {
    long millisUntilFinished;
    if (isPaused()) {
      millisUntilFinished = mPauseTimeRemaining;
    } else {
      millisUntilFinished = mStopTimeInFuture - SystemClock.elapsedRealtime();
      if (millisUntilFinished < 0) millisUntilFinished = 0;
    }
    return millisUntilFinished;
  }

  /**
   * Returns the number of milliseconds in total that the timer was set to run
   * @return number of milliseconds timer was set to run
   */
  public long totalCountdown() {
    return mTotalCountdown;
  }

  /**
   * Returns the number of milliseconds that have elapsed on the timer.
   * @return the number of milliseconds that have elapsed on the timer.
   */
  public long timePassed() {
    return mTotalCountdown - timeLeft();
  }

  /**
   * Returns true if the timer has been started, false otherwise.
   * @return true if the timer has been started, false otherwise.
   */
  public boolean hasBeenStarted() {
    return (mPauseTimeRemaining <= mMillisInFuture);
  }

    /**
     * Callback fired on regular interval
     * @param millisUntilFinished The amount of time until finished
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();


    private static final int MSG = 1;


    // handles counting down
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountDownTimerWithPause.this) {
                long millisLeft = timeLeft();

                if (millisLeft <= 0) {
                    cancel();
                  onFinish();
                } else if (millisLeft < mCountdownInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), millisLeft);
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);

                    // take into account user's onTick taking time to execute
                    long delay = mCountdownInterval - (SystemClock.elapsedRealtime() - lastTickStart);

                    // special case: user's onTick took more than mCountdownInterval to
                    // complete, skip to next interval
                    while (delay < 0) delay += mCountdownInterval;

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
