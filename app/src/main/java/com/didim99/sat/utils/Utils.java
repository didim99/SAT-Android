package com.didim99.sat.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;
import com.didim99.sat.BuildConfig;
import com.didim99.sat.R;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generic utilities class
 * Created by didim99 on 14.02.18.
 */
public class Utils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Utils";
  public static final String DATE_FORMAT = "yyyy.MM.dd";

  /* ======== STRING UTILS =========== */

  public static String joinStr(String delimiter, String... args) {
    if (args == null || args.length == 0)
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.length - 1;
    for (int i = 0; i < size; i++)
      builder.append(args[i]).append(delimiter);
    builder.append(args[size]);
    return builder.toString();
  }

  public static String joinStr(String delimiter, ArrayList<String> args) {
    if (args == null || args.isEmpty())
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.size() - 1;
    for (int i = 0; i < size; i++)
      builder.append(args.get(i)).append(delimiter);
    builder.append(args.get(size));
    return builder.toString();
  }

  public static String md5(String str) {
    try {
      // Create MD5 Hash
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(str.getBytes());

      // Create Hex String
      StringBuilder hexString = new StringBuilder();
      for (byte digit : digest.digest())
        hexString.append(String.format("%02x", digit & 0xFF));
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      if (BuildConfig.DEBUG)
        e.printStackTrace();
      return "";
    }
  }

  public static String base64Encode(String str) {
    return new String(Base64.encode(str.getBytes(), Base64.DEFAULT)).trim();
  }

  /* ======== TYPE CONVERT UTILS =========== */

  public static String floatToString(Float value, int precision) {
    if (value == null)
      return null;
    return String.format(Locale.US, "%." + precision + "f", value);
  }

  public static String intToString(Integer value) {
    return value == null ? null : String.valueOf(value);
  }

  public static float[] stringArrayToFloatArray(ArrayList<String> args) {
    if (args == null || args.isEmpty())
      return null;
    float[] result = new float[args.size()];
    for (int i = 0; i < args.size(); i++) {
      try {
        result[i] = Float.parseFloat(args.get(i));
      } catch (NumberFormatException ignored) {}
    }
    return result;
  }

  public static String[] floatArrayToStringArray(float[] args, int precision) {
    if (args == null || args.length == 0)
      return null;
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = floatToString(args[i], precision);
    }
    return result;
  }

  /* ======== ARRAY UTILS =========== */

  public static int arrayMax(int... args) {
    int max = Integer.MIN_VALUE;
    for (int value : args)
      if (value > max) max = value;
    return max;
  }

  /* ======== TIME UTILS =========== */

  public static int getTimestamp() {
    return  (int) (System.currentTimeMillis() / 1000);
  }

  public static long timestampToMillis(int timestamp) {
    return (long) timestamp * 1000;
  }

  /* ======== FILE UTILS =========== */

  public static void copyFile(String srcName, String targetName)
    throws IOException {
    if (srcName.equals(targetName))
      return;
    if (new File(targetName).isDirectory()) {
      if (!targetName.endsWith("/"))
        targetName = targetName.concat("/");
      targetName = targetName.concat(new File(srcName).getName());
    }
    MyLog.d(LOG_TAG, "Copying file: " + srcName + "\n  to: " + targetName);
    FileChannel src = new FileInputStream(srcName).getChannel();
    FileChannel out = new FileOutputStream(targetName).getChannel();
    src.transferTo(0, src.size(), out);
    src.close();
    out.close();
  }

  public static void writeFile(String fileName, byte[] data)
    throws IOException {
    MyLog.d(LOG_TAG, "Writing: " + fileName);
    File file = new File(fileName);
    DataOutputStream src = new DataOutputStream(new FileOutputStream(file));
    src.write(data);
    src.flush();
    src.close();
    MyLog.d(LOG_TAG, "Writing completed");
  }

  public static boolean checkPath(Context ctx, String path, boolean allowDir) {
    MyLog.d(LOG_TAG, "Path checking: " + path);
    int status = fileStatus(path, allowDir);
    if (status != 0) {
      String fileStatus = ctx.getString(status);
      MyLog.e(LOG_TAG, "Path check failed: " + fileStatus);
      Toast.makeText(ctx, ctx.getString(R.string.error, fileStatus),
        Toast.LENGTH_LONG).show();
      return false;
    } else {
      MyLog.d(LOG_TAG, "Path check done");
      return true;
    }
  }

  private static int fileStatus(String path, boolean allowDir) {
    if (path.isEmpty())
      return R.string.emptyPath;
    File file = new File(path);
    if (!file.exists() && !getAccessToFile(path))
      return R.string.fileNotExist;
    if (!allowDir && file.isDirectory())
      return R.string.fileIsDir;
    if (allowDir && !file.isDirectory())
      return R.string.fileIsNotDir;
    if (!file.canRead() && !getAccessToFile(path))
      return R.string.fileNotReadable;
    if (!file.canWrite() && !getAccessToFile(path))
      return R.string.fileNotWritable;
    return 0;
  }

  private static boolean getAccessToFile(String path) {
    if (RootShell.hasRootAccess()) {
      RootShell.exec(String.format("chmod 606 %s", path));
      return !RootShell.getError().contains("No such file or directory");
    } else
      return false;
  }

  /* ======== ANDROID UTILS =========== */

  public static boolean isIntentSafe(Context context, Intent intent) {
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    return activities.size() > 0;
  }

  public static String genDeviceUUID() {
    String uuid = Utils.md5(
      Build.BOARD + "\n" + Build.BOOTLOADER + "\n"
        + Build.BRAND + "\n" + Build.DEVICE + "\n"
        + Build.DISPLAY + "\n" + Build.FINGERPRINT + "\n"
        + Build.getRadioVersion() + "\n" + Build.HARDWARE + "\n"
        + Build.HOST + "\n" + Build.ID + "\n"
        + Build.MANUFACTURER + "\n" + Build.MODEL + "\n"
        + Build.PRODUCT + "\n" + Build.TAGS + "\n"
        + Build.TYPE + "\n" + Build.USER );
    MyLog.d(LOG_TAG, "Generated device UUID: " + uuid);
    return uuid;
  }
}
