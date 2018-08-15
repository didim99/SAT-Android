package com.didim99.sat.resconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.didim99.sat.MyLog;
import com.didim99.sat.R;
import com.didim99.sat.Utils;
import com.didim99.sat.Utils.Timer;
import com.didim99.sat.settings.Settings;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * AMBBMP<->PNG converter
 * Created by didim99 on 22.01.18.
 */

class TexConverter {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexConv";

  //parameters definitions
  static final int ACTION_TEST = 0;
  static final int ACTION_PACK = 1;
  static final int ACTION_UNPACK = 2;
  static final int MODE_SINGLE_FILE = 0;
  static final int MODE_DIRECTORY = 1;

  private static final int ERR_CODE_INPUT_FILE = -1;
  private static final int ERR_CODE_OUTPUT_FILE = -2;
  private static final int ERR_CODE_NO_MEMORY = -3;
  private static final int ERR_CODE_NO_FILES = -10;
  private static final int ERR_CODE_IO_ERROR = -11;
  private static final int ERR_CODE_NOT_PACKED = -12;
  private static final int ERR_CODE_ALREADY_PACKED = -13;
  private static final String ambMask = ".ambbmp";
  private static final String pngMask = ".png";
  private static final String tmpMask = ".bin";

  private Context context;
  private ProgressListener listener;
  private String startPath;
  private int lastAction;
  private int mode;
  private long status;
  private String errMsg;
  private Bitmap preview;
  private Timer timer = new Timer();

  private static native int[] uncompressTexture(String name);
  private static native long compressTexture(String name, int sizeX, int sizeY);

  static {
    System.loadLibrary("app");
  }

  TexConverter(Context context, Config config, ProgressListener listener) {
    this.context = context;
    this.listener = listener;
    this.startPath = config.path;
    this.lastAction = config.action;
    this.mode = config.mode;
  }

  void convert() {
    String[] list = {startPath};
    String mask = null;
    timer.start();

    switch (lastAction) {
      case ACTION_PACK:
        MyLog.d(LOG_TAG, "----- Packing texture(s)... -----");
        mask = pngMask;
        break;
      case ACTION_UNPACK:
        MyLog.d(LOG_TAG, "----- Unpacking texture(s)... -----");
        mask = ambMask;
        break;
      case ACTION_TEST:
        MyLog.d(LOG_TAG, "----- CONVERTER TEST STARTED -----");
        mask = ambMask;
        break;
    }

    if (mode == MODE_DIRECTORY)
      list = scanDir(startPath, mask);

    int current = 1;
    if (list.length > 0) {
      for (String name : list) {
        if (listener != null)
          listener.onConverterProgressUpdate(list.length, current++);
        MyLog.d(LOG_TAG, "----- " + name + " -----");
        try {
          switch (lastAction) {
            case ACTION_PACK:
              status = pack(name);
              break;
            case ACTION_UNPACK:
              status = unPack(name);
              break;
            case ACTION_TEST:
              status = test(name);
              break;
          }
        } catch (IOException e) {
          status = ERR_CODE_IO_ERROR;
          errMsg = e.getMessage();
        }
      }

      switch (lastAction) {
        case ACTION_PACK:
          MyLog.d(LOG_TAG, "----- Packing completed -----");
          break;
        case ACTION_UNPACK:
          MyLog.d(LOG_TAG, "----- Unpacking completed -----");
          break;
        case ACTION_TEST:
          MyLog.d(LOG_TAG, "----- CONVERTER TEST COMPLETED -----");
          break;
      }
    } else {
      status = ERR_CODE_NO_FILES;
    }

    timer.stop();
  }

