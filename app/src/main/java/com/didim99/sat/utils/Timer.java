package com.didim99.sat.utils;

import java.util.Locale;

/**
 * simple timer class
 * Created by didim99 on 28.01.18.
 */
public class Timer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Timer";
  private static final long TICKS_PER_MS = 1000000;
  private long timer;

  public void start () {
    timer = System.nanoTime();
  }

  public void stop () {
    timer = System.nanoTime() - timer;
  }

  public long getMillis() {
    return timer / TICKS_PER_MS;
  }

  public String getStr() {
    return longToTime (timer);
  }

  private String longToTime (long time) {
    time /= TICKS_PER_MS;
    if (time < 60000)
      return String.format(Locale.US, "%.3f сек.", time / 1000f);
    else {
      String out;
      long millis = time % 1000;
      time /= 1000;
      long sec = time % 60;
      time /= 60;
      long min = time % 60;
      time /= 60;
      if (time == 0)
        out = String.format(Locale.US, "%d:%02d.%03d", min, sec, millis);
      else
        out = String.format(Locale.US, "%d:%02d:%02d.%03d", time, min, sec, millis);

      MyLog.d(LOG_TAG, "Converting time: " + timer + " --> " + out);
      return out;
    }
  }
}
