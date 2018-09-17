package com.didim99.sat;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.widget.Toast;
import com.didim99.sat.db.DBTask;
import com.didim99.sat.core.sbxconverter.RootShell;
import com.didim99.sat.core.sbxeditor.utils.InputValidator;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.ui.sbxeditor.UIManager;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import java.io.File;
import java.util.Locale;

/**
 * Root application cass
 * Created by didim99 on 17.02.18.
 */

public class SAT extends Application implements DBTask.EventListener {
  public static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_root";

  public enum GlobalEvent { DB_DAMAGED, UI_RELOAD }

  public static final String ACTION_PICK_MODULE = "com.didim99.sat.pickModule";
  public static final String EXTRA_PART_ID = "com.didim99.sat.partId";
  //Modules icons
  private static final String ICONS_DIR_NAME = "/modules_icons";
  private static final String ICONS_MASK = "/%s.png";
  public static String ICONS_DIR_PATH, ICONS_PATH;

  private GlobalEventListener globalEventListener;
  private GlobalEvent lastPendingEvent;

  @Override
  public void onCreate() {
    super.onCreate();
    MyLog.d(LOG_TAG, "App starting...");
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    Context appContext = getApplicationContext();
    UIManager.getInstance().init(appContext);
    InputValidator.getInstance().init(appContext);
    Settings.init(appContext);
    updateLanguage();

    if (!findSystemDirs(appContext))
      Toast.makeText(appContext, R.string.systemErr_cacheDirsNotCreated,
        Toast.LENGTH_LONG).show();

    checkAppVersion();
    if (Settings.isHasDB())
      new DBTask(this, this, DBTask.Mode.LOAD).execute();
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

  @Override
  public void onTaskEvent(int event, int statusCode) {
    if (event == DBTask.Event.TASK_FAILED && statusCode == DBTask.Error.DB_DAMAGED)
      dispatchGlobalEvent(GlobalEvent.DB_DAMAGED);
  }

  public void registerEventListener(GlobalEventListener listener) {
    globalEventListener = listener;
    if (lastPendingEvent != null) {
      listener.onGlobalEvent(lastPendingEvent);
      lastPendingEvent = null;
    }
  }

  public void unregisterEventListener() {
    globalEventListener = null;
  }

  public void dispatchGlobalEvent(GlobalEvent event) {
    if (globalEventListener != null)
      globalEventListener.onGlobalEvent(event);
    else lastPendingEvent = event;
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

  private void checkAppVersion() {
    int lastVersion = Settings.getLastAppVersion();
    int currVersion = BuildConfig.VERSION_CODE;
    if (currVersion > lastVersion) {
      MyLog.d(LOG_TAG, "Nev version installed: " + lastVersion + "-->" + currVersion);
      Settings.setLastAppVersion(currVersion);
      firstStart(lastVersion > 0);
    }
  }

  private void firstStart(boolean update) {
    if (Settings.isFirstStart()) {
      MyLog.d(LOG_TAG, "App first start");
      Settings.setFirstStart(false);
      genDeviceUuid();
      getDeviceInfo();
    }

    if (BuildConfig.VERSION_CODE == 12) {
      boolean cleared = true;
      File cacheDir = new File(Settings.getSysCacheDir());
      for (File file : cacheDir.listFiles()) {
        if (!file.isDirectory())
          cleared &= file.delete();
      }

      if (cleared)
        MyLog.d(LOG_TAG, "Cache directory cleaned");
      else
        MyLog.w(LOG_TAG, "Can't clean cache directory");
    }
  }

  private void getDeviceInfo() {
    DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
    String vendor = Build.MANUFACTURER;
    String model = Build.MODEL;
    String resolution = displaymetrics.widthPixels + "x" + displaymetrics.heightPixels;
    String osVersion = Build.VERSION.RELEASE;
    String abi;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      abi = Utils.joinStr(", ", Build.SUPPORTED_ABIS);
    } else {
      abi = Utils.joinStr(", ", Build.CPU_ABI, Build.CPU_ABI2);
    }

    MyLog.d(LOG_TAG, "Device info:"
      + "\n  vendor: " + vendor
      + "\n  model: " + model
      + "\n  resolution: " + resolution
      + "\n  osVersion: " + osVersion
      + "\n  abi: " + abi
    );

    Settings.setDevVendor(Utils.base64Encode(vendor));
    Settings.setDevModel(Utils.base64Encode(model));
    Settings.setDevRes(Utils.base64Encode(resolution));
    Settings.setDevOsVer(Utils.base64Encode(osVersion));
    Settings.setDevAbi(Utils.base64Encode(abi));
  }

  private void genDeviceUuid() {
    String uuid = Utils.md5(
      Build.BOARD + "\n" + Build.BOOTLOADER + "\n"
        + Build.BRAND + "\n" + Build.DEVICE + "\n"
        + Build.DISPLAY + "\n" + Build.FINGERPRINT + "\n"
        + Build.getRadioVersion() + "\n" + Build.HARDWARE + "\n"
        + Build.HOST + "\n" + Build.ID + "\n"
        + Build.MANUFACTURER + "\n" + Build.MODEL + "\n"
        + Build.PRODUCT + "\n" + Build.TAGS + "\n"
        + Build.TYPE + "\n" + Build.USER );
    MyLog.d(LOG_TAG, "Generated device UUID:\n  " + uuid);
    Settings.setDevId(Utils.base64Encode(uuid));
  }

  /**
   * Application level event listener
   * Created by didim99 on 26.07.18.
   */
  public interface GlobalEventListener {
    void onGlobalEvent(GlobalEvent event);
  }
}