  String getStatus() {
    MyLog.d(LOG_TAG, "Checking status...");
    String out, action = "", error = "";

    switch (lastAction) {
      case ACTION_PACK:
        action = context.getString(R.string.resConverter_actionSuccess_pack);
        break;
      case ACTION_UNPACK:
        action = context.getString(R.string.resConverter_actionSuccess_unpack);
        break;
    }

    if (status >= 0) {
      out = context.getString(R.string.resConverter_actionSuccess, action, timer.getStr());
    } else {
      String mask = "";
      switch (lastAction) {
        case ACTION_PACK:
          action = context.getString(R.string.resConverter_actionFailed_pack);
          mask = pngMask;
          break;
        case ACTION_UNPACK:
          action = context.getString(R.string.resConverter_actionFailed_unpack);
          mask = ambMask;
          break;
      }

      switch ((int) status) {
        case ERR_CODE_INPUT_FILE:
          error = context.getString(R.string.errorCode_inputFile);
          break;
        case ERR_CODE_OUTPUT_FILE:
          error = context.getString(R.string.errorCode_outputFile);
          break;
        case ERR_CODE_NO_MEMORY:
          error = context.getString(R.string.errorCode_noMemory);
          break;
        case ERR_CODE_NO_FILES:
          error = context.getString(R.string.errorCode_no_files, mask);
          break;
        case ERR_CODE_IO_ERROR:
          error = errMsg;
          break;
        case ERR_CODE_NOT_PACKED:
          error = context.getString(R.string.errorCode_not_packed);
          break;
        case ERR_CODE_ALREADY_PACKED:
          error = context.getString(R.string.errorCode_already_packed);
          break;
      }

      out = context.getString(R.string.errorFrom, action, error);
    }

    MyLog.d(LOG_TAG, "Current status: " + out);
    return out;
  }

  private long pack(String pngName)
    throws IOException {
    if (!pngName.endsWith(pngMask))
      return ERR_CODE_ALREADY_PACKED;
    String ambName = pngName.replace(pngMask, ambMask);
    Bitmap bitmap = png2bitmap(pngName);
    preview = bitmap;
    Utils.writeFile(ambName, bitmap2rgba(bitmap));
    if (!Settings.ResConverter.isSaveOriginal())
      new File(pngName).delete();
    return compressTexture(ambName, bitmap.getWidth(), bitmap.getHeight());
  }

  private long unPack (String ambName)
    throws IOException {
    if (!ambName.endsWith(ambMask))
      return ERR_CODE_NOT_PACKED;
    String pngName = ambName.replace(ambMask, pngMask);
    if (Settings.ResConverter.isSaveOriginal()) {
      String tmpName = ambName.replace(ambMask, tmpMask);
      FileChannel src = new FileInputStream(ambName).getChannel();
      FileChannel out = new FileOutputStream(tmpName).getChannel();
      src.transferTo(0, src.size(), out);
      src.close();
      out.close();
      ambName = tmpName;
    }
    int[] size = uncompressTexture(ambName);
    if (size.length == 1)
      return size[0];
    Bitmap bitmap = rgba2bitmap(readRawData(ambName), size[0], size[1]);
    new File(ambName).delete();
    preview = bitmap;
    bitmap2png(bitmap, pngName);
    return new File(pngName).length();
  }

  private long test (String name)
    throws IOException {
    String pngName = name.replace(ambMask, pngMask);
    int[] size = uncompressTexture(name);
    if (size.length == 1)
      return size[0];
    int sizeX = size[0];
    int sizeY = size[1];
    byte[] buff = readRawData(name);
    Bitmap bitmap = rgba2bitmap(buff, sizeX, sizeY);
    bitmap2png(bitmap, pngName);
    Bitmap bitmap2 = png2bitmap(pngName);

    if (bitmap2.sameAs(bitmap))
      MyLog.d(LOG_TAG, "----- Bitmap loaded from png! -----");

    byte[] buff2 = bitmap2rgba(bitmap2);
    if (Arrays.equals(buff, buff2))
      MyLog.d(LOG_TAG, "----- Bitmap converted to raw! -----");
    else {
      MyLog.d(LOG_TAG, buff.length + " -> " + buff2.length);
      bitmap2 = null;
      int cnt = 0;
      int[] dmgBuff = new int[sizeX*sizeY];
      for (int i = 0; i < sizeX*sizeY*4; i+=4) {
        if (!(buff[i] == buff2[i] && buff[i + 1] == buff2[i + 1]
          && buff[i + 2] == buff2[i + 2] && buff[i + 3] == buff2[i + 3])) {
          //MyLog.d(LOG_TAG, "--" + i);
          dmgBuff[i] = 0xffff0000;
          cnt++;
            /*MyLog.d(LOG_TAG, "----- " *//*+ tnmBuff[i/4] + " -> " + wtfbuff[i/4]*//*
              + "\n" + buff[i] + " " + buff[i+1] + " " + buff[i+2] + " " + buff[i+3]
              + "\n" + buff2[i] + " " + buff2[i+1] + " " + buff2[i+2] + " " + buff2[i+3]
            );*/
        }
      }
      bitmap = Bitmap.createBitmap(dmgBuff, sizeX, sizeY, Bitmap.Config.ARGB_8888);
      MyLog.e(LOG_TAG, "----- " + cnt + " pixels damaged -----");
    }

    Utils.writeFile(name, buff2);
    preview = bitmap;
    long res = compressTexture(name, sizeX, sizeY);
    return res >= 0 ? 0 : res;
  }

