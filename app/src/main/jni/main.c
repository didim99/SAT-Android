/*
 * native sasbx encryption-decryption library
 *   Android interface ver 0.2
 * Created by didim99 on 20.02.18
 */

#include "main.h"

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_sbxconverter_SbxConverter_compressFile(
      JNIEnv *env, jclass type, jstring _fileName, jint verCode) {

  const char *fileName = (*env)->GetStringUTFChars(env, _fileName, 0);

  LOG_D("compressFile() is called"); //DEBUG
  LOG_V("Input file name: %s", fileName); //DEBUG
  jlong returnCode = compressFile(fileName, verCode);

  (*env)->ReleaseStringUTFChars(env, _fileName, fileName);
  return returnCode;
}

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_sbxconverter_SbxConverter_uncompressFile(
      JNIEnv *env, jclass type, jstring _fileName) {

  const char *fileName = (*env)->GetStringUTFChars(env, _fileName, 0);

  LOG_D("uncompressFile() is called"); //DEBUG
  LOG_V("Input file name: %s", fileName); //DEBUG
  jlong returnCode = uncompressFile(fileName);

  (*env)->ReleaseStringUTFChars(env, _fileName, fileName);
  return returnCode;
}

JNIEXPORT jlong JNICALL
Java_com_didim99_sat_core_resconverter_TexConverter_compressTexture(
    JNIEnv *env, jclass type, jstring _name, jint sizeX, jint sizeY) {
  const char *name = (*env)->GetStringUTFChars(env, _name, 0);

  LOG_D("Compressing texture:\n  %s\n  size: %dx%d", name, sizeX, sizeY);
  long out = compressTexture(name, sizeX, sizeY);
  LOG_D("Compressing completed");

  (*env)->ReleaseStringUTFChars(env, _name, name);
  return out;
}

JNIEXPORT jintArray JNICALL
Java_com_didim99_sat_core_resconverter_TexConverter_uncompressTexture(
    JNIEnv *env, jclass type, jstring _name) {
  const char *name = (*env)->GetStringUTFChars(env, _name, 0);

  LOG_D("Uncompressing texture:\n  %s", name);
  int res = (int) uncompressTexture(name);
  LOG_D("Uncompressing completed");

  int arrSize, *tmp;
  if (res >= 0) {
    arrSize = 2;
    tmp = &textureWidth;
    tmp[1] = textureHeight;
  } else {
    arrSize = 1;
    tmp = &res;
  }

  jintArray out = (*env)->NewIntArray(env, arrSize);
  (*env)->SetIntArrayRegion(env, out, 0, arrSize, tmp);

  (*env)->ReleaseStringUTFChars(env, _name, name);
  return out;
}