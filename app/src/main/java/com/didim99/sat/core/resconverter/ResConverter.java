package com.didim99.sat.core.resconverter;

import android.content.Context;
import com.didim99.sat.utils.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.utils.Timer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Common resources converter class
 * Created by didim99 on 23.08.18.
 */
public abstract class ResConverter {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ResConv";

  //parameters definitions
  public static final class Type {
    public static final int TEXTURES = 1;
    public static final int SOUNDS = 2;
  }

  public static final class Action {
    public static final int TEST = 0;
    public static final int PACK = 1;
    public static final int UNPACK = 2;
  }

  public static final class Mode {
    public static final int SINGLE_FILE = 0;
    public static final int DIRECTORY = 1;
  }

  static final class Error {
    private static final int INPUT_FILE = -1;
    private static final int OUTPUT_FILE = -2;
    private static final int NO_MEMORY = -3;
    private static final int NO_FILES = -10;
    private static final int IO_ERROR = -11;
    private static final int NOT_PACKED = -12;
    private static final int ALREADY_PACKED = -13;
    static final int INCORRECT_FORMAT = -14;
  }

  String packedMask, unpackedMask;
  byte[] unpackedSignature;

  private Context context;
  private ProgressListener listener;
  private String startPath;
  private int action;
  private int mode;
  private long status;
  private String errMsg;
  private Timer timer;
  private ArrayList<String> fileList;

  ResConverter(Context context, Config config, ProgressListener listener) {
    this.context = context;
    this.listener = listener;
    this.startPath = config.path;
    this.action = config.action;
    this.mode = config.mode;
    timer = new Timer();
  }

  final void convert() {
    if (packedMask == null || packedMask.isEmpty())
      throw new IllegalStateException("packedMask must be defined");
    if (unpackedMask == null || unpackedMask.isEmpty())
      throw new IllegalStateException("unpackedMask must be defined");
    if (unpackedSignature == null || unpackedSignature.length == 0)
      throw new IllegalStateException("unpackedSignature must be defined");

    ArrayList<String> files = new ArrayList<>();
    files.add(startPath);
    String mask = null;
    timer.start();

    switch (action) {
      case Action.PACK:
        MyLog.d(LOG_TAG, "----- Packing resources... -----");
        mask = unpackedMask;
        break;
      case Action.UNPACK:
        MyLog.d(LOG_TAG, "----- Unpacking resources... -----");
        mask = packedMask;
        break;
      case Action.TEST:
        MyLog.d(LOG_TAG, "----- CONVERTER TEST STARTED -----");
        mask = packedMask;
        break;
    }

    if (mode == Mode.DIRECTORY)
      files = scanDir(startPath, mask);
    fileList = new ArrayList<>(files.size());

    int current = 1;
    if (!files.isEmpty()) {
      for (String name : files) {
        if (listener != null)
          listener.onConverterProgressUpdate(files.size(), current++);
        MyLog.d(LOG_TAG, "----- " + name + " -----");

        try {
          switch (action) {
            case Action.PACK:
              if (name.endsWith(packedMask) || isPacked(name))
                status = Error.ALREADY_PACKED;
              else status = pack(name);
              break;
            case Action.UNPACK:
              if (name.endsWith(unpackedMask) || !isPacked(name))
                status = Error.NOT_PACKED;
              else status = unPack(name);
              break;
            case Action.TEST:
              status = test(name);
              break;
          }

          if (status < 0)
            throw new ConvertException();
        } catch (IOException e) {
          status = Error.IO_ERROR;
          errMsg = e.getMessage();
          break;
        } catch (ConvertException e) {
          MyLog.e(LOG_TAG, "An error occurred, stopping conversion!");
          break;
        }

        fileList.add(new File(name).getName());
      }

      switch (action) {
        case Action.PACK:
          MyLog.d(LOG_TAG, "----- Packing completed -----");
          break;
        case Action.UNPACK:
          MyLog.d(LOG_TAG, "----- Unpacking completed -----");
          break;
        case Action.TEST:
          MyLog.d(LOG_TAG, "----- CONVERTER TEST COMPLETED -----");
          break;
      }
    } else {
      status = Error.NO_FILES;
    }

    timer.stop();
  }

