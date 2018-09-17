package com.didim99.sat.core.sbxconverter;

import android.content.Context;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import java.util.Locale;

/**
 * sasbx converter class
 * Created by didim on 03.08.17.
 */

public class SbxConverter {
  private static final String LOG_TAG = "SED_log_SbxConverter";

  //parameters definitions
  public static final int ACTION_COMPRESS = 1;
  public static final int ACTION_UNCOMPRESS = 2;
  //status codes
  public static final int STATUS_CODE_OK = 0;

  private Context context;
  private String inFileName;
  private int action;
  private int verCode;
  private int lastAction;
  private long status;

  private native long compressFile (String fileName, int verCode);
  private native long uncompressFile (String fileName);

  // Used to load the 'crypto' library.
  static {
    System.loadLibrary("app");
  }

  public SbxConverter(Context context, Config config) {
    this.context = context;
    this.inFileName = config.path;
    this.action = config.action;
    this.verCode = config.verCode;
  }

  public void convert() {
    long res = 0;

    switch (action) {
      case ACTION_COMPRESS: {
        MyLog.d(LOG_TAG, "Compressing file...");
        lastAction = ACTION_COMPRESS;
        res = compressFile(inFileName, verCode);
        if (res < 0)
          MyLog.e(LOG_TAG, "Compression error (code: " + res + ")");
        else
          MyLog.d(LOG_TAG, "Compression successful (" + res + " bytes)");
        break;
      }
      case ACTION_UNCOMPRESS: {
        MyLog.d(LOG_TAG, "Decompressing file...");
        res = uncompressFile(inFileName);
        lastAction = ACTION_UNCOMPRESS;
        if (res < 0)
          MyLog.e(LOG_TAG, "Decompression error (code: " + res + ")");
        else
          MyLog.d(LOG_TAG, "Decompression successful (" + res + " bytes)");
        break;
      }
    }

    status = res;
  }

  String getStatus () {
    MyLog.d(LOG_TAG, "Checking status...");
    String out, action = "", error = "";

    switch (lastAction) {
      case ACTION_COMPRESS:
        action = context.getString(R.string.actionSuccess_compress);
        break;
      case ACTION_UNCOMPRESS:
        action = context.getString(R.string.actionSuccess_uncompress);
        break;
    }

    if (status >= 0) {
      out = context.getString(R.string.sbxConverter_actionSuccess,
        action, "%s", formatBytes(status, 2));
    } else {
      switch (lastAction) {
        case ACTION_COMPRESS:
          action = context.getString(R.string.actionFailed_compress);
          break;
        case ACTION_UNCOMPRESS:
          action = context.getString(R.string.actionFailed_uncompress);
          break;
      }

      switch ((int) status) {
        case -1:
          error = context.getString(R.string.errorCode_inputFile);
          break;
        case -2:
          error = context.getString(R.string.errorCode_outputFile);
          break;
        case -3:
          error = context.getString(R.string.errorCode_noMemory);
          break;
        case -4:
          error = context.getString(R.string.errorCode_versionCode);
          break;
        case -5:
          error = context.getString(R.string.errorCode_notEncrypted);
          break;
        case -6:
          error = context.getString(R.string.errorCode_alreadyEncrypted);
          break;
      }

      out = context.getString(R.string.errorFrom, action, error);
    }

    MyLog.d(LOG_TAG, "Current status: " + out);
    return out;
  }

  public int getStatusCode() {
    return status >= 0 ? STATUS_CODE_OK : (int) status;
  }

  //Форматирует байты в 'Kb', 'Mb', 'Gb', 'Tb'
  private String formatBytes (long bytes, int precision) {
    String units[] = {"b", "Kb", "Mb", "Gb", "Tb"};

    if (bytes == 0) return "0 " + units[0];

    int pow = (int) Math.floor(Math.log(bytes) / Math.log(1024));
    pow = Math.min(pow, units.length - 1);
    double outBytes = bytes / Math.pow(1024, pow);

    String format = "%." + precision + "f";
    String out = String.format(Locale.US, format, outBytes) + ' ' + units[pow];
    MyLog.d(LOG_TAG, "Converting: " + bytes + " bytes --> " + out);
    return out;
  }

  /**
   * Converter configuration container
   * Created by didim99 on 17.06.18.
   */
  public static class Config {
    private String path;
    private int action;
    private int verCode;

    Config(String path) {
      this.path = path;
    }

    public Config(String path, int action, int verCode) {
      this.path = path;
      this.action = action;
      this.verCode = verCode;
    }

    public void setAction(int action) {
      this.action = action;
    }

    public void setVerCode(int verCode) {
      this.verCode = verCode;
    }

    String getPath() {
      return path;
    }
  }
}