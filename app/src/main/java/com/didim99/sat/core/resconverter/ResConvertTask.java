package com.didim99.sat.core.resconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.didim99.sat.utils.MyLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Resources converter wrapper class
 * Created by didim99 on 23.01.18.
 */

public class ResConvertTask extends AsyncTask<ResConverter.Config, Integer, Void>
  implements ResConverter.ProgressListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexTask";

  public static final class Event {
    public static final int START = 1;
    public static final int FINISH = 2;
  }

  private WeakReference<Context> appContext;
  private EventListener listener;
  private int type;

  private ArrayList<String> fileList;
  private String result;
  private Bitmap preview;

  public ResConvertTask(Context context, int type) {
    appContext = new WeakReference<>(context);
    this.type = type;
  }

  public void registerEventListener(EventListener listener) {
    this.listener = listener;
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
  protected Void doInBackground(ResConverter.Config... configs) {
    MyLog.d(LOG_TAG, "Executing...");
    ResConverter converter;

    switch (type) {
      case ResConverter.Type.TEXTURES:
        converter = new TexConverter(
          appContext.get(), configs[0], this);
        converter.convert();
        preview = ((TexConverter) converter).getPreview();
        break;
      case ResConverter.Type.SOUNDS:
        converter = new SoundConverter(
          appContext.get(), configs[0], this);
        converter.convert();
        fileList = converter.getFileList();
        break;
      default:
        throw new IllegalArgumentException("Unknown converter type");
    }

    result = converter.getStatus();
    return null;
  }

  @Override
  protected void onPostExecute(Void res) {
    super.onPostExecute(res);
    MyLog.d(LOG_TAG, "Executing completed");
    if (listener != null)
      listener.onTaskEvent(Event.FINISH, result);
    appContext.clear();
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

  public String getResult() {
    return result;
  }

  public Bitmap getPreview() {
    return preview;
  }

  public ArrayList<String> getFileList() {
    return fileList;
  }

  public interface EventListener {
    void onTaskEvent(int event, String result);
    void onProgressUpdate(int max, int current);
  }
}
