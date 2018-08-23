package com.didim99.sat.resconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.didim99.sat.MyLog;
import com.didim99.sat.Utils;
import com.didim99.sat.settings.Settings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * AMBBMP<->PNG converter
 * Created by didim99 on 22.01.18.
 */

class TexConverter extends ResConverter {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexConv";

  private static final String ambMask = ".ambbmp";
  private static final String pngMask = ".png";
  private static final String tmpMask = ".bin";
  private static final byte[] PNG_SIGNATURE = {
    -119, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };

  private Bitmap preview;

  private static native int[] uncompressTexture(String name);
  private static native long compressTexture(String name, int sizeX, int sizeY);

  static {
    System.loadLibrary("app");
  }

  TexConverter(Context context, Config config, ProgressListener listener) {
    super(context, config, listener);
    unpackedSignature = PNG_SIGNATURE;
    unpackedMask = pngMask;
    packedMask = ambMask;
  }

  @Override
  protected long pack(String pngName)
    throws IOException {
    String ambName = pngName.replace(pngMask, ambMask);
    Bitmap bitmap = png2bitmap(pngName);
    preview = bitmap;
    Utils.writeFile(ambName, bitmap2rgba(bitmap));
    if (!Settings.ResConverter.isSaveOriginal())
      new File(pngName).delete();
    return compressTexture(ambName, bitmap.getWidth(), bitmap.getHeight());
  }

  @Override
  protected long unPack(String ambName)
    throws IOException {
    String pngName = ambName.replace(ambMask, pngMask);
    if (Settings.ResConverter.isSaveOriginal()) {
      String tmpName = ambName.replace(ambMask, tmpMask);
      Utils.copyFile(ambName, tmpName);
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

  @Override
  protected long test(String name)
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

  private Bitmap rgba2bitmap(byte[] rawData, int sizeX, int sizeY) {
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

  private void bitmap2png(Bitmap bitmap, String fileName)
    throws FileNotFoundException {
    MyLog.d(LOG_TAG, "Saving bitmap to:\n  " + fileName);
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(fileName));
    MyLog.d(LOG_TAG, "Bitmap saved");
  }

  private Bitmap png2bitmap(String fileName) {
    MyLog.d(LOG_TAG, "Loading bitmap from:\n  " + fileName);
    Bitmap bitmap = BitmapFactory.decodeFile(fileName);
    MyLog.d(LOG_TAG, "Bitmap loaded ");
    return bitmap;
  }

  private byte[] bitmap2rgba(Bitmap bitmap) {
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
}
