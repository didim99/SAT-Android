package com.didim99.sat;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;
import com.didim99.sat.core.sbxeditor.utils.InputValidator;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.event.GlobalEvent;
import com.didim99.sat.event.GlobalEventDispatcher;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.ui.sbxeditor.UIManager;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.RootShell;
import java.io.File;
import java.util.Locale;

/**
 * Root application cass
 * Created by didim99 on 17.02.18.
 */

public class SAT extends Application {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_root";

  public static final String ACTION_PICK_MODULE = "com.didim99.sat.pickModule";
  public static final String EXTRA_PART_ID = "com.didim99.sat.partId";
  //Modules icons
  private static final String ICONS_DIR_NAME = "/modules_icons";
  private static final String ICONS_MASK = "/%s.png";
  public static String ICONS_DIR_PATH, ICONS_PATH;

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

    if (!findSystemDirs(appContext))
      Toast.makeText(appContext, R.string.systemErr_cacheDirsNotCreated,
        Toast.LENGTH_LONG).show();

    new AppUpdateManager(appContext).checkAppVersion();

    if (Settings.isHasDB())
      new DBTask(this, this::onDBTaskEvent, DBTask.Mode.LOAD).execute();
    if (Settings.getRequestRoot()) {
      MyLog.d(LOG_TAG, "Need Root access");
      RootShell.init(appContext);
    }

    //load icons path
    ICONS_DIR_PATH = getFilesDir().getAbsolutePath() + ICONS_DIR_NAME;
    ICONS_PATH = ICONS_DIR_PATH + ICONS_MASK;
    if (!Settings.isHasIconsDir()) {
      boolean dirCreated = new File(ICONS_DIR_PATH).mkdirs();
      Settings.setHasIconsDir(dirCreated);
      if (!dirCreated)
        MyLog.e(LOG_TAG, "Can't create icons directory!");
    }

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

  private static boolean findSystemDirs(Context context) {
    if (new File(Settings.getSysCacheDir()).isDirectory()
      && new File(Settings.getSbxTempDir()).isDirectory()
      && new File(Settings.getResTempDir()).isDirectory())
      return true;

    MyLog.d(LOG_TAG, "Trying to find temporary directories");
    File directory = context.getExternalCacheDir();

    String dirname;
    if (directory != null)
      dirname = directory.getAbsolutePath();
    else
      dirname = Environment.getExternalStorageDirectory().getAbsolutePath();

    if (!dirname.endsWith(SBML.FS_PATH_SEP))
      dirname = dirname.concat(SBML.FS_PATH_SEP);
    String sysCacheDir = dirname;
    String sbxTempDir = dirname.concat(SBML.SANDBOX_TMP_DIR).concat(SBML.FS_PATH_SEP);
    String resTempDir = dirname.concat(SBML.RESOURCES_TMP_DIR).concat(SBML.FS_PATH_SEP);

    boolean success = true;
    if (!(new File(sbxTempDir).mkdirs())) {
      MyLog.e(LOG_TAG, "Can't create sandbox temp directory");
      sysCacheDir = sbxTempDir = resTempDir = "";
      success = false;
    }

    if (!(new File(resTempDir).mkdirs())) {
      MyLog.e(LOG_TAG, "Can't create resources temp directory");
      sysCacheDir = sbxTempDir = resTempDir = "";
      success = false;
    }

    if (success) {
      Settings.setSystemDirs(sysCacheDir, sbxTempDir, resTempDir);
      MyLog.d(LOG_TAG, "Temporary directories created successful"
        + "\n  sysCacheDir: " + sysCacheDir
        + "\n  sbxTempDir: " + sbxTempDir
        + "\n  resTempDir: " + resTempDir
      );
    }

    return success;
  }
}