  final String getStatus() {
    MyLog.d(LOG_TAG, "Checking status...");
    String out, action = "", error = "";

    switch (this.action) {
      case Action.PACK:
        action = context.getString(R.string.resConverter_actionSuccess_pack);
        break;
      case Action.UNPACK:
        action = context.getString(R.string.resConverter_actionSuccess_unpack);
        break;
    }

    if (status >= 0) {
      out = context.getString(R.string.resConverter_actionSuccess, action, timer.getStr());
    } else {
      String mask = "";
      switch (this.action) {
        case Action.PACK:
          action = context.getString(R.string.resConverter_actionFailed_pack);
          mask = unpackedMask;
          break;
        case Action.UNPACK:
          action = context.getString(R.string.resConverter_actionFailed_unpack);
          mask = packedMask;
          break;
      }

      switch ((int) status) {
        case Error.INPUT_FILE:
          error = context.getString(R.string.errorCode_inputFile);
          break;
        case Error.OUTPUT_FILE:
          error = context.getString(R.string.errorCode_outputFile);
          break;
        case Error.NO_MEMORY:
          error = context.getString(R.string.errorCode_noMemory);
          break;
        case Error.NO_FILES:
          error = context.getString(R.string.errorCode_no_files, mask);
          break;
        case Error.IO_ERROR:
          error = errMsg;
          break;
        case Error.NOT_PACKED:
          error = context.getString(R.string.errorCode_not_packed);
          break;
        case Error.ALREADY_PACKED:
          error = context.getString(R.string.errorCode_already_packed);
          break;
        case Error.INCORRECT_FORMAT:
          error = context.getString(R.string.errorCode_incorrect_format);
          break;
      }

      out = context.getString(R.string.errorFrom, action, error);
    }

    MyLog.d(LOG_TAG, "Current status: " + out);
    return out;
  }

  private ArrayList<String> scanDir (String dirName, String mask) {
    MyLog.d(LOG_TAG, "Searching for \"" + mask + "\" files in:\n  " + dirName);
    ArrayList<String> files = new ArrayList<>();
    File[] dir = new File(dirName).listFiles();
    Arrays.sort(dir);
    for (File file : dir) {
      String currName = file.getName();
      if (currName.endsWith(mask)) {
        MyLog.d(LOG_TAG, "found: " + currName);
        files.add(file.getAbsolutePath());
      }
    }
    if (files.isEmpty())
      MyLog.d(LOG_TAG, "\"" + mask + "\" files not found");
    return files;
  }

  private boolean isPacked(String fileName)
    throws IOException {
    byte[] buff = new byte[unpackedSignature.length];
    DataInputStream src = new DataInputStream(new FileInputStream(fileName));
    src.read(buff);
    src.close();
    return !Arrays.equals(buff, unpackedSignature);
  }

  byte[] readRawData(String fileName)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading: " + fileName);
    File file = new File(fileName);
    byte[] buff = new byte[(int) file.length()];
    MyLog.d(LOG_TAG, "File size: " + file.length());
    DataInputStream src = new DataInputStream(new FileInputStream(file));
    src.readFully(buff);
    src.close();
    MyLog.d(LOG_TAG, "Reading completed");
    return buff;
  }

  ArrayList<String> getFileList() {
    return fileList;
  }

  /**
   * Converter configuration container
   * Created by didim99 on 17.06.18.
   */
  public static class Config {
    private String path;
    private int action;
    private int mode;

    public Config(String path, int mode, int action) {
      this.path = path;
      this.mode = mode;
      this.action = action;
    }
  }

  protected abstract long pack(String fileName) throws IOException;
  protected abstract long unPack(String fileName) throws IOException;
  protected abstract long test(String fileName) throws IOException;

  interface ProgressListener {
    void onConverterProgressUpdate(int max, int current);
  }

  private class ConvertException extends IllegalStateException {}
}
