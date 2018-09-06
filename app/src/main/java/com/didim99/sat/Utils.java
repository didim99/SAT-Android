package com.didim99.sat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Base64;
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

  public static String joinStr (String delimiter, String... args) {
    if (args == null || args.length == 0)
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.length - 1;
    for (int i = 0; i < size; i++)
      builder.append(args[i]).append(delimiter);
    builder.append(args[size]);
    return builder.toString();
  }

  public static String joinStr (String delimiter, ArrayList<String> args) {
    if (args == null || args.isEmpty())
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.size() - 1;
    for (int i = 0; i < size; i++)
      builder.append(args.get(i)).append(delimiter);
    builder.append(args.get(size));
    return builder.toString();
  }

  public static String floatToString (Float value, int precision) {
    if (value == null)
      return null;
    return String.format(Locale.US, "%." + precision + "f", value);
  }

  public static String intToString (Integer value) {
    return value == null ? null : String.valueOf(value);
  }

  public static float[] stringArrayToFloatArray (ArrayList<String> args) {
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

  public static String[] FloatArrayToStringArray (float[] args, int precision) {
    if (args == null || args.length == 0)
      return null;
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = floatToString(args[i], precision);
    }
    return result;
  }

  public static int arrayMax (int... args) {
    int max = Integer.MIN_VALUE;
    for (int value : args)
      if (value > max) max = value;
    return max;
  }

  public static int getTimestamp() {
    return  (int) (System.currentTimeMillis() / 1000);
  }

  public static long timestampToMillis(int timestamp) {
    return (long) timestamp * 1000;
  }

  static String md5 (String str) {
    try {
      // Create MD5 Hash
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(str.getBytes());
      byte messageDigest[] = digest.digest();

      // Create Hex String
      StringBuilder hexString = new StringBuilder();
      for (byte aMessageDigest : messageDigest) {
        String h = Integer.toHexString(0xFF & aMessageDigest);
        if (h.length() < 2)
          h = "0" + h;
        hexString.append(h);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      if (BuildConfig.DEBUG)
        e.printStackTrace();
    }
    return "";
  }

  public static String base64Encode(String str) {
    return new String (Base64.encode(str.getBytes(), Base64.DEFAULT)).trim();
  }

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

  public static boolean isIntentSafe(Context context, Intent intent) {
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    return activities.size() > 0;
  }

  /**
   * simple timer class
   * Created by didim99 on 28.01.18.
   */
  public static class Timer {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Timer";
    private static final long TICKS_PER_MS = 1000000;
    private long timer;

    public void start () {
      timer = System.nanoTime();
    }

    public void stop () {
      timer = System.nanoTime() - timer;
    }

    public long getMillis() {
      return timer / TICKS_PER_MS;
    }

    public String getStr() {
      return longToTime (timer);
    }

    private String longToTime (long time) {
      time /= TICKS_PER_MS;
      if (time < 60000)
        return String.format(Locale.US, "%.3f сек.", time / 1000f);
      else {
        String out;
        long millis = time % 1000;
        time /= 1000;
        long sec = time % 60;
        time /= 60;
        long min = time % 60;
        time /= 60;
        if (time == 0)
          out = String.format(Locale.US, "%d:%02d.%03d", min, sec, millis);
        else
          out = String.format(Locale.US, "%d:%02d:%02d.%03d", time, min, sec, millis);

        MyLog.d(LOG_TAG, "Converting time: " + timer + " --> " + out);
        return out;
      }
    }
  }
}
