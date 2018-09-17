package com.didim99.sat.core.resconverter;

import android.content.Context;
import com.didim99.sat.utils.Utils;
import com.didim99.sat.settings.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * AMBSND<->WAV converter
 * Created by didim99 on 23.08.18.
 */
class SoundConverter extends ResConverter {

  private static final String ambMask = ".ambsnd";
  private static final String wavMask = ".wav";
  private static final byte[] RIFF_HEADER = { 0x52, 0x49, 0x46, 0x46 };
  private static final byte[] DATA_HEADER = {
    0x57, 0x41, 0x56, 0x45, 0x66, 0x6d, 0x74, 0x20,
    0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00,
    0x22, 0x56, 0x00, 0x00, 0x44,  -84, 0x00, 0x00,
    0x02, 0x00, 0x10, 0x00, 0x64, 0x61, 0x74, 0x61 };
  private static final int HEADER_LENGTH = RIFF_HEADER.length + 4 + DATA_HEADER.length;

  SoundConverter(Context context, Config config, ProgressListener listener) {
    super(context, config, listener);
    unpackedSignature = RIFF_HEADER;
    unpackedMask = wavMask;
    packedMask = ambMask;
  }

  @Override
  protected long pack(String wavName)
    throws IOException {
    String ambName = wavName.replace(wavMask, ambMask);
    byte[] data = readRawData(wavName);
    byte[] dataHeader = Arrays.copyOfRange(
      data, RIFF_HEADER.length + 4, HEADER_LENGTH);
    if (!Arrays.equals(dataHeader, DATA_HEADER))
      return Error.INCORRECT_FORMAT;
    byte[] outData = Arrays.copyOfRange(
      data, HEADER_LENGTH, data.length);
    Utils.writeFile(ambName, outData);
    if (!Settings.ResConverter.isSaveOriginal())
      new File(wavName).delete();
    return outData.length;
  }

  @Override
  protected long unPack(String ambName)
    throws IOException {
    String wavName = ambName.replace(ambMask, wavMask);
    byte[] data = readRawData(ambName);
    int length = data.length + HEADER_LENGTH;
    byte[] outData = ByteBuffer.allocate(length)
      .order(ByteOrder.LITTLE_ENDIAN).put(RIFF_HEADER)
      .putInt(data.length + DATA_HEADER.length)
      .put(DATA_HEADER).put(data).array();
    Utils.writeFile(wavName, outData);
    if (!Settings.ResConverter.isSaveOriginal())
      new File(ambName).delete();
    return length;
  }

  @Override
  protected long test(String ambName) { return 0; }
}
