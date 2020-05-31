package com.didim99.sat.system;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import com.didim99.sat.BuildConfig;
import com.didim99.sat.settings.Settings;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.utils.Utils;
import java.io.File;

/**
 * Created by didim99 on 31.05.20.
 */
public class AppUpdateManager {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_updateManager";

  public static void checkAppVersion(Context context) {
    int lastVersion = Settings.getLastAppVersion();
    int currVersion = BuildConfig.VERSION_CODE;
    if (currVersion > lastVersion) {
      MyLog.d(LOG_TAG, "Nev version installed: " + lastVersion + "-->" + currVersion);
      Settings.setLastAppVersion(currVersion);
      firstStart(context, lastVersion > 0);
    }
  }

  private static void firstStart(Context context, boolean update) {
    if (Settings.isFirstStart()) {
      MyLog.d(LOG_TAG, "App first start");
      Settings.setFirstStart(false);
      getDeviceInfo(context);
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

  private static void getDeviceInfo(Context context) {
    String uuid = Utils.genDeviceUUID();
    DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
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
      + "\n  uuid: " + uuid
    );

    Settings.setDevVendor(Utils.base64Encode(vendor));
    Settings.setDevModel(Utils.base64Encode(model));
    Settings.setDevRes(Utils.base64Encode(resolution));
    Settings.setDevOsVer(Utils.base64Encode(osVersion));
    Settings.setDevAbi(Utils.base64Encode(abi));
    Settings.setDevId(Utils.base64Encode(uuid));
  }
}
