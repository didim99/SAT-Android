package com.didim99.sat.system;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import com.didim99.sat.R;
import com.didim99.sat.core.sbxeditor.wrapper.SBML;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.utils.MyLog;
import java.io.File;

/**
 * Created by didim99 on 01.06.20.
 */
public class StorageManager {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SM";

  private static final String ICONS_DIR_NAME = "/modules_icons";
  private static final String ICONS_MASK = "/%s.png";
  public static String ICONS_PATH;

  public static void checkIconsDir(Context context) {
    String iconsDir = context.getFilesDir().getAbsolutePath() + ICONS_DIR_NAME;
    ICONS_PATH = iconsDir + ICONS_MASK;
    if (!Settings.isHasIconsDir()) {
      boolean dirCreated = new File(iconsDir).mkdirs();
      Settings.setHasIconsDir(dirCreated);
      if (!dirCreated)
        MyLog.e(LOG_TAG, "Can't create icons directory!");
    }
  }

  public static void checkSystemDirs(Context context) {
    if (!findSystemDirs(context))
      Toast.makeText(context, R.string.systemErr_cacheDirsNotCreated,
        Toast.LENGTH_LONG).show();
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
