package com.didim99.sat.resconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.didim99.sat.MyLog;
import java.lang.ref.WeakReference;

/**
 * Textures converter wrapper class
 * Created by didim99 on 23.01.18.
 */

class TexTask extends AsyncTask<TexConverter.Config, Integer, String>
  implements TexConverter.ProgressListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexTask";

  static final class Event {
    static final int START = 1;
    static final int FINISH = 2;
  }

  private WeakReference<Context> appContext;
  private EventListener listener;
  private Bitmap preview;
  private String result;

  TexTask(Context context) {
    appContext = new WeakReference<>(context);
  }

  void registerEventListener(EventListener listener) {
    this.listener = listener;
  }

  void unregisterEventListener() {
    this.listener = null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null)
      listener.onTaskEvent(Event.START, null);
  }

  @Override
  protected String doInBackground(TexConverter.Config... configs) {
    MyLog.d(LOG_TAG, "Executing...");

    TexConverter converter = new TexConverter(
      appContext.get(), configs[0], this);
    converter.convert();
    preview = converter.getPreview();
    result = converter.getStatus();

    return result;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    MyLog.d(LOG_TAG, "Executing completed");
    if (listener != null)
      listener.onTaskEvent(Event.FINISH, result);
    listener = null;
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    super.onProgressUpdate(values);
    if (listener != null)
      listener.onProgressUpdate(values[0], values[1]);
  }

  @Override
  public void onConverterProgressUpdate(int max, int current) {
    publishProgress(max, current);
  }

  String getResult() {
    return this.result;
  }

  Bitmap getPreview() {
    return preview;
  }

  interface EventListener {
    void onTaskEvent(int event, String result);
    void onProgressUpdate(int max, int current);
  }
}
