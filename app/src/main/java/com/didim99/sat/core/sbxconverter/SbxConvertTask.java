package com.didim99.sat.core.sbxconverter;

import android.content.Context;
import android.os.AsyncTask;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Timer;
import com.didim99.sat.utils.Utils;
import java.lang.ref.WeakReference;

/**
 * Created by didim99 on 11.08.17.
 */

public class SbxConvertTask extends AsyncTask<SbxConverter.Config, Void, String> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_sbxConvertTask";

  public static final class Event {
    public static final int START = 1;
    public static final int FINISH = 2;
  }

  private WeakReference<Context> appContext;
  private EventListener listener;
  private String result;
  private Timer timer;

  public SbxConvertTask(Context ctx) {
    appContext = new WeakReference<>(ctx);
    this.timer = new Timer();
  }

  public void registerEventListener(EventListener listener) {
    this.listener = listener;
    switch (getStatus()) {
      case RUNNING:
        listener.onTaskEvent(Event.START, null);
        break;
      case FINISHED:
        listener.onTaskEvent(Event.FINISH, result);
        break;
    }
  }

  public void unregisterEventListener() {
    this.listener = null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null)
      listener.onTaskEvent(Event.START, null);
  }

  @Override
  protected String doInBackground(SbxConverter.Config... configs) {
    MyLog.d(LOG_TAG, "Executing...");
    SbxConverter.Config config = configs[0];
    boolean started = false;
    timer.start();

    if (Utils.checkPath(appContext.get(), config.getPath(), false)) {
      started = true;
      SbxConverter converter = new SbxConverter(appContext.get(), config);
      converter.convert();
      result = converter.getStatus();
    }

    timer.stop();
    if (started)
      result = String.format(result, timer.getStr());

    return result;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    MyLog.d(LOG_TAG, "Executing completed");
    if (listener != null)
      listener.onTaskEvent(Event.FINISH, result);
    appContext.clear();
    listener = null;
  }

  public interface EventListener {
    void onTaskEvent(int event, String result);
  }
}
