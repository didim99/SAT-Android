package com.didim99.sat.core.sbxconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.utils.Timer;
import java.io.File;
import java.lang.ref.WeakReference;

/*
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
    if (checkPath(config.getPath())) {
      started = true;
      SbxConverter converter = new SbxConverter(appContext.get(), config);
      converter.convert ();
      result = converter.getStatus ();
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

  private boolean checkPath (String path) {
    MyLog.d(LOG_TAG, "Path checking...\n  " + path);
    String logMsg = "Path check failed: ";
    String fileStatus = "";
    boolean flag = false;

    if (path.equals(""))
      fileStatus = appContext.get().getString(R.string.emptyPath);
    else {
      File file = new File(path);
      flag = file.exists() || getAccessToFile(path);

      if (!flag)
        fileStatus = appContext.get().getString(R.string.fileNotExist);
      else if (file.isDirectory())
        fileStatus = appContext.get().getString(R.string.fileIsDir);
      else if (!file.canRead()) {
        flag = getAccessToFile(path);
        if (!flag)
          fileStatus = appContext.get().getString(R.string.fileNotReadable);
      } else if (!file.canWrite()) {
        flag = getAccessToFile(path);
        if (!flag)
          fileStatus = appContext.get().getString(R.string.fileNotWritable);
      }
      else flag = true;
    }

    if (flag)
      MyLog.d(LOG_TAG, "Path check done");
    else {
      MyLog.e(LOG_TAG, logMsg + fileStatus);
      Toast.makeText(appContext.get(), appContext.get()
        .getString(R.string.error, fileStatus), Toast.LENGTH_LONG).show();
    }
    return flag;
  }

  private boolean getAccessToFile (String path) {
    boolean access = false;

    if (RootShell.hasRootAccess()) {
      RootShell.exec("chmod 606 " + path);
      if (!RootShell.getError().contains("No such file or directory"))
        access = true;
    }

    return access;
  }

  public interface EventListener {
    void onTaskEvent(int event, String result);
  }
}
