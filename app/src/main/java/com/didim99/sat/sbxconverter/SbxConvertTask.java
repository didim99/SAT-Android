package com.didim99.sat.sbxconverter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils.Timer;
import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/*
 * Created by didim99 on 11.08.17.
 */

class SbxConvertTask extends AsyncTask<SbxConverter.Config, Void, String> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_sbxConvertTask";

  private WeakReference<SbxConvertActivity> activityRef;
  private SoftReference<Context> appContext;
  private String result;
  private Toast toastMsg;
  private Timer timer;

  SbxConvertTask(Context ctx) {
    appContext = new SoftReference<>(ctx);
    toastMsg = Toast.makeText(ctx, "", Toast.LENGTH_LONG);
    toastMsg.setGravity(Gravity.TOP,
      ctx.getResources().getDimensionPixelOffset(R.dimen.toastPosX),
      ctx.getResources().getDimensionPixelOffset(R.dimen.toastPosY)
    );
  }

  void link (SbxConvertActivity act) {
    activityRef = new WeakReference<>(act);
  }

  void unLink () {
    activityRef = null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    this.timer = new Timer();
    if (activityRef != null) {
      activityRef.get().uiLock (true);
    }
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
    if (activityRef != null) {
      activityRef.get().setTaskResult(result);
      activityRef.get().uiLock(false);
    }
  }

  String getResult() {
    return this.result;
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
      toastMsg.setText(appContext.get().getString(R.string.error, fileStatus));
      toastMsg.show();
    }
    return flag;
  }

  private boolean getAccessToFile (String path) {
    boolean flag = false;

    if (RootShell.hasRootAccess()) {
      RootShell.exec("chmod 606 " + path);
      if (!RootShell.getError().contains("No such file or directory"))
        flag = true;
    }

    return flag;
  }
}
