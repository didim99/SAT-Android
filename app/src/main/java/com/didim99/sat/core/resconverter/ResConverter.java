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

  public enum Action { TEST, PACK, UNPACK }
  public enum Mode { SINGLE_FILE, DIRECTORY }

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
  private Action action;
  private Mode mode;
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
    checkState();

    ArrayList<String> files = new ArrayList<>();
    String mask = getFileMask();
    files.add(startPath);

    if (mode == Mode.DIRECTORY)
      files = scanDir(startPath, mask);
    fileList = new ArrayList<>(files.size());

    logActionStart();
    timer.start();

    int current = 1;
    if (!files.isEmpty()) {
      for (String name : files) {
        if (listener != null)
          listener.onConverterProgressUpdate(files.size(), current++);
        if (!processFile(name)) break;
      }

      logActionEnd();
    } else {
      status = Error.NO_FILES;
    }

    timer.stop();
  }

  private void checkState() {
    if (packedMask == null || packedMask.isEmpty())
      throw new IllegalStateException("packedMask must be defined");
    if (unpackedMask == null || unpackedMask.isEmpty())
      throw new IllegalStateException("unpackedMask must be defined");
    if (unpackedSignature == null || unpackedSignature.length == 0)
      throw new IllegalStateException("unpackedSignature must be defined");
  }

  private String getFileMask() {
    switch (action) {
      case PACK: return unpackedMask;
      case UNPACK:
      case TEST: return packedMask;
      default: return null;
    }
  }

  private void logActionStart() {
    switch (action) {
      case PACK:
        MyLog.d(LOG_TAG, "----- Packing resources... -----");
        break;
      case UNPACK:
        MyLog.d(LOG_TAG, "----- Unpacking resources... -----");
        break;
      case TEST:
        MyLog.d(LOG_TAG, "----- CONVERTER TEST STARTED -----");
        break;
    }
  }

  private void logActionEnd() {
    switch (action) {
      case PACK:
        MyLog.d(LOG_TAG, "----- Packing completed -----");
        break;
      case UNPACK:
        MyLog.d(LOG_TAG, "----- Unpacking completed -----");
        break;
      case TEST:
        MyLog.d(LOG_TAG, "----- CONVERTER TEST COMPLETED -----");
        break;
    }
  }

  private boolean processFile(String name) {
    MyLog.d(LOG_TAG, "----- " + name + " -----");

    try {
      performAction(name);
      if (status < 0)
        throw new ConvertException();
    } catch (IOException e) {
      status = Error.IO_ERROR;
      errMsg = e.getMessage();
      return false;
    } catch (ConvertException e) {
      MyLog.e(LOG_TAG, "An error occurred, stopping conversion!");
      return false;
    }

    fileList.add(new File(name).getName());
    return true;
  }

  private void performAction(String name) throws IOException {
    switch (action) {
      case PACK:
        if (name.endsWith(packedMask) || isPacked(name))
          status = Error.ALREADY_PACKED;
        else status = pack(name);
        break;
      case UNPACK:
        if (name.endsWith(unpackedMask) || !isPacked(name))
          status = Error.NOT_PACKED;
        else status = unPack(name);
        break;
      case TEST:
        status = test(name);
        break;
    }
  }

  final String getStatus() {
    MyLog.d(LOG_TAG, "Checking status...");
    String out;

    if (status >= 0) {
      out = context.getString(R.string.resConverter_actionSuccess,
        getTextAction(), timer.getStr());
    } else {
      String error = getNativeError(getFileMask());
      out = context.getString(R.string.errorFrom, getTextError(), error);
    }

    MyLog.d(LOG_TAG, "Current status: " + out);
    return out;
  }

  private String getTextAction() {
    switch (action) {
      case PACK:
        return context.getString(R.string.resConverter_actionSuccess_pack);
      case UNPACK:
        return context.getString(R.string.resConverter_actionSuccess_unpack);
      default: return null;
    }
  }

  private String getTextError() {
    switch (this.action) {
      case PACK:
        return context.getString(R.string.resConverter_actionFailed_pack);
      case UNPACK:
        return context.getString(R.string.resConverter_actionFailed_unpack);
      default: return null;
    }
  }

  private String getNativeError(String mask) {
    switch ((int) status) {
      case Error.INPUT_FILE:
        return context.getString(R.string.errorCode_inputFile);
      case Error.OUTPUT_FILE:
        return context.getString(R.string.errorCode_outputFile);
      case Error.NO_MEMORY:
        return context.getString(R.string.errorCode_noMemory);
      case Error.NO_FILES:
        return context.getString(R.string.errorCode_no_files, mask);
      case Error.IO_ERROR:
        return errMsg;
      case Error.NOT_PACKED:
        return context.getString(R.string.errorCode_not_packed);
      case Error.ALREADY_PACKED:
        return context.getString(R.string.errorCode_already_packed);
      case Error.INCORRECT_FORMAT:
        return context.getString(R.string.errorCode_incorrect_format);
      default: return null;
    }
  }

  private ArrayList<String> scanDir(String dirName, String mask) {
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
    if (src.read(buff) < buff.length)
      throw new IOException("Unexpected end of file");
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
    private Action action;
    private Mode mode;

    public Config(String path, Mode mode, Action action) {
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

  private static class ConvertException extends IllegalStateException {}
}