  private String[] scanDir (String dirName, String mask) {
    MyLog.d(LOG_TAG, "Searching for \"" + mask + "\" files in:\n  " + dirName);
    ArrayList<String> files = new ArrayList<>();
    File dir = new File(dirName);
    for (File file : dir.listFiles()) {
      String currName = file.getName();
      if (currName.endsWith(mask)) {
        MyLog.d(LOG_TAG, "found: " + currName);
        files.add(file.getAbsolutePath());
      }
    }
    if (files.isEmpty())
      MyLog.d(LOG_TAG, "\"" + mask + "\" files not found");
    return files.toArray(new String[0]);
  }

  private byte[] readRawData (String fileName)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading:\n  " + fileName);
    File file = new File(fileName);
    byte[] buff = new byte[(int) file.length()];
    MyLog.d(LOG_TAG, "File size: " + file.length());
    DataInputStream src = new DataInputStream(new FileInputStream(file));
    src.readFully(buff);
    src.close();
    MyLog.d(LOG_TAG, "Reading completed");
    return buff;
  }

  private Bitmap rgba2bitmap (byte[] rawData, int sizeX, int sizeY) {
    MyLog.d(LOG_TAG, "Converting data...");
    int[] buff = new int[sizeX * sizeY];
    int pos = 0;
    for (int i = 0; i < sizeX * sizeY; i++) {
      buff[i] = 0;
      buff[i] |= (rawData[pos] << 16) & 0xff0000;
      buff[i] |= (rawData[pos + 1] << 8) & 0xff00;
      buff[i] |= (rawData[pos + 2]) & 0xff;
      buff[i] |= rawData[pos + 3] << 24;
      pos += 4;
    }
    MyLog.d(LOG_TAG, "Data converted");

    MyLog.d(LOG_TAG, "Creating bitmap...");
    Bitmap bitmap = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(buff, 0, sizeX, 0, 0, sizeX, sizeY);
    MyLog.d(LOG_TAG, "Bitmap created");

    return bitmap;
  }

  private void bitmap2png (Bitmap bitmap, String fileName)
    throws FileNotFoundException {
    MyLog.d(LOG_TAG, "Saving bitmap to:\n  " + fileName);
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(fileName));
    MyLog.d(LOG_TAG, "Bitmap saved");
  }

  private Bitmap png2bitmap (String fileName) {
    MyLog.d(LOG_TAG, "Loading bitmap from:\n  " + fileName);
    Bitmap bitmap = BitmapFactory.decodeFile(fileName);
    MyLog.d(LOG_TAG, "Bitmap loaded ");
    return bitmap;
  }

  private byte[] bitmap2rgba (Bitmap bitmap) {
    MyLog.d(LOG_TAG, "Reading bitmap...\n  Config: " + bitmap.getConfig());
    int sizeX = bitmap.getWidth();
    int sizeY = bitmap.getHeight();
    int[] buff = new int[sizeX * sizeY];
    bitmap.getPixels(buff, 0, sizeX, 0, 0, sizeX, sizeY);
    MyLog.d(LOG_TAG, "Bitmap read");

    MyLog.d(LOG_TAG, "Converting data...");
    byte[] rawData = new byte[sizeX * sizeY * 4];
    int pos = 0;
    for (int i = 0; i < sizeX * sizeY; i++) {
      rawData[pos] = (byte) (buff[i] >> 16);
      rawData[pos + 1] = (byte) (buff[i] >> 8);
      rawData[pos + 2] = (byte) buff[i];
      rawData[pos + 3] = (byte) (buff[i] >> 24);

      pos += 4;
    }
    MyLog.d(LOG_TAG, "Data converted");

    return rawData;
  }

  Bitmap getPreview () {
    return preview;
  }

  /**
   * Converter configuration container
   * Created by didim99 on 17.06.18.
   */
  static class Config {
    private String path;
    private int action;
    private int mode;

    Config(String path, int mode, int action) {
      this.path = path;
      this.mode = mode;
      this.action = action;
    }
  }

  interface ProgressListener {
    void onConverterProgressUpdate(int max, int current);
  }
}
