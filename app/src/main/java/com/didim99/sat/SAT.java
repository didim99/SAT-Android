package com.didim99.sat;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;
import com.didim99.sat.core.sbxeditor.utils.InputValidator;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.system.AppUpdateManager;
import com.didim99.sat.system.GlobalEvent;
import com.didim99.sat.system.GlobalEventDispatcher;
import com.didim99.sat.system.StorageManager;
import com.didim99.sat.ui.sbxeditor.UIManager;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.RootShell;
import java.util.Locale;

/**
 * Root application cass
 * Created by didim99 on 17.02.18.
 */

public class SAT extends Application {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_root";

  public static final String ACTION_PICK_MODULE = "com.didim99.sat.pickModule";
  public static final String EXTRA_PART_ID = "com.didim99.sat.partId";

  private GlobalEventDispatcher eventDispatcher;

  @Override
  public void onCreate() {
    super.onCreate();
    MyLog.d(LOG_TAG, "App starting...");
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    eventDispatcher = new GlobalEventDispatcher();

    Context appContext = getApplicationContext();
    UIManager.getInstance().init(appContext);
    InputValidator.getInstance().init(appContext);
    Settings.init(appContext);
    updateLanguage();

    AppUpdateManager.checkAppVersion(appContext);
    StorageManager.checkSystemDirs(appContext);
    StorageManager.checkIconsDir(appContext);
    initRootShell();
    initDatabase();

    MyLog.d(LOG_TAG, "App started");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    updateLanguage();
  }

  private void updateLanguage() {
    String lang = Settings.getLanguage();
    if (!lang.equals(Settings.DEFVALUE_LANGUAGE)) {
      Locale locale = new Locale(lang);
      Locale.setDefault(locale);
      Configuration config = getResources().getConfiguration();
      config.locale = locale;
      getBaseContext().getResources().updateConfiguration(config, null);
    }
  }

  public void onDBTaskEvent(int event, int statusCode) {
    if (event == DBTask.Event.TASK_FAILED && statusCode == DBTask.Error.DB_DAMAGED)
      eventDispatcher.dispatchGlobalEvent(GlobalEvent.DB_DAMAGED);
  }

  public GlobalEventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  private void initDatabase() {
    if (Settings.isHasDB())
      new DBTask(this, this::onDBTaskEvent, DBTask.Mode.LOAD).execute();
  }

  private void initRootShell() {
    if (Settings.getRequestRoot()) {
      MyLog.d(LOG_TAG, "Need Root access");
      RootShell.init(this);
    }
  }
}
